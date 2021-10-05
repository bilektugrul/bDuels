package io.github.bilektugrul.bduels.placeholders;

public class CustomPlaceholder {

    private final String name, value;

    public CustomPlaceholder(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
