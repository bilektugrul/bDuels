package io.github.bilektugrul.bduels.placeholders;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.users.UserManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

//TODO: OYNANAN MAÇ SAYISI, RAKİP İSMİ, ARENA İSMİ VS. PLACEHOLDERLERİ
public class PAPIPlaceholders extends PlaceholderExpansion {

    private final BDuels plugin;
    private final CustomPlaceholderManager placeholderManager;
    private final UserManager userManager;

    public PAPIPlaceholders(BDuels plugin) {
        this.plugin = plugin;
        this.placeholderManager = plugin.getPlaceholderManager();
        this.userManager = plugin.getUserManager();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "sst";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {

        if (identifier.contains("custom")) {
            String name = identifier.substring(identifier.indexOf("custom_") + 7);
            return placeholderManager.getPlaceholder(name).getValue();

        }
        return null;
    }

}