package io.github.bilektugrul.bduels.duels;

import com.hakan.inventoryapi.InventoryAPI;
import com.hakan.inventoryapi.inventory.ClickableItem;
import com.hakan.inventoryapi.inventory.HInventory;
import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.arenas.ArenaManager;
import io.github.bilektugrul.bduels.arenas.ArenaState;
import io.github.bilektugrul.bduels.economy.EconomyAdapter;
import io.github.bilektugrul.bduels.features.stats.StatisticType;
import io.github.bilektugrul.bduels.stuff.MessageType;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserState;
import io.github.bilektugrul.bduels.utils.Utils;
import me.despical.commons.compat.XMaterial;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;

public class DuelManager {

    private final BDuels plugin;
    private final InventoryAPI inventoryAPI;
    private final EconomyAdapter economy;
    private final ArenaManager arenaManager;
    private final BukkitScheduler scheduler;

    private final List<DuelRequestProcess> duelRequestProcesses = new ArrayList<>();
    private final List<MoneyBetSettings> moneyBetSettingsCache = new ArrayList<>();
    private final List<Duel> ongoingDuels = new ArrayList<>();
    private boolean delayTeleport = false;

    private static final int[] midGlasses = {4, 13, 22, 31, 40};
    private static final int[] playerSide = {0, 1, 2, 3, 9, 10, 11, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47};
    private static final int[] opponentSide = {5, 6, 7, 8, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35, 41, 42, 43, 44, 51, 52, 53};
    private static final int[] playerMoneySide = {0, 1, 2, 3};
    private static final int[] opponentMoneySide = {5, 6, 7, 8};

    private static ItemStack glass = XMaterial.BLACK_STAINED_GLASS_PANE.parseItem();
    private static ItemStack greenGlass = XMaterial.GREEN_STAINED_GLASS_PANE.parseItem();
    private static ItemStack redGlass = XMaterial.RED_STAINED_GLASS_PANE.parseItem();
    private static ItemStack cancelItem = new ItemStack(Material.BARRIER);
    private static final ItemStack playerHead = XMaterial.PLAYER_HEAD.parseItem();


    public DuelManager(BDuels plugin) {
        this.plugin = plugin;
        this.inventoryAPI = plugin.getInventoryAPI();
        this.economy = plugin.getEconomyAdapter();
        this.arenaManager = plugin.getArenaManager();
        this.scheduler = plugin.getServer().getScheduler();
        reload();
    }

    public void reload() {
        endProcesses();
        endMatches(DuelEndReason.RELOAD, false);
        prepareMoneyBetItems();
        prepareGuiItems();
        delayTeleport = Utils.getBoolean("delay-teleport");
    }

    public void prepareGuiItems() {
        FileConfiguration config = plugin.getConfig();
        XMaterial.matchXMaterial(config.getString("mid-item.material")).ifPresent(material -> glass = material.parseItem());
        XMaterial.matchXMaterial(config.getString("ready-item.material")).ifPresent(material -> greenGlass = material.parseItem());
        XMaterial.matchXMaterial(config.getString("not-ready-item.material")).ifPresent(material -> redGlass = material.parseItem());
        XMaterial.matchXMaterial(config.getString("request-cancel-item.material")).ifPresent(material -> cancelItem = material.parseItem());

        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(Utils.getString("mid-item.name", null));
        glassMeta.setLore(Utils.getStringList("mid-item.lore", null));
        glass.setItemMeta(glassMeta);

        ItemMeta greenGlassMeta = greenGlass.getItemMeta();
        greenGlassMeta.setDisplayName(Utils.getString("ready-item.name", null));
        greenGlassMeta.setLore(Utils.getStringList("ready-item.lore", null));
        greenGlass.setItemMeta(greenGlassMeta);

        ItemMeta redGlassMeta = redGlass.getItemMeta();
        redGlassMeta.setDisplayName(Utils.getString("not-ready-item.name", null));
        redGlassMeta.setLore(Utils.getStringList("not-ready-item.lore", null));
        redGlass.setItemMeta(redGlassMeta);

        ItemMeta cancelItemMeta = cancelItem.getItemMeta();
        cancelItemMeta.setDisplayName(Utils.getString("request-cancel-item.name", null));
        cancelItemMeta.setLore(Utils.getStringList("request-cancel-item.lore", null));
        cancelItem.setItemMeta(cancelItemMeta);
    }

