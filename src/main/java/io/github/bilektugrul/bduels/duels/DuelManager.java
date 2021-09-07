package io.github.bilektugrul.bduels.duels;

import com.hakan.inventoryapi.InventoryAPI;
import com.hakan.inventoryapi.inventory.ClickableItem;
import com.hakan.inventoryapi.inventory.HInventory;
import io.github.bilektugrul.bduels.BDuels;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DuelManager {

    private final InventoryAPI inventoryAPI;
    private final Map<String, String> duelRequests = new HashMap<>();
    private final ArrayList<DuelRequestProcess> duelRequestProcesses = new ArrayList<>();

    private final int[] midGlasses = {13, 22, 31, 40, 49};
    private final ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE);
    private final ItemStack greenGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 13);
    private final ItemStack redGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);

    public DuelManager(BDuels bDuels) {
        this.inventoryAPI = bDuels.getInventoryAPI();
    }

    public String getOpponent(String name) {
        return duelRequests.get(name);
    }

    public String getRequestSender(String name) {
        for (String key : duelRequests.keySet()) {
            if (duelRequests.get(key).equals(name)) {
                return key;
            }
        }
        return null;
    }

    public DuelRequestProcess getProcess(Player player) {
        for (DuelRequestProcess process : duelRequestProcesses) {
            if (process.getPlayer().equals(player) || process.getOpponent().equals(player)) {
                return process;
            }
        }
        return null;
    }

    public boolean canSendOrAcceptDuel(String name) {
        return getOpponent(name) == null && getRequestSender(name) == null;
    }

    public void sendDuelRequest(Player sender, Player opponent) {
        String senderName = sender.getName();
        String opponentName = opponent.getName();

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

        inventory.setItem(48, ClickableItem.of(redGlass, (event -> {
            String clicker = event.getWhoClicked().getName();
            if (clicker.equals(senderName)) {
                boolean newFinished = !process.isFinished(clicker);
                process.setFinished(clicker, newFinished);
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

        inventory.setItem(50, ClickableItem.of(redGlass, (event -> {
            String clicker = event.getWhoClicked().getName();
            if (clicker.equals(opponentName)) {
                boolean newFinished = !process.isFinished(clicker);
                process.setFinished(clicker, newFinished);
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

        inventory.open(sender);
        inventory.open(opponent);
    }

    public void cancel(DuelRequestProcess requestProcess, boolean starting) {
        for (Player p : requestProcess.getPlayers()) {
            inventoryAPI.getInventoryManager().getPlayerInventory(p).close(p);
        }
        duelRequestProcesses.remove(requestProcess);
        if (!starting) {

        }
    }

    public void startMatch(DuelRequestProcess requestProcess) {
        cancel(requestProcess, true);
        Player challenger = requestProcess.getPlayer();
        Player opponent = requestProcess.getOpponent();
    }

}
