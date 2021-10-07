package io.github.bilektugrul.bduels.features.leaderboards;

public class LeaderboardEntry {

    private final String name;
    private final Integer value;

    public LeaderboardEntry(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Integer getValue() {
        return value;
    }

}