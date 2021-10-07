package io.github.bilektugrul.bduels.features.placeholders;

import io.github.bilektugrul.bduels.BDuels;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;

public class CustomPlaceholderManager {

    private final BDuels plugin;
    private final Set<CustomPlaceholder> placeholderList = new HashSet<>();

    public CustomPlaceholderManager(BDuels plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        placeholderList.clear();
        FileConfiguration config = plugin.getConfig();
        for (String key : config.getConfigurationSection("custom-placeholders").getKeys(false)) {
            CustomPlaceholder placeholder = new CustomPlaceholder(key, colored(config.getString("custom-placeholders." + key)));
            placeholderList.add(placeholder);
        }
    }

    public String replacePlaceholders(String in) {
        for (CustomPlaceholder placeholder : placeholderList) {
            in = in.replace("%" + placeholder.getName() + "%", placeholder.getValue());
        }
        return in;
    }

    public String colored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public Set<CustomPlaceholder> getPlaceholderList() {
        return new HashSet<>(placeholderList);
    }

    public CustomPlaceholder getPlaceholder(String name) {
        for (CustomPlaceholder placeholder : placeholderList) {
            if (placeholder.getName().equalsIgnoreCase(name)) {
                return placeholder;
            }
        }
        return null;
    }

}
