package io.github.bilektugrul.bduels.listeners;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.Duel;
import io.github.bilektugrul.bduels.duels.DuelEndReason;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.duels.DuelRequestProcess;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserManager;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private final BDuels plugin;
    private final UserManager userManager;
    private final DuelManager duelManager;

    public PlayerListener(BDuels plugin) {
        this.plugin = plugin;
        this.userManager = plugin.getUserManager();
        this.duelManager = plugin.getDuelManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        player.removeMetadata("god-mode-bduels", plugin);
        userManager.loadUser(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        User user = userManager.getUser(player);
        Duel duel = user.getDuel();
        DuelRequestProcess process = user.getRequestProcess();

        if (duel != null) {
            duel.setWinner(duel.getOpponentOf(user));
            duelManager.endMatch(duel, DuelEndReason.QUIT);
        }

        if (process != null) {
            duelManager.cancel(null, process, true);
            User opponent = process.getOpponentOf(user);
            Player opponentPlayer = opponent.getBase();
            opponentPlayer.sendMessage(Utils.getMessage("duel.left-server", opponentPlayer)
                    .replace("%opponent%", user.getName()));
        }

        userManager.saveStatistics(user, false);
        userManager.removeUser(user);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        User user = userManager.getUser(e.getEntity());
        if (userManager.isInMatch(user)) {
            Duel duel = user.getDuel();
            duel.setWinner(duel.getOpponentOf(user));
            user.setRespawnLocation(duel.getPreDuelLocations().get(user));
            duelManager.endMatch(duel, DuelEndReason.DEATH);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        User user = userManager.getUser(player);
        if (user.getRespawnLocation() != null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.teleport(user.getRespawnLocation());
                user.setRespawnLocation(null);
            });
        }
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
        if (userManager.isInMatch(user)) {
            e.setCancelled(Utils.shouldCancelEvent("can-place-blocks", player));
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        User user = userManager.getUser(player);
        if (userManager.isInMatch(user)) {
            e.setCancelled(Utils.shouldCancelEvent("can-break-blocks", player));
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (!(e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            return;
        }

        Player player = e.getPlayer();
        User user = userManager.getUser(player);
        if (userManager.isInMatch(user)) {
            e.setCancelled(Utils.shouldCancelEvent("can-teleport", player));
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        User user = userManager.getUser(player);
        if (userManager.isInMatch(user)) {
            e.setCancelled(Utils.shouldCancelEvent("can-use-command", player));
        }
    }

}