    public void prepareMoneyBetItems() {
        moneyBetSettingsCache.clear();
        FileConfiguration config = plugin.getConfig();
        for (String key : config.getConfigurationSection("money-bet").getKeys(false)) {
            String path = "money-bet." + key + ".";

            ItemStack item = XMaterial.matchXMaterial(config.getString(path + "item")).orElse(XMaterial.GRAY_DYE).parseItem();
            int moneyToAdd = Utils.getInt(path + "money-to-add");
            String name = Utils.getString(path + "name", null, false);
            List<String> lore = Utils.getStringList(path + "lore", null);

            MoneyBetSettings settings = new MoneyBetSettings(item, moneyToAdd, lore, name);
            moneyBetSettingsCache.add(settings);
        }
    }

    public DuelRequestProcess getProcess(User player) {
        for (DuelRequestProcess process : duelRequestProcesses) {
            if (process.getPlayer().equals(player) || process.getOpponent().equals(player)) {
                return process;
            }
        }
        return null;
    }

    public boolean canSendOrAcceptDuel(User user) {
        if (user == null) {
            return false;
        }

        return user.getRequestProcess() == null && user.getState() == UserState.FREE;
    }

    public boolean sendDuelRequest(User sender, User opponent) {
        if (sender == null || opponent == null) {
            return false;
        }

        Player senderPlayer = sender.getBase();
        Player opponentPlayer = opponent.getBase();

        if (senderPlayer.equals(opponentPlayer)) {
            return false;
        }

        if (!canSendOrAcceptDuel(sender) || !canSendOrAcceptDuel(opponent)) {
            senderPlayer.sendMessage(Utils.getMessage("duel.not-now", senderPlayer)
                    .replace("%opponent%", opponent.getName()));
            return false;
        }

        if (!opponent.doesAcceptDuelRequests()) {
            senderPlayer.sendMessage(Utils.getMessage("duel.does-not-accept", senderPlayer)
                    .replace("%opponent%", opponentPlayer.getName()));
            return false;
        }

        if (!sender.doesAcceptDuelRequests()) {
            senderPlayer.sendMessage(Utils.getMessage("duel.do-not-accept", senderPlayer)
                    .replace("%opponent%", opponentPlayer.getName()));
            return false;
        }

        if (!arenaManager.isAnyArenaAvailable()) {
            senderPlayer.sendMessage(Utils.getMessage("arenas.all-in-usage", senderPlayer));
            return false;
        }

        senderPlayer.sendMessage(Utils.getMessage("duel.request-sent", opponentPlayer)
                .replace("%opponent%", opponentPlayer.getName()));
        opponentPlayer.sendMessage(Utils.getMessage("duel.new-request", opponentPlayer)
                .replace("%sender%", senderPlayer.getName()));

        DuelRequestProcess process = new DuelRequestProcess(sender, opponent);
        sender.setRequestProcess(process);
        opponent.setRequestProcess(process);

        duelRequestProcesses.add(process);
        return true;
    }

    public void acceptDuelRequest(DuelRequestProcess process, List<String> timer) {
        if (process == null || timer == null || timer.isEmpty()) {
            return;
        }

        process.setRequestAccepted(true);

        User sender = process.getPlayer();
        User opponent = process.getOpponent();

        Player senderPlayer = sender.getBase();
        Player opponentPlayer = opponent.getBase();

        String senderName = senderPlayer.getName();
        String opponentName = opponentPlayer.getName();

        senderPlayer.sendMessage(Utils.getMessage("duel.request-accepted", senderPlayer)
                .replace("%opponent%", opponentName));
        opponentPlayer.sendMessage(Utils.getMessage("duel.request-accepted", opponentPlayer)
                .replace("%opponent%", opponentName));

        scheduler.runTaskLater(plugin, () -> {
            timer.remove(opponentName);
            HInventory inventory = inventoryAPI.getInventoryCreator()
                    .setTitle(Utils.getString("request-gui-name", sender.getBase())
                            .replace("%opponent%", opponentName))
                    .setClosable(false)
                    .setId(senderName + "-bDuels")
                    .create();

            inventory.guiAir();

            for (int i : midGlasses) {
                inventory.setItem(i, ClickableItem.empty(replaceLoreAndName(glass, process, null)));
            }

            inventory.setItem(49, ClickableItem.of(replaceLoreAndName(cancelItem, process, null),
                    event -> cancel((Player) event.getWhoClicked(), process, true)));

            putAcceptItem(inventory, 48, sender, process);
            putAcceptItem(inventory, 50, opponent, process);
            putMoneyBetItems(inventory, playerMoneySide, sender, process);
            putMoneyBetItems(inventory, opponentMoneySide, opponent, process);
            updateHeads(inventory, process);

            inventory.open(senderPlayer);
            inventory.open(opponentPlayer);
        }, 40);

    }

