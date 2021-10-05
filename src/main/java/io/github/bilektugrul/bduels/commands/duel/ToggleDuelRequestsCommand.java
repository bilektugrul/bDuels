package io.github.bilektugrul.bduels.commands.duel;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.stats.StatisticType;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserManager;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ToggleDuelRequestsCommand implements CommandExecutor {

    private final UserManager userManager;

    public ToggleDuelRequestsCommand(BDuels plugin) {
        this.userManager = plugin.getUserManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.getMessage("only-players", sender));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bduels.togglerequests")) {
            Utils.noPermission(player);
            return true;
        }

        User user = userManager.getUser(player);
        int current = user.getStat(StatisticType.DUEL_REQUESTS);
        int newMode = current == 0 ? 1 : 0;
        user.setStat(StatisticType.DUEL_REQUESTS, newMode);

        String path = "request-toggle-command.";
        player.sendMessage(Utils.getMessage(path + "toggled", sender)
                .replace("%newmode%", Utils.getMessage(path + ".modes." + newMode, player)));

        return true;
    }

}
