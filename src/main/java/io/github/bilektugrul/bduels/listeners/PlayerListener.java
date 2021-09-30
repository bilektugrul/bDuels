package io.github.bilektugrul.bduels.listeners;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.Duel;
import io.github.bilektugrul.bduels.duels.DuelEndReason;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserManager;
import io.github.bilektugrul.bduels.users.UserState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {

    private final BDuels bDuels;
    private final UserManager userManager;
    private final DuelManager duelManager;

    public PlayerListener(BDuels bDuels) {
        this.bDuels = bDuels;
        this.userManager = bDuels.getUserManager();
        this.duelManager = bDuels.getDuelManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        userManager.loadUser(e.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        User user = userManager.getUser(e.getEntity());
        if (user.getState() == UserState.IN_MATCH) {
            Duel duel = user.getDuel();
            duel.setLoser(user);
            duel.setWinner(duel.getOpponentOf(user));
            user.setRespawnLocation(duel.getPreDuelData().get(user).getLocation());
            duelManager.endMatch(duel, DuelEndReason.DEATH);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        User user = userManager.getUser(player);
        if (user.getRespawnLocation() != null) {
            Bukkit.getScheduler().runTask(bDuels, () -> {
                player.teleport(user.getRespawnLocation());
                user.setRespawnLocation(null);
            });
        }
    }

}
