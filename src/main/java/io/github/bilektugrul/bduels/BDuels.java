package io.github.bilektugrul.bduels;

import com.hakan.inventoryapi.InventoryAPI;
import io.github.bilektugrul.bduels.arenas.ArenaManager;
import io.github.bilektugrul.bduels.commands.BDuelsCommand;
import io.github.bilektugrul.bduels.commands.DuelStatsCommand;
import io.github.bilektugrul.bduels.commands.arena.base.ArenaCommand;
import io.github.bilektugrul.bduels.commands.duel.DuelAcceptCommand;
import io.github.bilektugrul.bduels.commands.duel.DuelCommand;
import io.github.bilektugrul.bduels.commands.duel.ToggleDuelRequestsCommand;
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
import io.github.bilektugrul.bduels.stats.StatisticType;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserManager;
import io.github.bilektugrul.bduels.users.data.MySQLManager;
import me.despical.commons.database.MysqlDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

    private InventoryAPI inventoryAPI;

    private boolean databaseEnabled = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        inventoryAPI = InventoryAPI.getInstance(this);

        vaultManager = new VaultManager(this);
        vaultEconomy = new VaultEconomy(this);
        databaseEnabled = getConfig().getBoolean("database.enabled");
        if (databaseEnabled)
            mysqlDatabase = new MysqlDatabase(getConfig().getString("database.user"), getConfig().getString("database.password"), getConfig().getString("database.url"));
        customPlaceholderManager = new CustomPlaceholderManager(this);
        languageManager = new LanguageManager(this);
        duelManager = new DuelManager(this);
        arenaManager = new ArenaManager(this);
        duelManager.setArenaManager(arenaManager);
        arenaManager.setDuelManager(duelManager);
        userManager = new UserManager(this);

        for (Player p : Bukkit.getOnlinePlayers()) {
            User user = userManager.loadUser(p);
            userManager.loadStatistics(user);
        }

        new PAPIPlaceholders(this).register();
        
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new HInventoryClickListener(this), this);

        getCommand("accept").setExecutor(new DuelAcceptCommand(this));
        getCommand("arena").setExecutor(new ArenaCommand(this));
        getCommand("bduels").setExecutor(new BDuelsCommand(this));
        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("duelstats").setExecutor(new DuelStatsCommand(this));
        getCommand("toggleduel").setExecutor(new ToggleDuelRequestsCommand(this));
    }

    @Override
    public void onDisable() {
        duelManager.endMatches(DuelEndReason.SERVER_STOP);
        if (databaseEnabled) {
            saveAllUserStatistics();
            mysqlDatabase.shutdownConnPool();
        }
        Bukkit.getScheduler().cancelTasks(this);
        save();
    }

    public boolean saveAllUserStatistics() {
        MySQLManager mySQLManager = userManager.getMysqlManager();
        if (mySQLManager == null) {
            return false;
        }
        for (Player player : getServer().getOnlinePlayers()) {
            User user = userManager.getUser(player);

            StringBuilder update = new StringBuilder(" SET ");

            for (StatisticType stat : StatisticType.values()) {
                if (!stat.isPersistent()) continue;
                if (update.toString().equalsIgnoreCase(" SET ")) {
                    update.append(stat.getName()).append("'='").append(user.getStat(stat));
                }

                update.append(", ").append(stat.getName()).append("'='").append(user.getStat(stat));
            }

            String finalUpdate = update.toString();
            mySQLManager.getDatabase().executeUpdate("UPDATE " + mySQLManager.getTableName() + finalUpdate + " WHERE UUID='" + user.getUUID().toString() + "';");
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

    public boolean isDatabaseEnabled() {
        return databaseEnabled;
    }

    public MysqlDatabase getMySQLDatabase() {
        return mysqlDatabase;
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
