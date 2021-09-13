package io.github.bilektugrul.bduels;

import com.hakan.inventoryapi.InventoryAPI;
import io.github.bilektugrul.bduels.arenas.ArenaManager;
import io.github.bilektugrul.bduels.commands.DuelCommand;
import io.github.bilektugrul.bduels.commands.base.ArenaCommand;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.economy.EconomyAdapter;
import io.github.bilektugrul.bduels.economy.VaultEconomy;
import io.github.bilektugrul.bduels.language.LanguageManager;
import io.github.bilektugrul.bduels.listeners.HInventoryClickListener;
import io.github.bilektugrul.bduels.listeners.PlayerListener;
import io.github.bilektugrul.bduels.placeholders.CustomPlaceholderManager;
import io.github.bilektugrul.bduels.placeholders.PAPIPlaceholders;
import io.github.bilektugrul.bduels.users.UserManager;
import io.github.bilektugrul.bduels.economy.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

// TODO: ÖDÜL OLAYLARINI, ARENA SİSTEMİNİ HALLET, DÜELLO İSTEĞİ OLAYINI HALLET
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

        arenaManager = new ArenaManager();
        customPlaceholderManager = new CustomPlaceholderManager(this);
        languageManager = new LanguageManager(this);
        userManager = new UserManager(this);
        vaultManager = new VaultManager(this);
        vaultEconomy = new VaultEconomy(this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(p);
        }

        duelManager = new DuelManager(this);
        new PAPIPlaceholders(this).register();
        
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new HInventoryClickListener(this), this);

        getServer().getPluginCommand("duel").setExecutor(new DuelCommand(this));
        getServer().getPluginCommand("arena").setExecutor(new ArenaCommand(this));
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

}
