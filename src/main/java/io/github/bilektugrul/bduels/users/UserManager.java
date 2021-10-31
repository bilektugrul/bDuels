package io.github.bilektugrul.bduels.users;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.users.data.MySQLManager;
import io.github.bilektugrul.bduels.users.data.StatisticSaveProcess;
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
            this.mysqlManager = new MySQLManager(plugin);
            prepareSaveProcess();
        }
    }

    public void prepareSaveProcess() {
        if (isMysqlManagerReady()) {
            if (statisticSaveProcess != null) statisticSaveProcess.cancel();
            statisticSaveProcess = new StatisticSaveProcess(plugin);
            statisticSaveProcess.start();
        }
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

    public boolean isInMatch(User user) {
        return user.getState() == UserState.IN_MATCH || user.getState() == UserState.STARTING_MATCH;
    }

    public Set<User> getUserList() {
        return new HashSet<>(userList);
    }

    public MySQLManager getMysqlManager() {
        return mysqlManager;
    }

    public boolean isMysqlManagerReady() {
        return mysqlManager != null;
    }

    public void loadStatistics(User user) {
        if (isMysqlManagerReady()) mysqlManager.loadStatistics(user);
    }

    public void saveStatistics(User user, boolean sync) {
        if (isMysqlManagerReady()) mysqlManager.saveAllStatistic(user, sync);
    }

}