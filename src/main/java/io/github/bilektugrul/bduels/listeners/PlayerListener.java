package io.github.bilektugrul.bduels.listeners;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.users.UserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private final UserManager userManager;

    public PlayerListener(BDuels bDuels) {
        this.userManager = bDuels.getUserManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        userManager.loadUser(e.getPlayer());
    }

}
