package io.github.bilektugrul.bduels.commands;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.features.leaderboards.LeaderboardManager;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BDuelsCommand implements CommandExecutor {

    private final BDuels plugin;
    private LeaderboardManager leaderboardManager;

    public BDuelsCommand(BDuels plugin) {
        this.plugin = plugin;
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
            plugin.save();
            sender.sendMessage(Utils.getMessage("main-command.saved", sender));
            return;
        }

        switch (args[1]) {
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
                plugin.save();
                sender.sendMessage(Utils.getMessage("main-command.saved", sender));
                return;
            default:
                sender.sendMessage(Utils.getMessage("main-command.wrong-usage", sender));
        }
    }
    
    private static class BDuelsCommandTabCompleter implements TabCompleter {

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
            if (args.length > 1 && args[0].equalsIgnoreCase("save")) {
                return Arrays.asList("stats", "leaderboards", "all");
            }
            return Arrays.asList("reload", "save");
        }

    }

}