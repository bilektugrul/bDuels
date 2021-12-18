package io.github.bilektugrul.bduels.commands;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.arenas.ArenaManager;
import io.github.bilektugrul.bduels.features.leaderboards.LeaderboardManager;
import io.github.bilektugrul.bduels.utils.Utils;
import me.despical.commons.string.StringMatcher;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class BDuelsCommand implements CommandExecutor {

    private final BDuels plugin;
    private final ArenaManager arenaManager;

    private LeaderboardManager leaderboardManager;

    public BDuelsCommand(BDuels plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
        if (plugin.isLeaderboardManagerReady()) {
            this.leaderboardManager = plugin.getLeaderboardManager();
        }
        plugin.getCommand("bduels").setTabCompleter(new BDuelsCommandTabCompleter());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("bduels.admin")) {
            Utils.noPermission(sender);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Utils.getMessage("main-command.wrong-usage", sender));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload":
                plugin.reload();
                sender.sendMessage(Utils.getMessage("main-command.reloaded", sender));
                return true;
            case "save":
                save(sender, args);
                return true;
            default:
                sender.sendMessage(Utils.getMessage("main-command.wrong-usage", sender));
        }
        return true;
    }

    private void save(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.save(false);
            sender.sendMessage(Utils.getMessage("main-command.saved", sender));
            return;
        }

        switch (args[1]) {
            case "arena":
            case "arenas":
            case "arenalar":
                arenaManager.save();
                return;
            case "stats":
            case "stat":
            case "istatistik":
            case "istatistikler":
                if (!plugin.isDatabaseEnabled()) {
                    sender.sendMessage(Utils.getMessage("main-command.database-disabled", sender));
                } else if (plugin.saveAllUserStatistics(false)) {
                    sender.sendMessage(Utils.getMessage("main-command.saved-stats", sender));
                } else {
                    sender.sendMessage(Utils.getMessage("main-command.could-not-saved", sender));
                }
                return;
            case "leaderboards":
            case "sıralama":
            case "sıralamalar":
                boolean saved = false;
                if (!plugin.isLeaderboardManagerReady()) {
                    sender.sendMessage(Utils.getMessage("leaderboards.not-active", sender));
                } else {
                    saved = leaderboardManager.save();
                } if (saved) {
                    sender.sendMessage(Utils.getMessage("leaderboards.saved", sender));
                }
                return;
            case "all":
            case "hepsi":
            case "herşey":
                plugin.save(false);
                sender.sendMessage(Utils.getMessage("main-command.saved", sender));
                return;
            default:
                sender.sendMessage(Utils.getMessage("main-command.wrong-usage", sender));
        }
    }
    
    private static class BDuelsCommandTabCompleter implements TabCompleter {

        private static final List<String> defaultCompleters = new ArrayList<>(Arrays.asList("reload", "save"));
        private static final List<String> saveCompleters = new ArrayList<>(Arrays.asList("stats", "leaderboards", "all"));

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
            if (args[0].equalsIgnoreCase("save")) {
                return args.length == 2 ?
                        args[1].length() == 0
                                ? saveCompleters
                                : StringMatcher.match(args[1], saveCompleters).stream().map(StringMatcher.Match::getMatch).collect(Collectors.toList())
                        : Collections.emptyList();
            }
            return args.length == 1
                    ? args[0].length() == 0
                        ? defaultCompleters
                        : StringMatcher.match(args[0], defaultCompleters).stream().map(StringMatcher.Match::getMatch).collect(Collectors.toList())
                    : Collections.emptyList();
        }

    }

}