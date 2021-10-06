package io.github.bilektugrul.bduels.stats;

public enum StatisticType {

    TOTAL_MATCHES("total_matches", "matches"),
    WINS("wins", "win"),
    LOSES("loses", "lose"),
    TOTAL_EARNED_MONEY("total_earned_money", "money"),
    TOTAL_LOST_MONEY("total_lost_money", "lost_money"),
    TOTAL_EARNED_ITEM("total_earned_item", "item"),
    TOTAL_LOST_ITEM("total_lost_item", "lost_item"),
    DUEL_REQUESTS("duel_requests", "requests", 1);

    private final String name;
    private final String shortName;
    private final int defaultValue;
    private final boolean persistent;

    StatisticType(String name, String shortName) {
        this(name, shortName, 0, true);
    }

    StatisticType(String name, String shortName, int defaultValue) {
        this(name, shortName, defaultValue, true);
    }

    StatisticType(String name, String shortName, int defaultValue, boolean persistent) {
        this.name = name;
        this.shortName = shortName;
        this.defaultValue = defaultValue;
        this.persistent = persistent;
    }

    public String getName() {
        return name;
    }

    public int getDefaultValue() {
        return defaultValue;
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
