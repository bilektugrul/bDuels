package io.github.bilektugrul.bduels.features.leaderboards;

public class LeaderboardEntry {

    private final String name;
    private final int value;

    public LeaderboardEntry(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

}