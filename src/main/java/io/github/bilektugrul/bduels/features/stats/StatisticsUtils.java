//* Copyright (C) 2021 Despical and contributors

package io.github.bilektugrul.bduels.features.stats;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.features.leaderboards.LeaderboardEntry;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Despical
 **/
public class StatisticsUtils {

    private static final BDuels plugin = JavaPlugin.getPlugin(BDuels.class);

    public static void getStats(StatisticType stat, Consumer<List<LeaderboardEntry>> consumer) {
        if (stat == null | consumer == null) {
            return;
        }

        if (!plugin.isDatabaseEnabled()) {
            plugin.getLogger().warning(stat.name() + " için tüm istatistikler istendi ama database özelliği kapalı.");
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (plugin.getUsedDatabaseType() == DatabaseType.FLAT) {
                getFlatStats(stat, consumer);
                return;
            }

            UserManager userManager = plugin.getUserManager();
            if (!userManager.isMysqlManagerReady()) {
                plugin.getLogger().warning(stat.name() + " için tüm istatistikler istendi ama SQL bağlantısı kurulamadı.");
                return;
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
                    consumer.accept(leaderboardEntries);
                } catch (NullPointerException e) {
                    exception(e);
                }
            } catch (SQLException e) {
                exception(e);
            }
        });
    }

    private static void getFlatStats(StatisticType stat, Consumer<List<LeaderboardEntry>> consumer) {
        if (stat == null | consumer == null) {
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            File base = new File(plugin.getDataFolder() + "/players/");
            List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();

            List<File> users;
            try {
                users = new ArrayList<>(Arrays.asList(base.listFiles()));
            } catch (NullPointerException ignored) {
                plugin.getLogger().warning("Hiç kayıtlı flat oyuncu istatistiği yok.");
                consumer.accept(leaderboardEntries);
                return;
            }

            for (File file : users) {
                FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                String name = data.getString("name");
                int value = data.getInt("stats." + stat.name());
                LeaderboardEntry entry = new LeaderboardEntry(name, value);
                leaderboardEntries.add(entry);
            }
            consumer.accept(leaderboardEntries);
        });
    }

    private static void exception(Exception e) {
        plugin.getLogger().warning("SQL'den istatistikleri almaya çalışırken bir şeyler ters gitti. Lütfen SQL ayarlarınızı gözden geçirin.");
        e.printStackTrace();
        plugin.getLogger().warning("SQL'den istatistikleri almaya çalışırken bir şeyler ters gitti. Lütfen SQL ayarlarınızı gözden geçirin.");
    }

}