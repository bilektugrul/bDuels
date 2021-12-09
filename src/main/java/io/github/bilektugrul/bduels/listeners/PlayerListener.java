package io.github.bilektugrul.bduels.listeners;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.Duel;
import io.github.bilektugrul.bduels.duels.DuelEndReason;
import io.github.bilektugrul.bduels.duels.DuelRequestProcess;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener extends ListenerAdapter {

    public PlayerListener(BDuels plugin) {
        super(plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        player.removeMetadata("god-mode-bduels", plugin);
        userManager.getUser(player);
        if (Utils.getBoolean("teleport-to-spawn-on-join.enabled")) {
            if (player.getWorld().getName().equalsIgnoreCase(Utils.getString("teleport-to-spawn-on-join.duel-world"))) {
                World spawnWorld = Bukkit.getWorld(Utils.getString("teleport-to-spawn-on-join.spawn-world"));
                Bukkit.getScheduler().runTask(plugin, () -> player.teleport(spawnWorld.getSpawnLocation()));
            }
        }
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
        if (user.isInMatch()) {
            e.setKeepInventory(true);
            Duel duel = user.getDuel();
            duel.setWinner(duel.getOpponentOf(user));
            Bukkit.getScheduler().runTask(plugin, () -> {
                user.getBase().spigot().respawn();
                user.getBase().teleport(duel.getPreDuelLocations().get(user));
            });
            duelManager.endMatch(duel, DuelEndReason.DEATH);
        }
    }

}