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
    private final String name;
    private final StatisticType type;
    private final SortingType sortingType;
    private final int maxSize;
    private Hologram hologram;
    private List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();

    public Leaderboard(String id, String name, StatisticType type, SortingType sortingType, int maxSize) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.sortingType = sortingType;
        this.maxSize = maxSize;
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
        if (this.hologram != null) this.hologram.delete();
        this.hologram = HologramsAPI.createHologram(plugin, location);
        return this.hologram;
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

}