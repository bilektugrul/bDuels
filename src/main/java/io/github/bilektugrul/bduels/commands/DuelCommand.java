package io.github.bilektugrul.bduels.commands;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.duels.DuelRequestProcess;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserManager;
import io.github.bilektugrul.bduels.utils.Utils;
import me.despical.commons.util.Collections;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class DuelCommand implements CommandExecutor {

    private final BDuels plugin;
    private final UserManager userManager;
    private final DuelManager duelManager;

    public DuelCommand(BDuels plugin) {
        this.plugin = plugin;
        this.userManager = plugin.getUserManager();
        this.duelManager = plugin.getDuelManager();
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

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            DuelRequestProcess process = playerUser.getRequestProcess();
            if (process != null) {
                if (Collections.contains(opponent, process.getPlayers()) && !process.isRequestAccepted()) {
                    duelManager.cancel(process);
                    player.sendMessage(Utils.getMessage("duel.request-cancelled", player)
                            .replace("%user%", opponentPlayer.getName()));
                    opponentPlayer.sendMessage(Utils.getMessage("duel.request-cancelled", opponentPlayer)
                            .replace("%user%", opponentPlayer.getName()));
                }
            }
        }, 200);
        return true;
    }

}
