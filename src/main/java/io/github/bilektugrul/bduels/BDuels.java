package io.github.bilektugrul.bduels;

import com.hakan.inventoryapi.InventoryAPI;
import io.github.bilektugrul.bduels.arenas.ArenaManager;
import io.github.bilektugrul.bduels.commands.BDuelsCommand;
import io.github.bilektugrul.bduels.commands.DuelAcceptCommand;
import io.github.bilektugrul.bduels.commands.DuelCommand;
import io.github.bilektugrul.bduels.commands.arena.base.ArenaCommand;
import io.github.bilektugrul.bduels.duels.Duel;
import io.github.bilektugrul.bduels.duels.DuelEndReason;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.economy.EconomyAdapter;
import io.github.bilektugrul.bduels.economy.VaultEconomy;
import io.github.bilektugrul.bduels.economy.VaultManager;
import io.github.bilektugrul.bduels.language.LanguageManager;
import io.github.bilektugrul.bduels.listeners.HInventoryClickListener;
import io.github.bilektugrul.bduels.listeners.PlayerListener;
import io.github.bilektugrul.bduels.placeholders.CustomPlaceholderManager;
import io.github.bilektugrul.bduels.placeholders.PAPIPlaceholders;
import io.github.bilektugrul.bduels.users.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

// TODO: ISTATISTIK SISTEMI EKLESEN GÜZEL OLUR
public final class BDuels extends JavaPlugin {

    private CustomPlaceholderManager customPlaceholderManager;
    private LanguageManager languageManager;
    private UserManager userManager;
    private ArenaManager arenaManager;
    private VaultManager vaultManager;
    private EconomyAdapter economyAdapter;
    private VaultEconomy vaultEconomy;
    private DuelManager duelManager;

    private InventoryAPI inventoryAPI;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        inventoryAPI = InventoryAPI.getInstance(this);

        vaultManager = new VaultManager(this);
        vaultEconomy = new VaultEconomy(this);
        customPlaceholderManager = new CustomPlaceholderManager(this);
        languageManager = new LanguageManager(this);
        duelManager = new DuelManager(this);
        arenaManager = new ArenaManager(this);
        duelManager.setArenaManager(arenaManager);
        arenaManager.setDuelManager(duelManager);
        userManager = new UserManager(this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(p);
        }

        new PAPIPlaceholders(this).register();
        
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new HInventoryClickListener(this), this);

        getServer().getPluginCommand("bduels").setExecutor(new BDuelsCommand(this));
        getServer().getPluginCommand("duel").setExecutor(new DuelCommand(this));
        getServer().getPluginCommand("accept").setExecutor(new DuelAcceptCommand(this));
        getServer().getPluginCommand("arena").setExecutor(new ArenaCommand(this));
    }

    @Override
    public void onDisable() {
        for (Duel duel : duelManager.getOngoingDuels()) {
            duel.setWinner(duel.getPlayers()[0]);
            duelManager.endMatch(duel, DuelEndReason.SERVER_STOP);
        }
        Bukkit.getScheduler().cancelTasks(this);
        save();
    }

    public CustomPlaceholderManager getPlaceholderManager() {
        return customPlaceholderManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public VaultEconomy getVaultEconomy() {
        return vaultEconomy;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }

    public InventoryAPI getInventoryAPI() {
        return inventoryAPI;
    }

    public EconomyAdapter getEconomyAdapter() {
        return economyAdapter;
    }

    public void setEconomyAdapter(EconomyAdapter economyAdapter) {
        this.economyAdapter = economyAdapter;
    }

    public void reload() {
        reloadConfig();
        arenaManager.loadArenas();
        duelManager.reload();
        languageManager.loadLanguage();
    }

    public void save() {
        arenaManager.save();
    }

}
