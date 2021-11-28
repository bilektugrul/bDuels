package io.github.bilektugrul.bduels.features.leaderboards;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.features.stats.StatisticType;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Leaderboard {

    private final String id;

    private String name;
    private int maxSize;
    private StatisticType type;
    private SortingType sortingType;
    private Hologram hologram;
    private List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();

    public Leaderboard(String id) {
        this(id, id, null, null, 5);
    }

    public Leaderboard(String id, String name, StatisticType type, SortingType sortingType, int maxSize) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.sortingType = sortingType;
        setMaxSize(maxSize);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = Math.min(maxSize, 20);
    }

    public void setType(StatisticType type) {
        this.type = type;
    }

    public void setSortingType(SortingType sortingType) {
        this.sortingType = sortingType;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public StatisticType getType() {
        return type;
    }

    public SortingType getSortingType() {
        return sortingType;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public Hologram createHologram(BDuels plugin, Location location) {
        if (hologram != null) {
            hologram.delete();
        }
        hologram = HologramsAPI.createHologram(plugin, location);
        return hologram;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public void setLeaderboardEntries(List<LeaderboardEntry> leaderboardEntries) {
        this.leaderboardEntries = leaderboardEntries;
    }

    public List<LeaderboardEntry> getLeaderboardEntries() {
        return new ArrayList<>(leaderboardEntries);
    }

    public boolean isReady() {
        return id != null && name != null
                && type != null && sortingType != null
                && !leaderboardEntries.isEmpty();
    }

}