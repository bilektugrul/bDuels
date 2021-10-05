//* Copyright (C) 2021 Despical and contributors

package io.github.bilektugrul.bduels.stats;

import io.github.bilektugrul.bduels.BDuels;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 */
public class StatsStorage {

    private static final BDuels plugin = JavaPlugin.getPlugin(BDuels.class);

    public static Map<UUID, Integer> getStats(StatisticType stat) {
        if (!plugin.isDatabaseEnabled()) {
            return null;
        }

        try (Connection connection = plugin.getMySQLDatabase().getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet set = statement.executeQuery("SELECT UUID, " + stat.getName() + " FROM " + plugin.getUserManager().getMysqlManager().getTableName() + " ORDER BY " + stat.getName());
            Map<UUID, Integer> column = new HashMap<>();

            while (set.next()) {
                column.put(UUID.fromString(set.getString("UUID")), set.getInt(stat.getName()));
            }

            return column;
        } catch (SQLException e) {
            plugin.getLogger().warning("Something went wrong while trying to get statistics. Check your SQL settings.");
            return null;
        }
    }

}