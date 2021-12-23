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

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {

    private final BDuels plugin;
    private final Set<Leaderboard> leaderboards = new HashSet<>();

    private SimpleDateFormat formatter;
    private FileConfiguration file;

    public LeaderboardManager(BDuels plugin) {
        this.plugin = plugin;
        reload(true);
    }

    public void reload(boolean first) {
        clear();
        file = ConfigUtils.getConfig(plugin, "leaderboards");
        formatter = new SimpleDateFormat(Utils.getMessage("leaderboards.date-format", null));
        for (String key : file.getConfigurationSection("leaderboards").getKeys(false)) {
            if (key.contains(" ")) {
                plugin.getLogger().warning(key + " ID'li sıralamanın isminde boşluk olduğu için düzgün şekilde oluşturulamadı. Lütfen boşluk kullanmayın.");
                continue;
            }
            loadLeaderboardFromFile(key);
        }
        if (first) {
            sortEveryLeaderboard();
        }
    }

    public void loadLeaderboardFromFile(String id) {
        if (id == null) {
            return;
        }

        String path = "leaderboards." + id;
        if (!file.isConfigurationSection(path)) {
            return;
        }

        try {
            path = path + ".";
            String name = file.getString(path + "name");
            int max = file.getInt(path + "max-size");
            StatisticType type = StatisticType.valueOf(file.getString(path + "type").toUpperCase(Locale.ROOT));
            SortingType how = SortingType.valueOf(file.getString(path + "how"));
            Location hologramLocation = LocationSerializer.fromString(file.getString(path + "hologram-location"));

            Leaderboard leaderboard = new Leaderboard(id, name, type, how, max);
            prepareEntries(leaderboard);
            leaderboards.add(leaderboard);
            if (plugin.isHologramsEnabled() && hologramLocation != null) {
                leaderboard.createHologram(plugin, hologramLocation);
                updateHologram(leaderboard);
            }
        } catch (NullPointerException ignored) {
            plugin.getLogger().warning(id + " ID'li sıralamanın verilerinde hata olduğu için düzgün şekilde oluşturulamadı.");
        }
    }

    public void prepareEntries(Leaderboard leaderboard) {
        String name = leaderboard.getId();
        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
        for (String key : file.getConfigurationSection("leaderboards." + name + ".leaderboard").getKeys(false)) {
            String path = "leaderboards." + name + ".leaderboard." + key;
            String entryName = file.getString(path + ".name");
            int value = file.getInt(path + ".value");
            leaderboardEntries.add(new LeaderboardEntry(entryName, value));
            if (leaderboardEntries.size() == leaderboard.getMaxSize()) {
                break;
            }
        }
        leaderboard.setLeaderboardEntries(leaderboardEntries);
    }

    public void createLeaderboard(String id) {
        if (id == null) {
            return;
        }

        id = id.replace(" ", "_");
        leaderboards.add(new Leaderboard(id));
    }

    public void deleteLeaderboard(String id) {
        if (id == null) {
            return;
        }

        deleteLeaderboard(getFromID(id), true);
    }

    public void deleteLeaderboard(Leaderboard leaderboard, boolean remove) {
        Hologram hologram = leaderboard.getHologram();
        if (hologram != null) {
            hologram.delete();
        }
        if (remove) {
            leaderboards.remove(leaderboard);
        }
    }

    public void clear() {
        for (Leaderboard leaderboard : leaderboards) {
            deleteLeaderboard(leaderboard, false);
        }
        leaderboards.clear();
    }

    public boolean isPresent(String id) {
        return getFromID(id) != null;
    }

    public Leaderboard getFromID(String name) {
        for (Leaderboard leaderboard : leaderboards) {
            if (leaderboard.getId().equalsIgnoreCase(name)) {
                return leaderboard;
            }
        }
        return null;
    }

    public Leaderboard getFromName(String name) {
        for (Leaderboard leaderboard : leaderboards) {
            if (leaderboard.getName().equalsIgnoreCase(name)) {
                return leaderboard;
            }
        }
        return null;
    }

    public void sort(Leaderboard leaderboard) {
        StatisticsUtils.getStats(leaderboard.getType(), leaderboardEntries -> {
            if (leaderboardEntries != null && !leaderboardEntries.isEmpty()) {
                leaderboard.setLeaderboardEntries(leaderboardEntries
                        .stream()
                        .sorted(isReversed(leaderboard.getSortingType())
                                ? Comparator.comparingInt(LeaderboardEntry::getValue).reversed()
                                : Comparator.comparingInt(LeaderboardEntry::getValue))
                        .limit(leaderboard.getMaxSize())
                        .collect(Collectors.toList())
                );
                plugin.getServer().getScheduler().runTask(plugin, () -> updateHologram(leaderboard));
            }
        });
    }

    public void sortEveryLeaderboard() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Leaderboard leaderboard : leaderboards) {
                sort(leaderboard);
            }
        });
    }

    public List<String> leaderboardToString(Leaderboard leaderboard, CommandSender sender, String mode) {
        if (!leaderboard.isReady()) {
            return Collections.singletonList(Utils.getMessage("leaderboards.not-ready", sender));
        }

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
        if (!plugin.isHologramsEnabled()) {
            return;
        }

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

    public void save() {
        for (Leaderboard leaderboard : leaderboards) {
            saveLeaderboard(leaderboard);
        }
        ConfigUtils.saveConfig(plugin, file, "leaderboards");
    }

    public boolean saveLeaderboard(Leaderboard leaderboard) {
        if (leaderboard == null) {
            throw new NullPointerException("Leaderboard can not be null!");
        }

        try {
            String leaderboardId = leaderboard.getId();
            String path = "leaderboards." + leaderboardId + ".";
            file.set(path + "leaderboard", null);
            Hologram hologram = leaderboard.getHologram();
            if (hologram != null) {
                file.set(path + "hologram-location", LocationSerializer.toString(hologram.getLocation()));
            }
            file.set(path + "name", leaderboard.getName());
            file.set(path + "type", leaderboard.getType().name());
            file.set(path + "how", leaderboard.getSortingType().name());
            file.set(path + "max-size", leaderboard.getMaxSize());
            int i = 1;
            for (LeaderboardEntry entry : leaderboard.getLeaderboardEntries()) {
                String entryPath = path + "leaderboard." + i++ + ".";
                file.set(entryPath + "name", entry.getName());
                file.set(entryPath + "value", entry.getValue());
            }
        } catch (NullPointerException ignored) {
            return false;
        }
        return true;
    }

    public Set<Leaderboard> getLeaderboards() {
        return leaderboards;
    }

    public String getReadableLeaderboards() {
        StringBuilder builder = new StringBuilder();
        leaderboards.forEach(leaderboard -> builder.append(leaderboard.getName()).append(", "));
        return builder.substring(0, builder.length() - 2);
    }

    public String getReadableLeaderboardIDs() {
        StringBuilder builder = new StringBuilder();
        leaderboards.forEach(leaderboard -> builder.append(leaderboard.getId()).append(", "));
        return builder.substring(0, builder.length() - 2);
    }

    public boolean isReversed(SortingType type) {
        return type == SortingType.HIGH_TO_LOW;
    }

}