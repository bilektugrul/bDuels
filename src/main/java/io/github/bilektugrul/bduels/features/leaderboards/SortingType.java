package io.github.bilektugrul.bduels.features.leaderboards;

public enum SortingType {

    HIGH_TO_LOW("düz"),
    LOW_TO_HIGH("ters");

    private final String shortName;

    SortingType(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public static SortingType getByShort(String shortName) {
        for (SortingType sortingType : values()) {
            if (sortingType.getShortName().equalsIgnoreCase(shortName)) {
                return sortingType;
            }
        }
        return null;
    }

}