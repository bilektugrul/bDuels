package io.github.bilektugrul.bduels.listeners;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.Duel;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserManager;
import io.github.bilektugrul.bduels.users.UserState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private final UserManager userManager;
    private final DuelManager duelManager;

    public PlayerListener(BDuels bDuels) {
        this.userManager = bDuels.getUserManager();
        this.duelManager = bDuels.getDuelManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        userManager.loadUser(e.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        User user = userManager.getUser(e.getEntity().getPlayer());
        if (user.getState() == UserState.IN_MATCH) {
            Duel duel = user.getDuel();
            duelManager.endMatch(duel, user);
        }
    }

}
