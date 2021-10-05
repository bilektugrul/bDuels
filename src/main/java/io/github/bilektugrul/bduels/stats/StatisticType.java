package io.github.bilektugrul.bduels.stats;

public enum StatisticType {

    WINS("wins", "win"),
    LOSES("loses", "lose"),
    TOTAL_EARNED_MONEY("total_earned_money", "money"),
    TOTAL_EARNED_ITEM("total_earned_item", "item"),
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
