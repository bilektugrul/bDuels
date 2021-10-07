//* Copyright (C) 2021 Despical

package io.github.bilektugrul.bduels.users.data;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.features.stats.StatisticType;
import io.github.bilektugrul.bduels.users.User;
import me.despical.commons.database.MysqlDatabase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Despical
 */
public class MySQLManager {

    private final BDuels plugin;
    private final String tableName;
    private final MysqlDatabase database;

    public MySQLManager(BDuels plugin) {
        this.plugin = plugin;
        this.tableName = plugin.getConfig().getString("table", "bduelsstats");
        this.database = plugin.getMySQLDatabase();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = database.getConnection()) {
                Statement statement = connection.createStatement();
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n"
                        + "  `UUID` char(36) NOT NULL PRIMARY KEY,\n"
                        + "  `name` varchar(32) NOT NULL,\n"
                        + "  `wins` int(11) NOT NULL DEFAULT '0',\n"
                        + "  `loses` int(11) NOT NULL DEFAULT '0',\n"
                        + "  `total_matches` int(11) NOT NULL DEFAULT '0',\n"
                        + "  `total_earned_money` int(11) NOT NULL DEFAULT '0',\n"
                        + "  `total_earned_item` int(11) NOT NULL DEFAULT '0',\n"
                        + "  `total_lost_money` int(11) NOT NULL DEFAULT '0',\n"
                        + "  `total_lost_item` int(11) NOT NULL DEFAULT '0',\n"
                        + "  `duel_requests` tinyint(1) NOT NULL DEFAULT '1'\n"
                        + ");");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void saveStatistic(User user, StatisticType stat) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String query = "UPDATE " + tableName + " SET " + stat.getName() + "=" + user.getStat(stat)+ " WHERE UUID='" + user.getUUID().toString() + "';";
            database.executeUpdate(query);
        });
    }

    public void saveAllStatistic(User user, boolean sync) {
        StringBuilder update = new StringBuilder(" SET ");

        for (StatisticType stat : StatisticType.values()) {
            if (!stat.isPersistent()) continue;
            if (update.toString().equalsIgnoreCase(" SET ")) {
                update.append(stat.getName()).append("=").append(user.getStat(stat));
            }

            update.append(", ").append(stat.getName()).append("=").append(user.getStat(stat));
        }

        String finalUpdate = update.toString();
        if (!sync)
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE " + tableName + finalUpdate + " WHERE UUID='" + user.getUUID().toString() + "';"));
        else
            database.executeUpdate("UPDATE " + tableName + finalUpdate + " WHERE UUID='" + user.getUUID().toString() + "';");
    }

    public void loadStatistics(User user) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String uuid = user.getUUID().toString(), name = user.getName();

            try (Connection connection = database.getConnection()) {
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT * from " + tableName + " WHERE UUID='" + uuid + "';");

                if (rs.next()) {

                    for (StatisticType stat : StatisticType.values()) {
                        if (!stat.isPersistent()) continue;

                        user.setStat(stat, rs.getInt(stat.getName()));
                    }
                } else {
                    statement.executeUpdate("INSERT INTO " + tableName + " (UUID,name) VALUES ('" + uuid + "','" + name + "');");

                    for (StatisticType stat : StatisticType.values()) {
                        if (!stat.isPersistent()) continue;

                        user.setStat(stat, stat.getDefaultValue());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public String getTableName() {
        return tableName;
    }

    public MysqlDatabase getDatabase() {
        return database;
    }

}