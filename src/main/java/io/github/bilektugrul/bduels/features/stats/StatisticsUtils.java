//* Copyright (C) 2021 Despical and contributors

package io.github.bilektugrul.bduels.features.stats;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.features.leaderboards.LeaderboardEntry;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
**/
public class StatisticsUtils {

    private static final BDuels plugin = JavaPlugin.getPlugin(BDuels.class);

    public static List<LeaderboardEntry> getStats(StatisticType stat) {
        if (!plugin.isDatabaseEnabled()) {
            return null;
        }

        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
        try (Connection connection = plugin.getMySQLDatabase().getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet set = statement.executeQuery("SELECT UUID, " + stat.getName() + ", name FROM " + plugin.getUserManager().getMysqlManager().getTableName() + " ORDER BY " + stat.getName());
            while (set.next()) {
                String name = set.getString("name");
                Integer value = set.getInt(stat.getName());
                leaderboardEntries.add(new LeaderboardEntry(name, value));
            }
            return leaderboardEntries;
        } catch (SQLException e) {
            plugin.getLogger().warning("Something went wrong while trying to get statistics. Check your SQL settings.");
            e.printStackTrace();
            plugin.getLogger().warning("Something went wrong while trying to get statistics. Check your SQL settings.");
            return null;
        }
    }

}