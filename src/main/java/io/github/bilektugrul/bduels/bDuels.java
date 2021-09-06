package io.github.bilektugrul.bduels;

import io.github.bilektugrul.bduels.arenas.ArenaManager;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.listeners.PlayerListener;
import io.github.bilektugrul.bduels.users.UserManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class bDuels extends JavaPlugin {

    private UserManager userManager;
    private ArenaManager arenaManager;
    private DuelManager duelManager;

    @Override
    public void onEnable() {
        userManager = new UserManager(this);
        arenaManager = new ArenaManager();
        duelManager = new DuelManager();
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {

    }

    public UserManager getUserManager() {
        return userManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }

}
