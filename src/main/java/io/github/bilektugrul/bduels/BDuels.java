package io.github.bilektugrul.bduels;

import com.hakan.inventoryapi.InventoryAPI;
import io.github.bilektugrul.bduels.arenas.ArenaManager;
import io.github.bilektugrul.bduels.commands.BDuelsCommand;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.listeners.HInventoryClickListener;
import io.github.bilektugrul.bduels.listeners.PlayerListener;
import io.github.bilektugrul.bduels.users.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class BDuels extends JavaPlugin {

    private UserManager userManager;
    private ArenaManager arenaManager;
    private DuelManager duelManager;

    private InventoryAPI inventoryAPI;

    @Override
    public void onEnable() {
        inventoryAPI = InventoryAPI.getInstance(this);

        arenaManager = new ArenaManager();
        userManager = new UserManager(this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(p);
        }

        duelManager = new DuelManager(this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new HInventoryClickListener(this), this);

        getServer().getPluginCommand("duel").setExecutor(new BDuelsCommand(this));
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

    public InventoryAPI getInventoryAPI() {
        return inventoryAPI;
    }

}
