//* Copyright (C) 2021 Despical and contributors

package io.github.bilektugrul.bduels.features.stats;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.features.leaderboards.LeaderboardEntry;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserManager;
import io.github.bilektugrul.bduels.users.data.DatabaseType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Despical
**/
public class StatisticsUtils {

    private static final BDuels plugin = JavaPlugin.getPlugin(BDuels.class);

    public static List<LeaderboardEntry> getStats(StatisticType stat) {
        if (plugin.getUsedDatabaseType() == DatabaseType.FLAT) {
            return getFlatStats(stat);
        }
        UserManager userManager = plugin.getUserManager();
        if (!plugin.isDatabaseEnabled() || !userManager.isMysqlManagerReady()) {
            plugin.getLogger().warning(stat.name() + " için tüm istatistikler istendi ama bir hata oluştu.");
            return null;
        }

        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
        try (Connection connection = plugin.getMySQLDatabase().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                ResultSet set = statement.executeQuery("SELECT UUID, " + stat.getName()
                        + ", name FROM " + userManager.getMysqlManager().getTableName()
                        + " ORDER BY " + stat.getName());
                while (set.next()) {
                    String name = set.getString("name");
                    int value = set.getInt(stat.getName());
                    leaderboardEntries.add(new LeaderboardEntry(name, value));
                }
                return leaderboardEntries;
            } catch (NullPointerException e) {
                exception(e);
                return null;
            }
        } catch (SQLException e) {
            exception(e);
            return null;
        }
    }
    
    public static List<LeaderboardEntry> getFlatStats(StatisticType statisticType) {
        File base = new File(plugin.getDataFolder() + "/players/");
        List<File> users = null;
        try {
            users = new ArrayList<>(Arrays.asList(base.listFiles()));
        } catch (NullPointerException ignored) {
            plugin.getLogger().warning("Hiç kayıtlı flat oyuncu istatistiği yok.");
        }

        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
        for (File file : users) {
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            String name = data.getString("name");
            int value = data.getInt("stats." + statisticType.name());
            LeaderboardEntry entry = new LeaderboardEntry(name, value);
            leaderboardEntries.add(entry);
        }
        return leaderboardEntries;
    }

    private static void exception(Exception e) {
        plugin.getLogger().warning("Something went wrong while trying to get statistics. Check your SQL settings.");
        e.printStackTrace();
        plugin.getLogger().warning("Something went wrong while trying to get statistics. Check your SQL settings.");
    }

}