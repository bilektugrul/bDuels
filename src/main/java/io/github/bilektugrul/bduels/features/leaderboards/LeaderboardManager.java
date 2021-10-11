package io.github.bilektugrul.bduels.features.leaderboards;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.features.stats.StatisticType;
import io.github.bilektugrul.bduels.features.stats.StatisticsUtils;
import io.github.bilektugrul.bduels.utils.Utils;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitScheduler;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {

    private final BDuels plugin;
    private final BukkitScheduler scheduler;
    private final List<Leaderboard> leaderboards = new ArrayList<>();

    private SimpleDateFormat formatter;
    private FileConfiguration file;

    public LeaderboardManager(BDuels plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
        reloadSettings(true);
    }

    public void reloadSettings(boolean first) {
        clear();
        file = ConfigUtils.getConfig(plugin, "leaderboards");
        formatter = new SimpleDateFormat(Utils.getMessage("leaderboards.date-format", null));
        for (String key : file.getConfigurationSection("leaderboards").getKeys(false)) {
            String path = "leaderboards." + key + ".";
            String name = file.getString(path + "name");
            int max = file.getInt(path + "max-size");
            StatisticType type = StatisticType.valueOf(file.getString(path + "type").toUpperCase(Locale.ROOT));
            Location hologramLocation = LocationSerializer.fromString(file.getString(path + "hologram-location"));
            Leaderboard leaderboard = new Leaderboard(key, name, type, SortingType.valueOf(file.getString(path + "how")), max);
            prepareEntries(leaderboard);
            leaderboards.add(leaderboard);
            if (plugin.isHologramsEnabled() && hologramLocation != null) {
                leaderboard.createHologram(plugin, hologramLocation);
            }
        }
        if (first) sortEveryLeaderboard();
    }

    public void prepareEntries(Leaderboard leaderboard) {
        String name = leaderboard.getId();
        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
        for (String key : file.getConfigurationSection("leaderboards." + name + ".leaderboard").getKeys(false)) {
            String path = "leaderboards." + name + ".leaderboard." + key;
            String entryName = file.getString(path + ".name");
            int value = file.getInt(path + ".value");
            leaderboardEntries.add(new LeaderboardEntry(entryName, value));
            if (leaderboardEntries.size() == leaderboard.getMaxSize()) break;
        }
        leaderboard.setLeaderboardEntries(leaderboardEntries);
    }

    public void clear() {
        for (Leaderboard leaderboard : leaderboards) {
            Hologram hologram = leaderboard.getHologram();
            if (hologram != null) hologram.delete();
        }
        leaderboards.clear();
    }

    public Leaderboard getFromID(String name) {
        for (Leaderboard leaderboard : leaderboards) {
            if (leaderboard.getId().equals(name)) {
                return leaderboard;
            }
        }
        return null;
    }

    public void sort(Leaderboard leaderboard) {
        List<LeaderboardEntry> leaderboardEntries = StatisticsUtils.getStats(leaderboard.getType());
        if (leaderboardEntries != null) {
            leaderboard.setLeaderboardEntries(leaderboardEntries
                    .stream()
                    .sorted(isReversed(leaderboard.getSortingType())
                            ? Comparator.comparingInt(LeaderboardEntry::getValue).reversed()
                            : Comparator.comparingInt(LeaderboardEntry::getValue))
                    .limit(leaderboard.getMaxSize())
                    .collect(Collectors.toList())
            );
            scheduler.runTask(plugin, () -> updateHologram(leaderboard));
        }
    }

    public void sortEveryLeaderboard() {
        scheduler.runTaskAsynchronously(plugin, () -> leaderboards.forEach(this::sort));
    }

    public List<String> leaderboardToString(Leaderboard leaderboard, CommandSender sender, String mode) {
        List<String> messages = new ArrayList<>();
        Date date = new Date();
        for (String value : Utils.getMessageList("leaderboards." + mode + ".before-leaderboard", sender)) {
            value = value.replace("%leaderboardname%", leaderboard.getName())
                    .replace("%lastrenew%", formatter.format(date));
            messages.add(value);
        }

        int i = 1;
        for (LeaderboardEntry entry : leaderboard.getLeaderboardEntries()) {
            messages.add(Utils.getMessage("leaderboards." + mode + ".entry-format", sender)
                    .replace("%#%", String.valueOf(i++))
                    .replace("%name%", entry.getName())
                    .replace("%value%", String.valueOf(entry.getValue()))
                    .replace("%type%", Utils.getMessage("leaderboards.type-names." + leaderboard.getType().name()))
            );
        }

        for (String value : Utils.getMessageList("leaderboards." + mode + ".after-leaderboard", sender)) {
            value = value.replace("%leaderboardname%", leaderboard.getName())
                    .replace("%lastrenew%", formatter.format(date));
            messages.add(value);
        }
        return messages;
    }

    public void updateHologram(Leaderboard leaderboard) {
        if (!plugin.isHologramsEnabled()) return;
        Hologram hologram = leaderboard.getHologram();
        if (hologram != null) {
            hologram.clearLines();
            for (String line : leaderboardToString(leaderboard, null, "hologram")) {
                hologram.appendTextLine(line);
            }
        }
    }

    public void leaderboardToChatMessage(Leaderboard leaderboard, CommandSender sender) {
        sender.sendMessage(Utils.listToString(leaderboardToString(leaderboard, sender, "chat")));
    }

    public boolean save() {
        for (Leaderboard leaderboard : leaderboards) {
            String leaderboardName = leaderboard.getId();
            String path = "leaderboards." + leaderboardName + ".";
            Hologram hologram = leaderboard.getHologram();
            if (hologram != null) file.set(path + "hologram-location", LocationSerializer.toString(hologram.getLocation()));
            int i = 1;
            file.set(path + "leaderboard", new ArrayList<>());
            for (LeaderboardEntry entry : leaderboard.getLeaderboardEntries()) {
                String entryPath = path + "leaderboard." + i++ + ".";
                file.set(entryPath + "name", entry.getName());
                file.set(entryPath + "value", entry.getValue());
            }
        }
        ConfigUtils.saveConfig(plugin, file, "leaderboards");
        return true;
    }

    public List<Leaderboard> getLeaderboards() {
        return new ArrayList<>(leaderboards);
    }

    public String getReadableLeaderboards() {
        StringBuilder builder = new StringBuilder();
        leaderboards.forEach(leaderboard -> builder.append(leaderboard.getId()).append(", "));
        return builder.substring(0, builder.length() - 2);
    }

    public boolean isReversed(SortingType type) {
        return type == SortingType.HIGH_TO_LOW;
    }

}