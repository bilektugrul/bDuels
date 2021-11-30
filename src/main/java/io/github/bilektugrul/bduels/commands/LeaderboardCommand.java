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
import java.util.concurrent.ExecutionException;

public class LeaderboardCommand implements CommandExecutor {

    private final BDuels plugin;
    private final LeaderboardManager leaderboardManager;
    private final List<String> adminSubCommands = new ArrayList<>(Arrays.asList("oluştur", "sil", "isim", "yenile", "hologram", "limit", "veri", "data", "sıralamatürü", "type"));

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

        if (adminSubCommands.contains(args[0]) && sender.hasPermission("bduels.leaderboards.admin")) {
            if (args.length == 1) {
                wrongUsage(sender);
                return true;
            }
            switch (args[0]) {
                case "oluştur":
                    createLeaderboard(sender, args);
                    return true;
                case "sil":
                    deleteLeaderboard(sender, args);
                    return true;
                case "isim":
                    setName(sender, args);
                    return true;
                case "yenile":
                    try {
                        refresh(sender, args);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    return true;
                case "hologram":
                    setHologram(sender, args);
                    return true;
                case "limit":
                    try {
                        setLimit(sender, args);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    return true;
                case "veri":
                case "data":
                    try {
                        setDataType(sender, args);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    return true;
                case "sıralamatürü":
                case "type":
                    try {
                        setSortType(sender, args);
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
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

    private void deleteLeaderboard(CommandSender sender, String[] args) {
        String name = args[1];
        if (leaderboardManager.isPresent(name)) {
            leaderboardManager.deleteLeaderboard(name);
            sender.sendMessage(Utils.getMessage("leaderboards.deleted", sender)
                    .replace("%leaderboard%", name));
        } else {
            sender.sendMessage(Utils.getMessage("leaderboards.not-found", sender)
                    .replace("%leaderboard%", name));
        }
    }

    private void setName(CommandSender sender, String[] args) {
        if (args.length == 2) {
            wrongUsage(sender);
            return;
        }

        String leaderboardID = args[1];
        Leaderboard leaderboard = leaderboardManager.getFromID(leaderboardID);
        if (leaderboard == null) {
            sender.sendMessage(Utils.getMessage("leaderboards.not-found", sender));
            return;
        }

        String leaderboardName = Utils.arrayToString(Arrays.copyOfRange(args, 2, args.length), sender, false, false);
        leaderboard.setName(leaderboardName);
        sender.sendMessage(Utils.getMessage("leaderboards.name-changed", sender)
                .replace("%newname%", leaderboardName)
                .replace("%leaderboard%", leaderboardID));
        if (leaderboard.isReady()) {
            leaderboardManager.updateHologram(leaderboard);
        }
    }

    private void refresh(CommandSender sender, String[] args) throws ExecutionException, InterruptedException {
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
        if (leaderboardManager.saveLeaderboard(leaderboard)) {
            leaderboardManager.updateHologram(leaderboard);
            player.sendMessage(Utils.getMessage("leaderboards.location-changed", player));
        } else {
            player.sendMessage(Utils.getMessage("leaderboards.not-ready", player));
        }
    }

    private void setDataType(CommandSender sender, String[] args) throws ExecutionException, InterruptedException {
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
        if (leaderboard.isReady()) {
            leaderboardManager.sort(leaderboard);
        }
    }

    private void setLimit(CommandSender sender, String[] args) throws ExecutionException, InterruptedException {
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
        if (leaderboard.isReady()) {
            leaderboardManager.sort(leaderboard);
        }
    }

    private void setSortType(CommandSender sender, String[] args) throws ExecutionException, InterruptedException {
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
        if (leaderboard.isReady()) {
            leaderboardManager.sort(leaderboard);
        }
    }

    private void wrongUsage(CommandSender sender) {
        if (sender.hasPermission("bduels.leaderboards.admin")) {
            sender.sendMessage(Utils.getMessage("leaderboards.command.wrong-usage-admin", sender)
                    .replace("%leaderboards%", leaderboardManager.getReadableLeaderboards())
                    .replace("%leaderboardids%", leaderboardManager.getReadableLeaderboardIDs()));
        } else {
            sender.sendMessage(Utils.getMessage("leaderboards.command.wrong-usage", sender)
                    .replace("%leaderboards%", leaderboardManager.getReadableLeaderboards()));
        }
    }

}