package io.github.bilektugrul.bduels.commands;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.features.leaderboards.Leaderboard;
import io.github.bilektugrul.bduels.features.leaderboards.LeaderboardManager;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class LeaderboardCommand implements CommandExecutor {

    private final LeaderboardManager leaderboardManager;

    public LeaderboardCommand(BDuels plugin) {
        this.leaderboardManager = plugin.getLeaderboardManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("bduels.leaderboards")) {
            Utils.noPermission(sender);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Utils.getMessage("leaderboards.command.wrong-usage", sender)
                    .replace("%leaderboards%", leaderboardManager.getReadableLeaderboards()));
            return true;
        }

        String name = Utils.arrayToString(Arrays.copyOfRange(args, 0, args.length), sender, false, false);
        Leaderboard leaderboard = leaderboardManager.getFromID(name);
        if (leaderboard == null) {
            sender.sendMessage(Utils.getMessage("leaderboards.not-found", sender));
            return true;
        }

        leaderboardManager.leaderboardToChatMessage(leaderboard, sender);
        return true;
    }

}