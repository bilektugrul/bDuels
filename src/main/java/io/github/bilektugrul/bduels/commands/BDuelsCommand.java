package io.github.bilektugrul.bduels.commands;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BDuelsCommand implements CommandExecutor {

    private final UserManager userManager;
    private final DuelManager duelManager;

    public BDuelsCommand(BDuels bDuels) {
        this.userManager = bDuels.getUserManager();
        this.duelManager = bDuels.getDuelManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("sadece oyuncular kardeşim");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sender.sendMessage("player gir la");
            return true;
        }

        Player opponentPlayer = Bukkit.getPlayer(args[0]);

        if (opponentPlayer == null) {
            sender.sendMessage("böyle bi oyuncu yok olm");
            return true;
        }

        if (opponentPlayer.equals(player)) {
            player.sendMessage("kendine düello atamazsın");
            return true;
        }

        User playerUser = userManager.getUser(player);
        User opponent = userManager.getUser(opponentPlayer);
        duelManager.sendDuelRequest(playerUser, opponent);

        return true;
    }

}
