package io.github.bilektugrul.bduels.commands;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserManager;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {

    private final UserManager userManager;
    private final DuelManager duelManager;

    public DuelCommand(BDuels bDuels) {
        this.userManager = bDuels.getUserManager();
        this.duelManager = bDuels.getDuelManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.getMessage("only-players", sender));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sender.sendMessage(Utils.getMessage("duel.enter-player", sender));
            return true;
        }

        Player opponentPlayer = Bukkit.getPlayer(args[0]);

        if (opponentPlayer == null) {
            sender.sendMessage(Utils.getMessage("duel.player-not-found", sender));
            return true;
        }

        if (opponentPlayer.equals(player)) {
            sender.sendMessage(Utils.getMessage("duel.not-yourself", sender));
            return true;
        }

        User playerUser = userManager.getUser(player);
        User opponent = userManager.getUser(opponentPlayer);
        duelManager.sendDuelRequest(playerUser, opponent);

        return true;
    }

}
