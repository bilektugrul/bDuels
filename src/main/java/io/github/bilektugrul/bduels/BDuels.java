package io.github.bilektugrul.bduels;

import com.hakan.controller.LicenseController;
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
import io.github.bilektugrul.bduels.economy.EmptyEconomy;
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
import io.github.bilektugrul.bduels.users.data.DatabaseType;
import io.github.bilektugrul.bduels.users.data.MySQLManager;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public final class BDuels extends JavaPlugin {

    private CustomPlaceholderManager customPlaceholderManager;
    private LeaderboardManager leaderboardManager;
    private LanguageManager languageManager;
    private EconomyAdapter economyAdapter;
    private MysqlDatabase mysqlDatabase;
    private ArenaManager arenaManager;
    private VaultManager vaultManager;
    private UserManager userManager;
    private DuelManager duelManager;

    private PluginManager pluginManager;
    private InventoryAPI inventoryAPI;

    private DatabaseType usedDatabaseType = null;
    private boolean hologramsEnabled = false;
    private boolean databaseEnabled = false;
    private boolean forceDisable = false;

    @Override
    public void onEnable() {
        if (!checkLicence()) {
            return;
        }

        saveDefaultConfig();
        inventoryAPI = InventoryAPI.getInstance(this);
        pluginManager = getServer().getPluginManager();

        if (!registerManagers()) {
            forceClose();
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            userManager.getOrLoadUser(p);
        }

        pluginManager.registerEvents(new PlayerListener(this), this);
        pluginManager.registerEvents(new HInventoryClickListener(this), this);
        pluginManager.registerEvents(new InMatchEventListener(this), this);

        getCommand("accept").setExecutor(new DuelAcceptCommand(this));
        getCommand("arena").setExecutor(new ArenaCommand());
        getCommand("bduels").setExecutor(new BDuelsCommand(this));
        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("duelstats").setExecutor(new DuelStatsCommand(this));
        getCommand("toggleduel").setExecutor(new ToggleDuelRequestsCommand(this));
        if (isLeaderboardManagerReady()) {
            getCommand("leaderboard").setExecutor(new LeaderboardCommand(this));
        }

        for (String s : Utils.getMessageList("after-load", null)) {
            getLogger().info(s);
        }
    }

    private boolean checkLicence() {
        boolean licenced = new LicenseController().checkLicense("bDuels");
        if (!licenced) {
            getLogger().warning("Lisans doğrulanamadı. Eklenti kapatılıyor.");
            forceClose();
        }
        return licenced;
    }

    private void forceClose() {
        forceDisable = true;
        setEnabled(false);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        if (!forceDisable) {
            duelManager.endMatches(DuelEndReason.SERVER_STOP, true);
            save(true);
            if (databaseEnabled && usedDatabaseType == DatabaseType.MYSQL) {
                mysqlDatabase.shutdownConnPool();
            }
        }
    }

    private boolean registerManagers() {
        if (pluginManager.isPluginEnabled("Vault")) {
            vaultManager = new VaultManager(this);
            setEconomyAdapter(new VaultEconomy(this));
        } else {
            setEconomyAdapter(new EmptyEconomy());
            getLogger().warning("Sunucunuzda Vault bulunamadığı için ekonomiye bağlı sistemler çalışmayacaktır.");
        }

        databaseEnabled = getConfig().getBoolean("database.enabled");
        if (databaseEnabled) {
            usedDatabaseType = Optional.of(DatabaseType.valueOf(getConfig().getString("database.database-type"))).orElse(DatabaseType.FLAT);
            if (usedDatabaseType == DatabaseType.MYSQL) {
                mysqlDatabase = new MysqlDatabase(getLogger(), getConfig());
                if (mysqlDatabase.shouldClose()) {
                    return false;
                }
            }

            customPlaceholderManager = new CustomPlaceholderManager(this);
            languageManager = new LanguageManager(this);
            arenaManager = new ArenaManager(this);
            duelManager = new DuelManager(this);
            userManager = new UserManager(this);

            hologramsEnabled = pluginManager.isPluginEnabled("HolographicDisplays");
            if (!hologramsEnabled) {
                getLogger().warning("HolographicDisplays bulunamadı, sıralama hologramı çalışmayacak.");
            }

            if (databaseEnabled) {
                leaderboardManager = new LeaderboardManager(this);
            }

            if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
                new PAPIPlaceholders(this).register();
            } else {
                getLogger().warning("Sunucunuzda PlaceholderAPI kurulu değil, ona bağlı özellikleri kullanamayacaksınız.");
            }
        }
        return true;
    }

    public boolean saveAllUserStatistics(boolean sync) {
        if (!databaseEnabled) {
            return false;
        }

        MySQLManager mySQLManager = userManager.getMysqlManager();
        for (User user : userManager.getUserList()) {
            if (usedDatabaseType == DatabaseType.FLAT) {
                userManager.saveStatistics(user, sync);
            } else if (mySQLManager != null) {
                mySQLManager.saveAllStatistic(user, sync);
            }
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

    public EconomyAdapter getEconomyAdapter() {
        return economyAdapter;
    }

    public DuelManager getDuelManager() {
        return duelManager;
    }

    public InventoryAPI getInventoryAPI() {
        return inventoryAPI;
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

    public DatabaseType getUsedDatabaseType() {
        return usedDatabaseType;
    }

    public MysqlDatabase getMySQLDatabase() {
        return mysqlDatabase;
    }

    public void reload() {
        reloadConfig();
        languageManager.load();
        arenaManager.load();
        duelManager.reload();
        userManager.prepareSaveProcess();
        if (isLeaderboardManagerReady()) {
            leaderboardManager.reload(false);
        }
    }

    public void save(boolean sync) {
        arenaManager.save();
        saveAllUserStatistics(sync);
        if (isLeaderboardManagerReady()) {
            leaderboardManager.save();
        }
    }

}