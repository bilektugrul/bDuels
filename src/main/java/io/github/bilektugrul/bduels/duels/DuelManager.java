package io.github.bilektugrul.bduels.duels;

import com.hakan.inventoryapi.InventoryAPI;
import com.hakan.inventoryapi.inventory.ClickableItem;
import com.hakan.inventoryapi.inventory.HInventory;
import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.arenas.ArenaManager;
import io.github.bilektugrul.bduels.arenas.ArenaState;
import io.github.bilektugrul.bduels.economy.VaultEconomy;
import io.github.bilektugrul.bduels.stuff.MessageType;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserState;
import io.github.bilektugrul.bduels.utils.Utils;
import me.despical.commons.compat.XMaterial;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuelManager {

    private final BDuels plugin;
    private final InventoryAPI inventoryAPI;
    private final VaultEconomy vaultEconomy;
    private ArenaManager arenaManager;

    private final Map<User, User> duelRequests = new HashMap<>();
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
        this.vaultEconomy = plugin.getVaultEconomy();
        reload();
    }

    public void reload() {
        prepareMoneyBetItems();
        prepareGuiItems();
    }

    public void prepareGuiItems() {
        FileConfiguration config = plugin.getConfig();
        glass = XMaterial.valueOf(config.getString("mid-item.material")).parseItem();
        greenGlass = XMaterial.valueOf(config.getString("ready-item.material")).parseItem();
        redGlass = XMaterial.valueOf(config.getString("not-ready-item.material")).parseItem();
        cancelItem = XMaterial.valueOf(config.getString("request-cancel-item.material")).parseItem();

        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(Utils.getString("mid-item.name", null));

        ItemMeta greenGlassMeta = glass.getItemMeta();
        greenGlassMeta.setDisplayName(Utils.getString("ready-item.name", null));

        ItemMeta redGlassMeta = glass.getItemMeta();
        redGlassMeta.setDisplayName(Utils.getString("not-ready-item.name", null));

        ItemMeta cancelItemMeta = cancelItem.getItemMeta();
        cancelItemMeta.setDisplayName(Utils.getString("request-cancel-item.name", null));

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

            ItemStack item = XMaterial.valueOf(config.getString(path + "item")).parseItem();
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

    public User getOpponent(User user) {
        return duelRequests.get(user);
    }

    public User getRequestSender(User user) {
        for (User user2 : duelRequests.keySet()) {
            if (duelRequests.get(user2).equals(user)) {
                return user2;
            }
        }
        return null;
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
        return getOpponent(user) == null && getRequestSender(user) == null
                && user.getState() == UserState.FREE;
    }

    public void sendDuelRequest(User sender, User opponent) {
        Player senderPlayer = sender.getBase();
        Player opponentPlayer = opponent.getBase();

        if (senderPlayer.equals(opponentPlayer)) return;

        if (!canSendOrAcceptDuel(sender) || !canSendOrAcceptDuel(opponent)) {
            senderPlayer.sendMessage(Utils.getMessage("duel.not-now", senderPlayer)
                    .replace("%rakip%", opponent.getName()));
            return;
        }

        if (arenaManager.isAnyArenaAvailable()) {

            String senderName = senderPlayer.getName();
            String opponentName = opponentPlayer.getName();
            duelRequests.put(sender, opponent);

            DuelRequestProcess process = new DuelRequestProcess(sender, opponent);
            sender.setRequestProcess(process);
            opponent.setRequestProcess(process);
            duelRequestProcesses.add(process);

            HInventory inventory = inventoryAPI.getInventoryCreator()
                    .setTitle(Utils.getString("request-gui-name", sender.getBase())
                            .replace("%opponent%", opponentName))
                    .setClosable(false)
                    .setId(senderName + "-bDuels")
                    .create();

            inventory.guiAir();

            for (int i : midGlasses) {
                inventory.setItem(i, ClickableItem.empty(glass));
            }


            inventory.setItem(49, ClickableItem.of(cancelItem, event -> cancel(process)));

            putAcceptItem(inventory, 48, sender, process);
            putAcceptItem(inventory, 50, opponent, process);
            putMoneyBetItems(inventory, playerMoneySide, sender, process);
            putMoneyBetItems(inventory, opponentMoneySide, opponent, process);
            updateHeads(inventory, process);

            inventory.open(senderPlayer);
            inventory.open(opponentPlayer);
        } else {
            senderPlayer.sendMessage(Utils.getMessage("arenas.all-in-usage", senderPlayer));
        }
    }

    public void updateHeads(HInventory inventory, DuelRequestProcess process) {
        ItemStack playerSkull = XMaterial.PLAYER_HEAD.parseItem().clone();
        ItemStack opponentSkull = XMaterial.PLAYER_HEAD.parseItem().clone();
        inventory.setItem(12, ClickableItem.empty(playerSkull));
        inventory.setItem(14, ClickableItem.empty(opponentSkull));
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
            ItemStack item = settings.getItem();
            int moneyToAdd = settings.getMoneyToAdd();
            inventory.setItem(i, ClickableItem.of(item, event -> {
                Player clicker = (Player) event.getWhoClicked();
                DuelRewards rewards = process.getDuelRewards().get(user);
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
        inventory.setItem(slot, ClickableItem.of(redGlass, event -> {
            Player clicker = (Player) event.getWhoClicked();
            if (clicker.equals(user.getBase())) {
                boolean newFinished = !process.isFinished(user);
                process.setFinished(user, newFinished);
                if (newFinished) {
                    event.setCurrentItem(greenGlass);
                    if (process.isBothFinished()) {
                        inventory.setClosable(true);
                        startMatch(process);
                    }
                } else {
                    event.setCurrentItem(redGlass);
                }
            }
        }));
    }

    public void cancel(DuelRequestProcess requestProcess) {
        for (User user : requestProcess.getPlayers()) {
            user.setRequestProcess(null);
            Player player = user.getBase();
            HInventory inventory = inventoryAPI.getInventoryManager().getPlayerInventory(player);
            if (inventory != null) inventory.close(player);
        }
        duelRequestProcesses.remove(requestProcess);
        duelRequests.remove(requestProcess.getPlayer());
    }

    public void startMatch(DuelRequestProcess requestProcess) {
        cancel(requestProcess);

        if (!arenaManager.isAnyArenaAvailable()) {
            for (User user : requestProcess.getPlayers()) {
                Player player = user.getBase();
                player.sendMessage(Utils.getMessage("arenas.all-in-usage-2", player));
            }
            return;
        }

        Arena matchArena = arenaManager.findNextEmptyArenaIfPresent();
        matchArena.setState(ArenaState.PRE_MATCH);

        Duel duel = new Duel(requestProcess, matchArena);
        DuelStartingTask startingTask = new DuelStartingTask(plugin, duel);
        startingTask.runTaskTimer(plugin, 0, 20L);

        ongoingDuels.add(duel);
        for (User user : duel.getPlayers()) {
            vaultEconomy.removeMoney(user.getBase(), duel.getDuelRewards().get(user).getMoneyBet());
        }
    }

    public void endMatch(Duel duel, DuelEndReason duelEndReason) {
        User winner = duel.getWinner();
        User loser = duel.getLoser();

        Arena arena = duel.getArena();
        arena.setState(ArenaState.POST_MATCH);
        ongoingDuels.remove(duel);
        for (User user : duel.getPlayers()) {
            PreDuelData preDuelData = duel.getPreDuelData().get(user);
            Player player = user.getBase();
            player.removeMetadata("god-mode-bduels", plugin); // DuelStartingTask tamamlanmadan maç bittiyse bug olmaması için tekrar siliyoruz
            player.teleport(preDuelData.getLocation());
            user.setState(UserState.FREE);
            user.setDuel(null);
        }

        arena.setState(ArenaState.EMPTY);

        Player winnerPlayer = winner.getBase();
        Player loserPlayer = loser.getBase();

        DuelRewards loserRewards = duel.getRewardsOf(loser);
        DuelRewards winnerRewards = duel.getRewardsOf(winner);

        Inventory winnerInventory = winnerPlayer.getInventory();
        Inventory loserInventory = loserPlayer.getInventory();

        World winnerWorld = winnerPlayer.getWorld();
        World loserWorld = loserPlayer.getWorld();

        Location winnerLocation = winnerPlayer.getLocation();
        Location loserLocation = loserPlayer.getLocation();

        if (duelEndReason == DuelEndReason.RELOAD || duelEndReason == DuelEndReason.SERVER_STOP) {
            for (ItemStack item : winnerRewards.getItemsBet()) { // ortaya koyduğu eşyaları geri veriyoz çünkü stoğ veya reload yedi
                if (Utils.hasSpace(winnerInventory, item)) {
                    winnerInventory.addItem(item);
                } else {
                    winnerWorld.dropItem(winnerLocation, item);
                }
            }

            for (ItemStack item : loserRewards.getItemsBet()) { // Diğerinin eşyalarını da geri veriyoz çünkü reload veya stop yedi
                if (Utils.hasSpace(loserInventory, item)) {
                    loserInventory.addItem(item);
                } else {
                    loserWorld.dropItem(loserLocation, item);
                }
            }

            winnerPlayer.sendMessage(Utils.getMessage("duel.match-force-ended", winnerPlayer));
            loserPlayer.sendMessage(Utils.getMessage("duel.match-force-ended", loserPlayer));

            vaultEconomy.addMoney(winnerPlayer, winnerRewards.getMoneyBet());
            vaultEconomy.addMoney(loserPlayer, loserRewards.getMoneyBet());
            return;
        }

        MessageType messageType = MessageType.valueOf(Utils.getMessage("duel.win.used-mode"));
        Utils.sendWinMessage(messageType, winnerPlayer, loserPlayer, loserRewards.getItemsBet().size(), loserRewards.getMoneyBet());

        for (ItemStack item : winnerRewards.getItemsBet()) { // Kazanan kişinin ortaya koyduğu eşyaları geri verir, önce bunu yapıyoruz çünkü adam kendi eşyalarını kaybetmemeli.
            if (Utils.hasSpace(winnerInventory, item)) {
                winnerInventory.addItem(item);
            } else {
                winnerWorld.dropItem(winnerLocation, item);
            }
        }

        vaultEconomy.addMoney(winnerPlayer, loserRewards.getMoneyBet() + winnerRewards.getMoneyBet());
        for (ItemStack item : loserRewards.getItemsBet()) { // Kaybeden kişinin ortaya koyduğu eşyaları kazanana verir
            if (Utils.hasSpace(winnerInventory, item)) {
                winnerInventory.addItem(item);
            } else {
                winnerWorld.dropItem(winnerLocation, item);
            }
        }
    }

    public Map<User, User> getDuelRequests() {
        return duelRequests;
    }

    public int[] getOpponentSide() {
        return opponentSide;
    }

    public int[] getPlayerSide() {
        return playerSide;
    }

}
