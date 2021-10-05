package io.github.bilektugrul.bduels.stats;

public enum StatisticType {
    WINS("wins", "win"),
    LOSES("loses", "lose"),
    TOTAL_EARNED_MONEY("total_earned_money", "money"),
    TOTAL_EARNED_ITEM("total_earned_item", "item");

    private final String name;
    private final String shortName;
    private final boolean persistent;

    StatisticType(String name, String shortName) {
        this(name, shortName, true);
    }

    StatisticType(String name, String shortName, boolean persistent) {
        this.name = name;
        this.shortName = shortName;
        this.persistent = persistent;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public static StatisticType getByShort(String shortName) {
        for (StatisticType value : values()) {
            if (value.getShortName().equalsIgnoreCase(shortName)) {
                return value;
            }
        }
        return null;
    }
}
