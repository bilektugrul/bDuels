package io.github.bilektugrul.bduels.users;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.features.stats.StatisticType;
import io.github.bilektugrul.bduels.users.data.DatabaseType;
import io.github.bilektugrul.bduels.users.data.MySQLManager;
import io.github.bilektugrul.bduels.users.data.StatisticSaveProcess;
import me.despical.commons.configuration.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserManager {

    private final BDuels plugin;
    private final Set<User> userList = new HashSet<>();

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

    public User getUser(Player player) {
        if (!player.isOnline()) {
            return null;
        }

        UUID uuid = player.getUniqueId();
        for (User user : userList) {
            if (user.getUUID().equals(uuid)) {
                return user;
            }
        }

        User user = new User(player);
        loadStatistics(user);
        userList.add(user);
        return user;
    }

    public void removeUser(User user) {
        userList.remove(user);
    }

    public boolean isMysqlManagerReady() {
        return mysqlManager != null;
    }

    public void loadStatistics(User user) {
        if (plugin.getUsedDatabaseType() == DatabaseType.FLAT) {
            FileConfiguration data = user.getData();
            for (StatisticType statisticType : StatisticType.values()) {
                String path = "stats." + statisticType.name();
                if (statisticType == StatisticType.DUEL_REQUESTS && !data.isSet(path)) {
                    data.set(path, 1);
                }
                int stat = data.getInt(path);
                user.setStat(statisticType, stat);
            }
        } else if (plugin.getUsedDatabaseType() == DatabaseType.MYSQL && isMysqlManagerReady()) {
            mysqlManager.loadStatistics(user);
        }
    }

    public void saveStatistics(User user, boolean sync) {
        if (plugin.getUsedDatabaseType() == DatabaseType.FLAT) {
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
        } else if (plugin.getUsedDatabaseType() == DatabaseType.MYSQL && isMysqlManagerReady()) {
            mysqlManager.saveAllStatistic(user, sync);
        }
    }

    public Set<User> getUserList() {
        return new HashSet<>(userList);
    }

    public MySQLManager getMysqlManager() {
        return mysqlManager;
    }

}