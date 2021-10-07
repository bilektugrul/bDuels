package io.github.bilektugrul.bduels.commands.duel;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.features.stats.StatisticType;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserManager;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DuelStatsCommand implements CommandExecutor {

    private final UserManager userManager;

    public DuelStatsCommand(BDuels plugin) {
        this.userManager = plugin.getUserManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player statPlayer = args.length > 0 ? Bukkit.getPlayer(args[0]) : sender instanceof Player ? (Player) sender : null;

        if (statPlayer == null) {
            sender.sendMessage(Utils.getMessage("player-not-found", sender));
            return true;
        }

        if ((!statPlayer.equals(sender) && Utils.getBoolean("other-stats-require-permission")) && !sender.hasPermission("bduels.otherstats")) {
            sender.sendMessage(Utils.getMessage("stats-command.cannot-check", sender));
            return true;
        }

        User statUser = userManager.getUser(statPlayer);
        String message = Utils.getMessage("stats-command.command", sender)
                .replace("%statplayer%", statPlayer.getName())
                .replace("%totalmatches%", String.valueOf(statUser.getStat(StatisticType.TOTAL_MATCHES)))
                .replace("%wins%", String.valueOf(statUser.getStat(StatisticType.WINS)))
                .replace("%loses%", String.valueOf(statUser.getStat(StatisticType.LOSES)))
                .replace("%money%", String.valueOf(statUser.getStat(StatisticType.TOTAL_EARNED_MONEY)))
                .replace("%lostmoney%", String.valueOf(statUser.getStat(StatisticType.TOTAL_LOST_MONEY)))
                .replace("%item%", String.valueOf(statUser.getStat(StatisticType.TOTAL_EARNED_ITEM)))
                .replace("%lostitem%", String.valueOf(statUser.getStat(StatisticType.TOTAL_LOST_ITEM)))
                .replace("%requests%", Utils.getMessage("request-toggle-command.modes." + statUser.getStat(StatisticType.DUEL_REQUESTS), sender));
        sender.sendMessage(message);

        return true;
    }

}
