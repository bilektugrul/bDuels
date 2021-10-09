package io.github.bilektugrul.bduels.commands;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.features.leaderboards.Leaderboard;
import io.github.bilektugrul.bduels.features.leaderboards.LeaderboardManager;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
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
        if (plugin.isLeaderboardManagerReady()) this.leaderboardManager = plugin.getLeaderboardManager();
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
            case "refresh-leaderboard":
            case "refresh-leaderboards":
                refreshLeaderboard(sender, args);
                return true;
            case "leaderboardholo":
                setLeaderboardHologramLocation(sender, args);
                return true;
            case "save":
                save(sender, args);
                return true;
            default:
                sender.sendMessage(Utils.getMessage("main-command.wrong-usage", sender));
        }
        return true;
    }

    private void refreshLeaderboard(CommandSender sender, String[] args) {
        if (!plugin.isLeaderboardManagerReady()) return;
        boolean all = true;
        String toRefresh = "";

        if (args.length >= 2) {
            all = false;
            toRefresh = Utils.arrayToString(Arrays.copyOfRange(args, 1, args.length), sender, false, false);
        }

        if (all) {
            leaderboardManager.sortEveryLeaderboard();
            sender.sendMessage(Utils.getMessage("leaderboards.refreshed-all", sender));
        } else {
            Leaderboard leaderboard = leaderboardManager.getFromID(toRefresh);
            if (leaderboard == null) {
                sender.sendMessage(Utils.getMessage("leaderboards.not-found", sender));
                return;
            }
            leaderboardManager.sort(leaderboard);
            sender.sendMessage(Utils.getMessage("leaderboards.refreshed-one", sender)
                    .replace("%leaderboard%", leaderboard.getName()));
        }
    }

    private void setLeaderboardHologramLocation(CommandSender sender, String[] args) {
        if (!plugin.isLeaderboardManagerReady()) return;
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.getMessage("only-players", sender));
            return;
        }
        Player player = (Player) sender;
        if (args.length < 2) {
            player.sendMessage(Utils.getMessage("leaderboards.type-leaderboard-name", player));
            return;
        }
        String leaderboardName = Utils.arrayToString(Arrays.copyOfRange(args, 1, args.length), sender, false, false);
        Leaderboard leaderboard = leaderboardManager.getFromID(leaderboardName);
        if (leaderboard == null) {
            player.sendMessage(Utils.getMessage("leaderboards.not-found", player));
            return;
        }
        leaderboard.createHologram(plugin, player.getLocation());
        leaderboardManager.save();
        leaderboardManager.updateHologram(leaderboard);
        player.sendMessage(Utils.getMessage("leaderboards.location-changed", player));
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
                if (!plugin.isDatabaseEnabled()) sender.sendMessage(Utils.getMessage("main-command.database-disabled", sender));
                else if (plugin.saveAllUserStatistics()) sender.sendMessage(Utils.getMessage("main-command.saved-stats", sender));
                else sender.sendMessage(Utils.getMessage("main-command.could-not-saved", sender));
                return;
            case "leaderboards":
            case "sıralama":
            case "sıralamalar":
                boolean saved = false;
                if (!plugin.isLeaderboardManagerReady()) sender.sendMessage(Utils.getMessage("leaderboards.not-active", sender));
                else saved = leaderboardManager.save();
                if (saved) sender.sendMessage(Utils.getMessage("leaderboards.saved", sender));
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
            if (args.length > 1 && args[0].equalsIgnoreCase("save"))
                return Arrays.asList("stats", "leaderboards", "all");
            return Arrays.asList("reload", "refresh-leaderboard", "leaderboardholo", "save", "save");
        }

    }

}