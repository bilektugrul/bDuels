package io.github.bilektugrul.bduels.duels;

import com.hakan.inventoryapi.InventoryAPI;
import com.hakan.inventoryapi.inventory.ClickableItem;
import com.hakan.inventoryapi.inventory.HInventory;
import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.arenas.ArenaManager;
import io.github.bilektugrul.bduels.arenas.ArenaState;
import io.github.bilektugrul.bduels.economy.EconomyAdapter;
import io.github.bilektugrul.bduels.stats.StatisticType;
import io.github.bilektugrul.bduels.stuff.MessageType;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserState;
import io.github.bilektugrul.bduels.utils.Utils;
import me.despical.commons.compat.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class DuelManager {

    private final BDuels plugin;
    private final InventoryAPI inventoryAPI;
    private final EconomyAdapter economy;
    private ArenaManager arenaManager;

    private final List<DuelRequestProcess> duelRequestProcesses = new ArrayList<>();
    private final List<Duel> ongoingDuels = new ArrayList<>();
    private final List<MoneyBetSettings> moneyBetSettingsCache = new ArrayList<>();

    private final int[] midGlasses = {4, 13, 22, 31, 40};
    private final int[] playerSide = {0, 1, 2, 3, 9, 10, 11, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47};
    private final int[] opponentSide = {5, 6, 7, 8, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35, 41, 42, 43, 44, 51, 52, 53};
    private final int[] playerMoneySide = {0, 1, 2, 3};
    private final int[] opponentMoneySide = {5, 6, 7, 8};

    private ItemStack glass = XMaterial.BLACK_STAINED_GLASS_PANE.parseItem();
    private ItemStack greenGlass = XMaterial.GREEN_STAINED_GLASS_PANE.parseItem();
    private ItemStack redGlass = XMaterial.RED_STAINED_GLASS_PANE.parseItem();
    private ItemStack cancelItem = new ItemStack(Material.BARRIER);

    public DuelManager(BDuels plugin) {
        this.plugin = plugin;
        this.inventoryAPI = plugin.getInventoryAPI();
        this.economy = plugin.getEconomyAdapter();
        reload();
    }

    public void reload() {
        endProcesses();
        endMatches(DuelEndReason.RELOAD);
        prepareMoneyBetItems();
        prepareGuiItems();
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

        ItemMeta greenGlassMeta = glass.getItemMeta();
        greenGlassMeta.setDisplayName(Utils.getString("ready-item.name", null));
        greenGlassMeta.setLore(Utils.getStringList("ready-item.lore", null));

        ItemMeta redGlassMeta = glass.getItemMeta();
        redGlassMeta.setDisplayName(Utils.getString("not-ready-item.name", null));
        redGlassMeta.setLore(Utils.getStringList("not-ready-item.lore", null));

        ItemMeta cancelItemMeta = cancelItem.getItemMeta();
        cancelItemMeta.setDisplayName(Utils.getString("request-cancel-item.name", null));
        cancelItemMeta.setLore(Utils.getStringList("request-cancel-item.lore", null));

        glass.setItemMeta(glassMeta);
        greenGlass.setItemMeta(greenGlassMeta);
        redGlass.setItemMeta(redGlassMeta);
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

    public void setArenaManager(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    public DuelRequestProcess getProcess(User player) {
        for (DuelRequestProcess process : duelRequestProcesses) {
            if (process.getPlayer().equals(player) || process.getOpponent().equals(player)) {
                return process;
            }
        }
        return null;
    }

    public List<Duel> getOngoingDuels() {
        return ongoingDuels;
    }

    public boolean canSendOrAcceptDuel(User user) {
        return user.getRequestProcess() == null && user.getState() == UserState.FREE;
    }

    public void sendDuelRequest(User sender, User opponent) {
        Player senderPlayer = sender.getBase();
        Player opponentPlayer = opponent.getBase();

        if (senderPlayer.equals(opponentPlayer)) return;

        if (!canSendOrAcceptDuel(sender) || !canSendOrAcceptDuel(opponent)) {
            senderPlayer.sendMessage(Utils.getMessage("duel.not-now", senderPlayer)
                    .replace("%opponent%", opponent.getName()));
            return;
        }

        if (!opponent.doesAcceptDuelRequests()) {
            senderPlayer.sendMessage(Utils.getMessage("duel.does-not-accept", senderPlayer)
                    .replace("%opponent%", opponentPlayer.getName()));
            return;
        }

        if (!sender.doesAcceptDuelRequests()) {
            senderPlayer.sendMessage(Utils.getMessage("duel.do-not-accept", senderPlayer)
                    .replace("%opponent%", opponentPlayer.getName()));
            return;
        }

        if (!arenaManager.isAnyArenaAvailable()) {
            senderPlayer.sendMessage(Utils.getMessage("arenas.all-in-usage", senderPlayer));
            return;
        }

        senderPlayer.sendMessage(Utils.getMessage("duel.request-sent", opponentPlayer)
                .replace("%opponent%", opponentPlayer.getName()));
        opponentPlayer.sendMessage(Utils.getMessage("duel.new-request", opponentPlayer)
                .replace("%sender%", senderPlayer.getName()));

        DuelRequestProcess process = new DuelRequestProcess(sender, opponent);
        sender.setRequestProcess(process);
        opponent.setRequestProcess(process);

        duelRequestProcesses.add(process);
    }

    public void acceptDuelRequest(DuelRequestProcess process, List<String> timer) {
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

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
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

        if (oldLore == null) return itemStack;

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
        ItemStack playerSkull = XMaterial.PLAYER_HEAD.parseItem().clone();
        ItemStack opponentSkull = XMaterial.PLAYER_HEAD.parseItem().clone();
        inventory.setItem(12, ClickableItem.empty(replaceLoreAndName(playerSkull, process, null)));
        inventory.setItem(14, ClickableItem.empty(replaceLoreAndName(opponentSkull, process, null)));
        updateMetas(inventory, process);
    }

    public void updateMetas(HInventory inventory, DuelRequestProcess process) {
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
        int index = 0;
        for (int i : side) {
            MoneyBetSettings settings = moneyBetSettingsCache.get(index);
            index++;
            ItemStack item = replaceLoreAndName(settings.getItem(), process, null);
            int moneyToAdd = settings.getMoneyToAdd();
            inventory.setItem(i, ClickableItem.of(item, event -> {
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
        for (User user : requestProcess.getPlayers()) {
            user.setRequestProcess(null);
            user.setState(UserState.FREE);
            Player player = user.getBase();
            HInventory inventory = inventoryAPI.getInventoryManager().getPlayerInventory(player);
            if (inventory != null) inventory.close(player);
            if (canceller != null) player.sendMessage(Utils.getMessage("duel.cancelled", player)
                    .replace("%who%", canceller.getName()));

        }
        if (remove) duelRequestProcesses.remove(requestProcess);
    }

    public void startMatch(DuelRequestProcess requestProcess) {
        cancel(null, requestProcess, true);

        if (!arenaManager.isAnyArenaAvailable()) {
            for (User user : requestProcess.getPlayers()) {
                Player player = user.getBase();
                player.sendMessage(Utils.getMessage("arenas.all-in-usage-2", player));
            }
            return;
        }

        Arena matchArena = arenaManager.findNextEmptyArenaIfPresent();
        matchArena.setState(ArenaState.PRE_MATCH);

        Duel duel = new Duel(plugin, requestProcess, matchArena);
        duel.startCountdown();

        ongoingDuels.add(duel);
        for (User user : duel.getPlayers()) {
            economy.removeMoney(user.getBase(), duel.getRewardsOf(user).getMoneyBet());
        }
    }

    public void endMatch(Duel duel, DuelEndReason reason) {
        User winner = duel.getWinner();
        User loser = duel.getLoser();
        Player winnerPlayer = winner.getBase();
        Player loserPlayer = loser.getBase();

        Arena arena = duel.getArena();
        arena.setState(ArenaState.POST_MATCH);

        boolean isReloadOrStop = reason == DuelEndReason.RELOAD || reason == DuelEndReason.SERVER_STOP;
        if (!isReloadOrStop) ongoingDuels.remove(duel);

        for (User user : duel.getPlayers()) {
            Location preDuelLocation = duel.getPreDuelLocations().get(user);
            Player player = user.getBase();
            player.removeMetadata("god-mode-bduels", plugin); // DuelStartingTask tamamlanmadan maç bittiyse bug olmaması için tekrar siliyoruz
            player.teleport(preDuelLocation);
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

        if (isReloadOrStop) {
            giveItems(winnerItemsPut, winnerPlayer); // ortaya koyduğu eşyaları geri veriyoz çünkü stop veya reload yedi
            giveItems(loserItemsPut, loserPlayer); // / diğerinin eşyalarını da geri veriyoz çünkü stop veya reload yedi

            winnerPlayer.sendMessage(Utils.getMessage("duel.match-force-ended", winnerPlayer));
            loserPlayer.sendMessage(Utils.getMessage("duel.match-force-ended", loserPlayer));

            economy.addMoney(winnerPlayer, winnerMoneyBet);
            economy.addMoney(loserPlayer, loserMoneyBet);
            return;
        }

        MessageType messageType = MessageType.valueOf(Utils.getMessage("duel.win.used-mode"));
        Utils.sendWinMessage(messageType, winnerPlayer, loserPlayer, loserItemsPutSize, loserMoneyBet);

        giveItems(winnerItemsPut, winnerPlayer); // Kazanan kişinin ortaya koyduğu eşyaları geri verir, önce bunu yapıyoruz çünkü adam kendi eşyalarını kaybetmemeli

        winner.addStat(StatisticType.WINS, 1);
        winner.addStat(StatisticType.TOTAL_EARNED_ITEM, loserItemsPutSize);
        winner.addStat(StatisticType.TOTAL_EARNED_MONEY, loserMoneyBet);
        loser.addStat(StatisticType.TOTAL_LOST_ITEM, loserItemsPutSize);
        loser.addStat(StatisticType.TOTAL_LOST_MONEY, loserMoneyBet);
        loser.addStat(StatisticType.LOSES, 1);
        economy.addMoney(winnerPlayer, loserMoneyBet + winnerMoneyBet);

        giveItems(loserItemsPut, winnerPlayer); // Kaybeden kişinin ortaya koyduğu eşyaları kazanana verir
    }

    public void giveItems(List<ItemStack> items, Player player) {
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

    public void endMatches(DuelEndReason reason) {
        for (Duel duel : ongoingDuels) {
            duel.setWinner(duel.getPlayers()[0]);
            duel.getStartingTask().cancel();
            endMatch(duel, reason);
        }
        ongoingDuels.clear();
    }

    public void endProcesses() {
        for (DuelRequestProcess process : duelRequestProcesses) {
            cancel(null, process, false);
        }
        duelRequestProcesses.clear();
    }

    public int[] getOpponentSide() {
        return opponentSide;
    }

    public int[] getPlayerSide() {
        return playerSide;
    }

}