    public ItemStack replaceLoreAndName(ItemStack itemStack, DuelRequestProcess process, Player who) {
        ItemStack newItem = itemStack.clone();
        ItemMeta meta = newItem.getItemMeta();
        List<String> oldLore = meta.getLore();

        if (oldLore == null) {
            return itemStack;
        }

        List<String> newLore = new ArrayList<>();
        for (String string : oldLore) {
            string = string.replace("%sender%", process.getPlayer().getName())
                    .replace("%opponent%", process.getOpponent().getName());
            if (who != null) {
                string = string.replace("%who%", who.getName());
            }
            newLore.add(string);
        }

        meta.setLore(newLore);
        newItem.setItemMeta(meta);
        return newItem;
    }

    public void updateHeads(HInventory inventory, DuelRequestProcess process) {
        if (inventory == null || process == null) {
            return;
        }

        ItemStack playerSkull = playerHead.clone();
        ItemStack opponentSkull = playerHead.clone();
        inventory.setItem(12, ClickableItem.empty(replaceLoreAndName(playerSkull, process, null)));
        inventory.setItem(14, ClickableItem.empty(replaceLoreAndName(opponentSkull, process, null)));
        updateMetas(inventory, process);
    }

    public void updateMetas(HInventory inventory, DuelRequestProcess process) {
        if (inventory == null || process == null) {
            return;
        }

        Inventory original = inventory.getInventory();
        ItemStack playerSkull = original.getItem(12);
        ItemStack opponentSkull = original.getItem(14);

        User playerUser = process.getPlayer();
        Player player = playerUser.getBase();

        User opponentUser = process.getOpponent();
        Player opponent = opponentUser.getBase();

        List<String> playerLore = Utils.getHeadInfo(player, process.getDuelRewards().get(playerUser).getMoneyBet());
        List<String> opponentLore = Utils.getHeadInfo(opponent, process.getDuelRewards().get(opponentUser).getMoneyBet());

        ItemMeta playerMeta = playerSkull.getItemMeta();
        ItemMeta opponentMeta = opponentSkull.getItemMeta();

        playerMeta.setDisplayName(Utils.getMessage("duel.request-gui.heads-name", player));
        playerMeta.setLore(playerLore);
        SkullMeta skullMeta = (SkullMeta) playerMeta;
        skullMeta.setOwner(player.getName());
        playerSkull.setItemMeta(skullMeta);

        opponentMeta.setDisplayName(Utils.getMessage("duel.request-gui.heads-name", opponent));
        opponentMeta.setLore(opponentLore);
        SkullMeta opponentSkullMeta = (SkullMeta) opponentMeta;
        opponentSkullMeta.setOwner(opponent.getName());
        opponentSkull.setItemMeta(opponentMeta);
    }

    public void putMoneyBetItems(HInventory inventory, int[] side, User user, DuelRequestProcess process) {
        if (inventory == null || user == null || process == null) {
            return;
        }

        int index = 0;
        for (int i : side) {
            MoneyBetSettings settings = moneyBetSettingsCache.get(index);
            index++;
            ItemStack item = replaceLoreAndName(settings.getItem(), process, null);
            int moneyToAdd = settings.getMoneyToAdd();
            inventory.setItem(i, ClickableItem.of(item, event -> {
                if (process.isFinished(user)) {
                    return;
                }

                Player clicker = (Player) event.getWhoClicked();
                DuelRewards rewards = process.getRewardsOf(user);
                if (clicker.equals(user.getBase())) {
                    if (!Utils.canPutMoreMoney(rewards.getMoneyBet(), moneyToAdd, clicker)) {
                        clicker.sendMessage(Utils.getMessage("duel.not-enough-money", clicker));
                        return;
                    }
                    rewards.addMoneyToBet(moneyToAdd);
                    updateMetas(inventory, process);
                    clicker.sendMessage(Utils.getMessage("duel.bet-money-added", clicker)
                            .replace("%amount%", String.valueOf(moneyToAdd))
                            .replace("%total%", String.valueOf(rewards.getMoneyBet())));
                }
            }));
        }
    }

