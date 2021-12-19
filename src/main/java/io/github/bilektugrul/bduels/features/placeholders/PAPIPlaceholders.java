package io.github.bilektugrul.bduels.features.placeholders;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.Duel;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.duels.DuelRequestProcess;
import io.github.bilektugrul.bduels.features.leaderboards.Leaderboard;
import io.github.bilektugrul.bduels.features.leaderboards.LeaderboardManager;
import io.github.bilektugrul.bduels.features.stats.StatisticType;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserManager;
import io.github.bilektugrul.bduels.utils.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PAPIPlaceholders extends PlaceholderExpansion {

    private final BDuels plugin;
    private final CustomPlaceholderManager placeholderManager;
    private final UserManager userManager;
    private final DuelManager duelManager;
    private final LeaderboardManager leaderboardManager;

    public PAPIPlaceholders(BDuels plugin) {
        this.plugin = plugin;
        this.placeholderManager = plugin.getPlaceholderManager();
        this.userManager = plugin.getUserManager();
        this.duelManager = plugin.getDuelManager();
        this.leaderboardManager = plugin.getLeaderboardManager();
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
        return "bduels";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (identifier.contains("leaderboard") && plugin.isLeaderboardManagerReady() && identifier.length() >= 13) {
            String id = StringUtils.substringBetween(identifier, "\"");
            Leaderboard leaderboard = leaderboardManager.getFromID(id);
            if (leaderboard == null) {
                return "";
            }

            int place = Integer.parseInt(StringUtils.substringBetween(identifier, "'"));
            place--;

            if (identifier.endsWith("isim")) {
                return leaderboard.getLeaderboardEntries().get(place).getName();
            } else if (identifier.endsWith("stat")) {
                return String.valueOf(leaderboard.getLeaderboardEntries().get(place).getValue());
            }

            return "";
        }

        if (identifier.contains("custom")) {
            String name = identifier.substring(identifier.indexOf("custom_") + 7);
            return placeholderManager.getPlaceholder(name).getValue();
        }

        if (identifier.equalsIgnoreCase("matches")) {
            return String.valueOf(duelManager.getOngoingDuels().size());
        }

        if (identifier.equalsIgnoreCase("players")) {
            return String.valueOf(duelManager.getOngoingDuels().size() * 2);
        }

        boolean userRequired = identifier.contains("stat") || identifier.contains("opponent") || identifier.contains("arena");
        if (!userRequired) {
            return "";
        }

        User user = userManager.getOrLoadUser(player);
        Duel duel = user.getDuel();

        if (identifier.contains("state_raw")) {
            return user.getState().name();
        }

        if (identifier.contains("state")) {
            return Utils.getMessage("user-states." + user.getState().name(), player);
        }

        if (identifier.contains("stat")) {
            String statName = identifier.substring(identifier.indexOf("stat_") + 5);
            return String.valueOf(user.getStat(StatisticType.getByShort(statName)));
        }

        if (identifier.contains("opponent")) {
            DuelRequestProcess process = user.getRequestProcess();
            if (process != null) {
                return process.getOpponentOf(user).getName();
            } if (duel != null) {
                return duel.getOpponentOf(user).getName();
            }
            return "";
        }

        if (identifier.contains("arena")) {
            if (duel != null) {
                return duel.getArena().getName();
            }
        }

        return "";
    }

}