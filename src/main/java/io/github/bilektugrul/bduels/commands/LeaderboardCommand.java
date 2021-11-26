package io.github.bilektugrul.bduels.commands;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.features.leaderboards.Leaderboard;
import io.github.bilektugrul.bduels.features.leaderboards.LeaderboardManager;
import io.github.bilektugrul.bduels.features.leaderboards.SortingType;
import io.github.bilektugrul.bduels.features.stats.StatisticType;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LeaderboardCommand implements CommandExecutor {

    private final BDuels plugin;
    private final LeaderboardManager leaderboardManager;
    private final List<String> adminSubCommands = new ArrayList<>(Arrays.asList("oluştur", "yenile", "hologram", "limit", "veri", "data", "sıralamatürü", "type"));

    public LeaderboardCommand(BDuels plugin) {
        this.plugin = plugin;
        this.leaderboardManager = plugin.getLeaderboardManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("bduels.leaderboards")) {
            Utils.noPermission(sender);
            return true;
        }

        if (args.length == 0) {
            wrongUsage(sender);
            return true;
        }

        if (adminSubCommands.contains(args[0])) {
            if (args.length == 1) {
                wrongUsage(sender);
                return true;
            }
            switch (args[0]) {
                case "oluştur":
                    createLeaderboard(sender, args);
                    return true;
                case "yenile":
                    refresh(sender, args);
                    return true;
                case "hologram":
                    setHologram(sender, args);
                    return true;
                case "limit":
                    setLimit(sender, args);
                    return true;
                case "veri":
                case "data":
                    setDataType(sender, args);
                    return true;
                case "sıralamatürü":
                case "type":
                    setSortType(sender, args);
                    return true;
            }
        }

        String leaderboardID = Utils.arrayToString(Arrays.copyOfRange(args, 0, args.length), sender, false, false);
        Leaderboard leaderboard = leaderboardManager.getFromName(leaderboardID);
        if (leaderboard == null) {
            sender.sendMessage(Utils.getMessage("leaderboards.not-found", sender));
            return true;
        }

        leaderboardManager.leaderboardToChatMessage(leaderboard, sender);
        return true;
    }

    private void createLeaderboard(CommandSender sender, String[] args) {
        String name = args[1];
        if (!leaderboardManager.isPresent(name)) {
            leaderboardManager.createLeaderboard(name);
            sender.sendMessage(Utils.getMessage("leaderboards.created", sender)
                    .replace("%leaderboard%", name));
        } else {
            sender.sendMessage(Utils.getMessage("leaderboards.not-created", sender)
                    .replace("%leaderboard%", name));
        }
    }

    private void refresh(CommandSender sender, String[] args) {
        boolean all = args[1].equalsIgnoreCase("hepsi");

        if (all) {
            leaderboardManager.sortEveryLeaderboard();
            sender.sendMessage(Utils.getMessage("leaderboards.refreshed-all", sender));
            return;
        }

        Leaderboard leaderboard = leaderboardManager.getFromID(args[1]);
        if (leaderboard == null) {
            sender.sendMessage(Utils.getMessage("leaderboards.not-found", sender));
            return;
        }

        leaderboardManager.sort(leaderboard);
        sender.sendMessage(Utils.getMessage("leaderboards.refreshed-one", sender)
                .replace("%leaderboard%", leaderboard.getName()));
    }

    private void setHologram(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.getMessage("only-players", sender));
            return;
        }

        String leaderboardID = args[1];
        Leaderboard leaderboard = leaderboardManager.getFromID(leaderboardID);
        if (leaderboard == null) {
            sender.sendMessage(Utils.getMessage("leaderboards.not-found", sender));
            return;
        }

        Player player = (Player) sender;
        leaderboard.createHologram(plugin, player.getLocation());
        leaderboardManager.save();
        leaderboardManager.updateHologram(leaderboard);
        player.sendMessage(Utils.getMessage("leaderboards.location-changed", player));
    }

    private void setDataType(CommandSender sender, String[] args) {
        if (args.length < 3) {
            wrongUsage(sender);
            return;
        }

        String leaderboardID = args[1];
        Leaderboard leaderboard = leaderboardManager.getFromID(leaderboardID);
        if (leaderboard == null) {
            sender.sendMessage(Utils.getMessage("leaderboards.not-found", sender));
            return;
        }

        StatisticType statisticType = StatisticType.getByShort(args[2]);
        if (statisticType == null) {
            wrongUsage(sender);
            return;
        }

        leaderboard.setType(statisticType);
        sender.sendMessage(Utils.getMessage("leaderboards.data-changed", sender)
                .replace("%leaderboard%", leaderboard.getName())
                .replace("%newdata%", Utils.getMessage("leaderboards.type-names." + leaderboard.getType().name())));
    }

    private void setLimit(CommandSender sender, String[] args) {
        if (args.length < 3) {
            wrongUsage(sender);
            return;
        }

        String leaderboardID = args[1];
        Leaderboard leaderboard = leaderboardManager.getFromID(leaderboardID);
        if (leaderboard == null) {
            sender.sendMessage(Utils.getMessage("leaderboards.not-found", sender));
            return;
        }

        int size = Integer.parseInt(args[2]);
        leaderboard.setMaxSize(size);
        sender.sendMessage(Utils.getMessage("leaderboards.size-changed", sender)
                .replace("%leaderboard%", leaderboard.getName())
                .replace("%newsize%", String.valueOf(size)));
    }

    private void wrongUsage(CommandSender sender) {
        if (sender.hasPermission("bduels.leaderboards.admin")) {
            sender.sendMessage(Utils.getMessage("leaderboards.command.wrong-usage-admin", sender)
                    .replace("%leaderboards%", leaderboardManager.getReadableLeaderboards()));
        } else {
            sender.sendMessage(Utils.getMessage("leaderboards.command.wrong-usage", sender)
                    .replace("%leaderboards%", leaderboardManager.getReadableLeaderboards()));
        }
    }

    private void setSortType(CommandSender sender, String[] args) {
        if (args.length < 3) {
            wrongUsage(sender);
            return;
        }

        String leaderboardID = args[1];
        Leaderboard leaderboard = leaderboardManager.getFromID(leaderboardID);
        if (leaderboard == null) {
            sender.sendMessage(Utils.getMessage("leaderboards.not-found", sender));
            return;
        }

        SortingType sortingType = SortingType.getByShort(args[2]);
        if (sortingType == null) {
            sender.sendMessage(Utils.getMessage("leaderboards.type-not-found", sender));
            return;
        }

        leaderboard.setSortingType(sortingType);
        sender.sendMessage(Utils.getMessage("leaderboards.sorting-type-changed", sender)
                .replace("%leaderboard%", leaderboard.getName())
                .replace("%newtype%", sortingType.name()));
    }

}