    public void putAcceptItem(HInventory inventory, int slot, User user, DuelRequestProcess process) {
        if (inventory == null || user == null || process == null) {
            return;
        }

        Player player = user.getBase();
        inventory.setItem(slot, ClickableItem.of(replaceLoreAndName(redGlass, process, player), event -> {
            Player clicker = (Player) event.getWhoClicked();
            if (clicker.equals(player)) {
                boolean newFinished = !process.isFinished(user);
                process.setFinished(user, newFinished);
                if (newFinished) {
                    event.setCurrentItem(replaceLoreAndName(greenGlass, process, clicker));
                    if (process.isBothFinished()) {
                        inventory.setClosable(true);
                        startMatch(process);
                    }
                } else {
                    event.setCurrentItem(replaceLoreAndName(redGlass, process, player));
                }
            }
        }));
    }

    public void cancel(Player canceller, DuelRequestProcess requestProcess, boolean remove) {
        if (canceller == null || requestProcess == null) {
            return;
        }

        for (User user : requestProcess.getPlayers()) {
            user.setRequestProcess(null);
            user.setState(UserState.FREE);
            Player player = user.getBase();
            HInventory inventory = inventoryAPI.getInventoryManager().getPlayerInventory(player);
            if (inventory != null) {
                inventory.close(player);
            }
            player.sendMessage(Utils.getMessage("duel.cancelled", player)
                    .replace("%who%", canceller.getName()));
        }
        if (remove) {
            duelRequestProcesses.remove(requestProcess);
        }
    }

    public void startMatch(DuelRequestProcess requestProcess) {
        if (requestProcess == null) {
            return;
        }

        cancel(null, requestProcess, true);
        if (!arenaManager.isAnyArenaAvailable()) {
            for (User user : requestProcess.getPlayers()) {
                user.sendMessage("arenas.all-in-usage-2");
            }
            return;
        }

        Arena matchArena = arenaManager.findNextEmptyArenaIfPresent();
        matchArena.setState(ArenaState.PRE_MATCH);

        Duel duel = new Duel(requestProcess, matchArena);
        duel.startCountdown();
        ongoingDuels.add(duel);

        for (User user : duel.getPlayers()) {
            economy.removeMoney(user.getBase(), duel.getRewardsOf(user).getMoneyBet());
        }
    }

    private void clearArena(Duel duel, boolean sync) {
        if (duel == null) {
            return;
        }

        Location maxArea = duel.getArena().getEdge();
        Location minArea = duel.getArena().getOtherEdge();

        if (minArea == null || maxArea == null) {
            return;
        }

        if (!sync) {
            scheduler.runTaskAsynchronously(plugin, () -> startCleaning(minArea, maxArea, sync));
        } else {
            startCleaning(minArea, maxArea, sync);
        }
    }

