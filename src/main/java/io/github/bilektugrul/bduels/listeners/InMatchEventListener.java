package io.github.bilektugrul.bduels.listeners;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.Duel;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class InMatchEventListener extends ListenerAdapter {

    public InMatchEventListener(BDuels plugin) {
        super(plugin);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof Player)) return;

        if (entity.hasMetadata("god-mode-bduels")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        User user = userManager.getUser(player);
        if (user.isInMatch()) {
            e.setCancelled(Utils.shouldCancelEvent("can-place-blocks", player));
            if (!e.isCancelled()) {
                Duel duel = user.getDuel();
                duel.addPlacedBlockLocation(e.getBlock().getLocation());
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        User user = userManager.getUser(player);
        if (user.isInMatch()) {
            e.setCancelled(Utils.shouldCancelEvent("can-break-blocks", player));
            if (!e.isCancelled()) {
                Duel duel = user.getDuel();
                duel.removePlacedBlockLocation(e.getBlock().getLocation());
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        User user = userManager.getUser(player);
        if (user.isInMatch()) {
            e.setCancelled(Utils.shouldCancelEvent("can-drop-item", player));
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (!(e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            return;
        }

        Player player = e.getPlayer();
        User user = userManager.getUser(player);
        if (user.isInMatch()) {
            e.setCancelled(Utils.shouldCancelEvent("can-teleport", player));
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        User user = userManager.getUser(player);
        if (user.isInMatch()) {
            e.setCancelled(Utils.shouldCancelEvent("can-use-command", player));
        }
    }

}