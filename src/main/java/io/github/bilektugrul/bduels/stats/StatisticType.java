package io.github.bilektugrul.bduels.stats;

public enum StatisticType {
    WINS("wins"),
    LOSES("loses"),
    TOTAL_EARNED_MONEY("total_earned_money"),
    TOTAL_EARNED_ITEM("total_earned_item");

    private final String name;
    private final boolean persistent;

    StatisticType(String name) {
        this(name, true);
    }

    StatisticType(String name, boolean persistent) {
        this.name = name;
        this.persistent = persistent;
    }

    public String getName() {
        return name;
    }

    public boolean isPersistent() {
        return persistent;
    }
}
