package io.github.bilektugrul.bduels.listeners;

import com.hakan.inventoryapi.InventoryAPI;
import com.hakan.inventoryapi.inventory.HInventory;
import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.duels.DuelRequestProcess;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class HInventoryClickListener implements Listener {

    private final DuelManager duelManager;
    private final InventoryAPI inventoryAPI;
    private final UserManager userManager;

    public HInventoryClickListener(BDuels bDuels) {
        this.duelManager = bDuels.getDuelManager();
        this.inventoryAPI = bDuels.getInventoryAPI();
        this.userManager = bDuels.getUserManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        User user = userManager.getUser(clicker);
        DuelRequestProcess process = duelManager.getProcess(user);
        HInventory playerHInventory = inventoryAPI.getInventoryManager().getPlayerInventory(clicker);
        if (process != null && playerHInventory != null) {
            if (!e.getClickedInventory().equals(playerHInventory.getInventory())) { // KENDİ ENVANTERİNE TIKLADIĞINDA TIKLADIĞI EŞYA BET OLAN İTEMLERE EKLENİCEK

            } else { // GUIYE TIKLADIĞINDA TIKLADIĞI İTEM BETLENEN İTEMLERDENSE SİLİNECEK

            }
        }
    }

}
