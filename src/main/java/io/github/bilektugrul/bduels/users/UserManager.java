package io.github.bilektugrul.bduels.users;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.features.stats.StatisticType;
import io.github.bilektugrul.bduels.users.data.DatabaseType;
import io.github.bilektugrul.bduels.users.data.MySQLManager;
import io.github.bilektugrul.bduels.users.data.StatisticSaveProcess;
import me.despical.commons.configuration.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class UserManager {

    private final BDuels plugin;
    private final Map<UUID, User> cachedUsers = new HashMap<>();

    private MySQLManager mysqlManager;
    private StatisticSaveProcess statisticSaveProcess;

    public UserManager(BDuels plugin) {
        this.plugin = plugin;
        if (plugin.isDatabaseEnabled()) {
            if (plugin.getUsedDatabaseType() == DatabaseType.MYSQL) {
                this.mysqlManager = new MySQLManager(plugin);
            }
            prepareSaveProcess();
        }
    }

    public void prepareSaveProcess() {
        if (statisticSaveProcess != null) {
            statisticSaveProcess.cancel();
        }
        statisticSaveProcess = new StatisticSaveProcess(plugin);
        statisticSaveProcess.start();
    }

    public User getOrLoadUser(Player player) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        UUID uuid = player.getUniqueId();
        return cachedUsers.computeIfAbsent(uuid, k -> {
            User user = new User(player);
            loadStatistics(user);
            return user;
        });
    }

    public void removeUser(User user) {
        cachedUsers.remove(user.getUUID());
    }

    public boolean isMysqlManagerReady() {
        return mysqlManager != null;
    }

    public void loadStatistics(User user) {
        if (user == null) {
            return;
        }

        switch (plugin.getUsedDatabaseType()) {
            case FLAT:
                FileConfiguration data = user.getData();
                for (StatisticType statisticType : StatisticType.values()) {
                    String path = "stats." + statisticType.name();
                    if (statisticType == StatisticType.DUEL_REQUESTS && !data.isSet(path)) {
                        data.set(path, 1);
                    }
                    int stat = data.getInt(path);
                    user.setStat(statisticType, stat);
                }
                return;
            case MYSQL:
                mysqlManager.loadStatistics(user);
        }
    }

    public void saveStatistics(User user, boolean sync) {
        if (user == null) {
            return;
        }

        switch (plugin.getUsedDatabaseType()) {
            case FLAT:
                FileConfiguration data = user.getData();
                String path = "/players/" + user.getUUID();
                data.set("name", user.getName());
                for (StatisticType statisticType : StatisticType.values()) {
                    data.set("stats." + statisticType.name(), user.getStat(statisticType));
                }
                if (!sync) {
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> ConfigUtils.saveConfig(plugin, data, path));
                } else {
                    ConfigUtils.saveConfig(plugin, data, path);
                }
                return;
            case MYSQL:
                mysqlManager.saveAllStatistic(user, sync);
        }
    }

    public Set<User> getCachedUsers() {
        return new HashSet<>(cachedUsers.values());
    }

    public MySQLManager getMysqlManager() {
        return mysqlManager;
    }

}