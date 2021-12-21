package io.github.bilektugrul.bduels.listeners;

import com.hakan.inventoryapi.inventory.HInventory;
import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.DuelRequestProcess;
import io.github.bilektugrul.bduels.duels.DuelRewards;
import io.github.bilektugrul.bduels.stuff.PlayerType;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class HInventoryClickListener extends ListenerAdapter {

    public HInventoryClickListener(BDuels plugin) {
        super(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player clicker = (Player) e.getWhoClicked();
        HInventory playerHInventory = plugin.getInventoryAPI().getInventoryManager().getPlayerInventory(clicker);
        if (playerHInventory != null && playerHInventory.getId().contains("-bDuels")) {
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null) {
                return;
            }

            Inventory clickedInventory = e.getClickedInventory();
            if (clickedInventory == null) {
                return;
            }

            User user = userManager.getOrLoadUser(clicker);
            DuelRequestProcess process = user.getRequestProcess();
            if (process.isFinished(user)) {
                return;
            }

            PlayerType clickerType = process.getPlayerType(user);
            int[] side = clickerType == PlayerType.PLAYER ? duelManager.getPlayerSide() : duelManager.getOpponentSide();
            DuelRewards rewards = process.getRewardsOf(user);

            Inventory playerHInventoryOriginal = playerHInventory.getInventory();

            if (!clickedInventory.equals(playerHInventoryOriginal)) { // KENDİ ENVANTERİNE TIKLADIĞINDA TIKLADIĞI EŞYA BET OLAN İTEMLERE EKLENİCEK
                int slotToPut = Utils.nextEmptySlot(side, playerHInventoryOriginal);
                if (rewards.containsItem(clicked) || slotToPut == -1) {
                    return;
                }

                rewards.addItemToBet(clicked);
                playerHInventoryOriginal.setItem(slotToPut, clicked);
            } else { // GUIYE TIKLADIĞINDA TIKLADIĞI İTEM BETLENEN İTEMLERDENSE SİLİNECEK
                if (!rewards.containsItem(clicked)) {
                    return;
                }

                rewards.removeItem(clicked);
                clickedInventory.setItem(e.getSlot(), null);
            }
        }
    }

}