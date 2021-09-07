package io.github.bilektugrul.bduels.duels;

import com.hakan.inventoryapi.InventoryAPI;
import com.hakan.inventoryapi.inventory.ClickableItem;
import com.hakan.inventoryapi.inventory.HInventory;
import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.arenas.ArenaManager;
import io.github.bilektugrul.bduels.arenas.ArenaState;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DuelManager {

    private final InventoryAPI inventoryAPI;
    private final ArenaManager arenaManager;

    private final Map<User, User> duelRequests = new HashMap<>();
    private final ArrayList<DuelRequestProcess> duelRequestProcesses = new ArrayList<>();
    private final ArrayList<Duel> ongoingDuels = new ArrayList<>();

    private final int[] midGlasses = {13, 22, 31, 40, 49};
    private final ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE);
    private final ItemStack greenGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 13);
    private final ItemStack redGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);

    public DuelManager(BDuels bDuels) {
        this.inventoryAPI = bDuels.getInventoryAPI();
        this.arenaManager = bDuels.getArenaManager();
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

    public ArrayList<Duel> getOngoingDuels() {
        return ongoingDuels;
    }

    public boolean canSendOrAcceptDuel(User user) {
        return getOpponent(user) == null && getRequestSender(user) == null
                && user.getState() == UserState.FREE;
    }

    public void sendDuelRequest(User sender, User opponent) {
        Player senderPlayer = sender.getPlayer();
        Player opponentPlayer = opponent.getPlayer();
        if (!canSendOrAcceptDuel(sender) || !canSendOrAcceptDuel(opponent)) {
            senderPlayer.sendMessage("Şu an duel atamazsın veya bu oyuncu şu an duel kabul edemez.");
        } else if (arenaManager.isAnyArenaAvailable()) {

            String senderName = senderPlayer.getName();
            String opponentName = opponentPlayer.getName();
            duelRequests.put(sender, opponent);

            DuelRequestProcess process = new DuelRequestProcess(sender, opponent);
            duelRequestProcesses.add(process);

            HInventory inventory = inventoryAPI.getInventoryCreator().setTitle("Duel - " + senderName + " vs " + opponentName)
                    .setClosable(false)
                    .setId(senderName + "-duel")
                    .create();

            for (int i : midGlasses) {
                inventory.setItem(i, ClickableItem.empty(glass));
            }

            inventory.setItem(4, ClickableItem.of(new ItemStack(Material.BARRIER), (event -> {
                cancel(process, false);
            })));

            putAcceptItem(inventory, 48, sender, process);
            putAcceptItem(inventory, 50, opponent, process);

            inventory.open(senderPlayer);
            inventory.open(opponentPlayer);
        } else {
            senderPlayer.sendMessage("Hiç müsait arena yok.");
        }
    }

    public void putAcceptItem(HInventory inventory, int slot, User user, DuelRequestProcess process) {
        inventory.setItem(slot, ClickableItem.of(redGlass, (event -> {
            Player clicker = (Player) event.getWhoClicked();
            if (clicker.equals(user.getPlayer())) {
                boolean newFinished = !process.isFinished(user);
                process.setFinished(user, newFinished);
                if (newFinished) {
                    event.setCurrentItem(greenGlass);
                    if (process.isBothFinished()) {
                        startMatch(process);
                    }
                } else {
                    event.setCurrentItem(redGlass);
                }
            }
        })));
    }

    public void cancel(DuelRequestProcess requestProcess, boolean starting) {
        for (User user : requestProcess.getPlayers()) {
            Player player = user.getPlayer();
            player.closeInventory();
        }
        duelRequestProcesses.remove(requestProcess);
        duelRequests.remove(requestProcess.getPlayer());
        if (!starting) {
            // TODO: İLERDE DUEL CANCEL MESAJI FALAN BURAYA KNK
        }
    }

    public void startMatch(DuelRequestProcess requestProcess) {
        cancel(requestProcess, true);

        Arena matchArena = arenaManager.findNextEmptyArenaIfPresent();
        matchArena.setState(ArenaState.PRE_MATCH);

        Duel duel = new Duel(requestProcess, matchArena);
        ongoingDuels.add(duel);
        duel.start();
    }

    public void endMatch(Duel duel, User loser) {
        User winner = duel.getOpponentOf(loser);
        Arena arena = duel.getArena();
        arena.setState(ArenaState.POST_MATCH);
        ongoingDuels.remove(duel);
        for (User user : duel.getPlayers()) {
            PreDuelData duelData = duel.getPreDuelData().get(user);
            Player player = user.getPlayer();
            player.teleport(duelData.getLocation());
        }
    }

}
