package io.github.bilektugrul.bduels.listeners;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.users.UserManager;
import org.bukkit.event.Listener;

public class ListenerAdapter implements Listener {

    protected final BDuels plugin;
    protected final UserManager userManager;
    protected final DuelManager duelManager;

    public ListenerAdapter(BDuels plugin) {
        this.plugin = plugin;
        this.userManager = plugin.getUserManager();
        this.duelManager = plugin.getDuelManager();
    }

}