    private void startCleaning(Location minArea, Location maxArea, boolean sync) {
        List<String> blocksToClear = plugin.getConfig().getStringList("whitelisted-blocks");
        for (int x = (int) minArea.getX(); x <= maxArea.getX(); x++) {
            for (int y = (int) minArea.getY(); y <= maxArea.getY(); y++) {
                for (int z = (int) minArea.getZ(); z <= maxArea.getZ(); z++) {
                    Block block = minArea.getWorld().getBlockAt(x, y, z);

                    if (blocksToClear.contains(block.getType().name())) {
                        if (!sync) {
                            scheduler.runTask(plugin, () -> block.setType(Material.AIR));
                        } else {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    public void endMatch(Duel duel, DuelEndReason reason, boolean sync) {
        if (duel == null || reason == null) {
            return;
        }

        duel.cancel();

        User winner = duel.getWinner();
        User loser = duel.getLoser();
        Player winnerPlayer = winner.getBase();
        Player loserPlayer = loser.getBase();

        Arena arena = duel.getArena();
        arena.setState(ArenaState.POST_MATCH);

        clearArena(duel, sync);

        boolean isReloadOrStop = reason == DuelEndReason.RELOAD || reason == DuelEndReason.SERVER_STOP;
        if (!isReloadOrStop) {
            ongoingDuels.remove(duel);
        }

        for (User user : duel.getPlayers()) {
            Player player = user.getBase();
            player.removeMetadata("god-mode-bduels", plugin); // DuelStartingTask tamamlanmadan maç bittiyse bug olmaması için tekrar siliyoruz
            if (delayTeleport) {
                scheduler.runTaskLater(plugin, () -> player.teleport(duel.getPreDuelLocations().get(user)), 40);
            } else {
                scheduler.runTask(plugin, () -> player.teleport(duel.getPreDuelLocations().get(user)));

            }
            player.setHealth(player.getMaxHealth());
            user.setState(UserState.FREE);
            user.setDuel(null);
        }

        arena.setState(ArenaState.EMPTY);

        DuelRewards loserRewards = duel.getRewardsOf(loser);
        List<ItemStack> loserItemsPut = loserRewards.getItemsBet();
        int loserMoneyBet = loserRewards.getMoneyBet();
        int loserItemsPutSize = loserItemsPut.size();

        DuelRewards winnerRewards = duel.getRewardsOf(winner);
        List<ItemStack> winnerItemsPut = winnerRewards.getItemsBet();
        int winnerMoneyBet = winnerRewards.getMoneyBet();

        boolean timeEnded = reason == DuelEndReason.TIME_ENDED;
        if (isReloadOrStop || timeEnded) {
            giveItems(winnerItemsPut, winnerPlayer);
            giveItems(loserItemsPut, loserPlayer);

            winnerPlayer.sendMessage(Utils.getMessage((timeEnded ? "duel.time-ended" : "duel.match-force-ended"), winnerPlayer));
            loserPlayer.sendMessage(Utils.getMessage((timeEnded ? "duel.time-ended" : "duel.match-force-ended"), loserPlayer));

            economy.addMoney(winnerPlayer, winnerMoneyBet);
            economy.addMoney(loserPlayer, loserMoneyBet);
            return;
        }

        MessageType messageType = MessageType.valueOf(Utils.getMessage("duel.win.used-mode"));
        Utils.sendWinMessage(messageType, winnerPlayer, loserPlayer, String.valueOf(loserItemsPutSize), String.valueOf(loserMoneyBet));

        giveItems(winnerItemsPut, winnerPlayer); // Kazanan kişinin ortaya koyduğu eşyaları geri verir, önce bunu yapıyoruz çünkü adam kendi eşyalarını kaybetmemeli
        giveItems(loserItemsPut, winnerPlayer); // Kaybeden kişinin ortaya koyduğu eşyaları kazanana verir

        winner.addStat(StatisticType.WINS, 1);
        winner.addStat(StatisticType.WIN_STREAK, 1);
        winner.addStat(StatisticType.TOTAL_EARNED_ITEM, loserItemsPutSize);
        winner.addStat(StatisticType.TOTAL_EARNED_MONEY, loserMoneyBet);
        loser.addStat(StatisticType.TOTAL_LOST_ITEM, loserItemsPutSize);
        loser.addStat(StatisticType.TOTAL_LOST_MONEY, loserMoneyBet);
        loser.addStat(StatisticType.LOSES, 1);
        loser.setStat(StatisticType.WIN_STREAK, 0);
        economy.addMoney(winnerPlayer, loserMoneyBet + winnerMoneyBet);
    }

    public void giveItems(List<ItemStack> items, Player player) {
        if (items == null || items.isEmpty() || player == null) {
            return;
        }

        Inventory inventory = player.getInventory();
        World world = player.getWorld();
        Location location = player.getLocation();

        for (ItemStack item : items) {
            if (Utils.hasSpace(inventory, item)) {
                inventory.addItem(item);
            } else {
                world.dropItem(location, item);
            }
        }
    }

    public void endMatches(DuelEndReason reason, boolean sync) {
        for (Duel duel : ongoingDuels) {
            duel.setWinner(duel.getPlayers()[0]);
            duel.getStartingTask().cancel();
            endMatch(duel, reason, sync);
        }
        ongoingDuels.clear();
    }

    public void endProcesses() {
        for (DuelRequestProcess process : duelRequestProcesses) {
            cancel(null, process, false);
        }
        duelRequestProcesses.clear();
    }

    public List<DuelRequestProcess> getDuelRequestProcesses() {
        return duelRequestProcesses;
    }

    public List<Duel> getOngoingDuels() {
        return ongoingDuels;
    }

    public int[] getOpponentSide() {
        return opponentSide;
    }

    public int[] getPlayerSide() {
        return playerSide;
    }

}