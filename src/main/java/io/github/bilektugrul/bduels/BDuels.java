package io.github.bilektugrul.bduels;

import com.hakan.inventoryapi.InventoryAPI;
import io.github.bilektugrul.bduels.arenas.ArenaManager;
import io.github.bilektugrul.bduels.commands.BDuelsCommand;
import io.github.bilektugrul.bduels.commands.LeaderboardCommand;
import io.github.bilektugrul.bduels.commands.arena.base.ArenaCommand;
import io.github.bilektugrul.bduels.commands.duel.DuelAcceptCommand;
import io.github.bilektugrul.bduels.commands.duel.DuelCommand;
import io.github.bilektugrul.bduels.commands.duel.DuelStatsCommand;
import io.github.bilektugrul.bduels.commands.duel.ToggleDuelRequestsCommand;
import io.github.bilektugrul.bduels.duels.DuelEndReason;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.economy.EconomyAdapter;
import io.github.bilektugrul.bduels.economy.VaultEconomy;
import io.github.bilektugrul.bduels.economy.VaultManager;
import io.github.bilektugrul.bduels.features.language.LanguageManager;
import io.github.bilektugrul.bduels.features.leaderboards.LeaderboardManager;
import io.github.bilektugrul.bduels.features.placeholders.CustomPlaceholderManager;
import io.github.bilektugrul.bduels.features.placeholders.PAPIPlaceholders;
import io.github.bilektugrul.bduels.listeners.HInventoryClickListener;
import io.github.bilektugrul.bduels.listeners.InMatchEventListener;
import io.github.bilektugrul.bduels.listeners.PlayerListener;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserManager;
import io.github.bilektugrul.bduels.users.data.MySQLManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class BDuels extends JavaPlugin {

    private CustomPlaceholderManager customPlaceholderManager;
    private LanguageManager languageManager;
    private UserManager userManager;
    private ArenaManager arenaManager;
    private VaultManager vaultManager;
    private EconomyAdapter economyAdapter;
    private VaultEconomy vaultEconomy;
    private DuelManager duelManager;
    private MysqlDatabase mysqlDatabase;
    private LeaderboardManager leaderboardManager;

    private InventoryAPI inventoryAPI;

    private boolean databaseEnabled = false;
    private boolean hologramsEnabled = false;
    private boolean forceDisable = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        inventoryAPI = InventoryAPI.getInstance(this);

        PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.isPluginEnabled("Vault")) {
            vaultManager = new VaultManager(this);
            vaultEconomy = new VaultEconomy(this);
        } else {
            getLogger().warning("Sunucunuzda Vault kurulu değil, BDuels'in çalışması için Vault gereklidir.");
            setEnabled(false);
        }

        databaseEnabled = getConfig().getBoolean("database.enabled");
        if (databaseEnabled) {
            mysqlDatabase = new MysqlDatabase(getLogger(), getConfig());
            if (mysqlDatabase.shouldClose()) {
                forceDisable = true;
                setEnabled(false);
                return;
            }
        }

        customPlaceholderManager = new CustomPlaceholderManager(this);
        languageManager = new LanguageManager(this);
        arenaManager = new ArenaManager(this);
        duelManager = new DuelManager(this);
        userManager = new UserManager(this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(p);
        }

        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            new PAPIPlaceholders(this).register();
        } else {
            getLogger().warning("Sunucunuzda PlaceholderAPI kurulu değil, ona bağlı özellikleri kullanamayacaksınız.");
        }

        hologramsEnabled = pluginManager.isPluginEnabled("HolographicDisplays");
        if (!hologramsEnabled) {
            getLogger().warning("HolographicDisplays bulunamadı, sıralama hologramı çalışmayacak.");
        }

        if (databaseEnabled) {
            leaderboardManager = new LeaderboardManager(this);
        }

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new HInventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new InMatchEventListener(this), this);

        getCommand("accept").setExecutor(new DuelAcceptCommand(this));
        getCommand("arena").setExecutor(new ArenaCommand());
        getCommand("bduels").setExecutor(new BDuelsCommand(this));
        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("duelstats").setExecutor(new DuelStatsCommand(this));
        getCommand("toggleduel").setExecutor(new ToggleDuelRequestsCommand(this));
        if (isLeaderboardManagerReady()) getCommand("leaderboard").setExecutor(new LeaderboardCommand(this));
    }

    @Override
    public void onDisable() {
        if (!forceDisable) {
            duelManager.endMatches(DuelEndReason.SERVER_STOP);
            if (databaseEnabled) {
                saveAllUserStatistics();
                mysqlDatabase.shutdownConnPool();
            }
            Bukkit.getScheduler().cancelTasks(this);
            save();
        }
    }

    public boolean saveAllUserStatistics() {
        MySQLManager mySQLManager = userManager.getMysqlManager();
        if (mySQLManager == null || !databaseEnabled) {
            return false;
        }

        for (User user : userManager.getUserList()) {
            mySQLManager.saveAllStatistic(user, true);
        }
        return true;
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

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public boolean isLeaderboardManagerReady() {
        return leaderboardManager != null;
    }

    public boolean isHologramsEnabled() {
        return hologramsEnabled;
    }

    public boolean isDatabaseEnabled() {
        return databaseEnabled;
    }

    public MysqlDatabase getMySQLDatabase() {
        return mysqlDatabase;
    }

    public void reload() {
        reloadConfig();
        languageManager.loadLanguage();
        arenaManager.loadArenas();
        duelManager.reload();
        userManager.prepareSaveProcess();
        if (isLeaderboardManagerReady()) leaderboardManager.reloadSettings(false);
    }

    public void save() {
        arenaManager.save();
        if (isLeaderboardManagerReady()) leaderboardManager.save();
    }

}