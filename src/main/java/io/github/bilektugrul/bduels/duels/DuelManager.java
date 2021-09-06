package io.github.bilektugrul.bduels.duels;

import com.hakan.inventoryapi.InventoryAPI;
import com.hakan.inventoryapi.inventory.ClickableItem;
import com.hakan.inventoryapi.inventory.HInventory;
import io.github.bilektugrul.bduels.BDuels;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DuelManager {

    private final InventoryAPI inventoryAPI;
    private final Map<String, String> duelRequests = new HashMap<>();
    private final int[] midGlasses = {4, 13, 22, 31, 40, 49};
    private final ClickableItem midGlass = ClickableItem.empty(new ItemStack(Material.STAINED_GLASS_PANE));

    public DuelManager(BDuels bDuels) {
        this.inventoryAPI = bDuels.getInventoryAPI();
    }

    public String getOpponent(String name) {
        return duelRequests.get(name);
    }

    public String getRequestSender(String name) {
        for (String key : duelRequests.values()) {
            if (duelRequests.get(key).equals(name)) {
                return key;
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
        DuelRewards senderRewards = new DuelRewards();
        DuelRewards opponentRewards = new DuelRewards();
        HInventory inventory = inventoryAPI.getInventoryCreator().setTitle("Duel - " + senderName + " vs " + opponentName)
                .setClosable(false)
                .setId(senderName + "-duel")
                .create();
        for (int i : midGlasses) {
            inventory.setItem(i, midGlass);
        }
    }

}
