package io.github.bilektugrul.bduels.commands.duel;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.DuelManager;
import io.github.bilektugrul.bduels.duels.DuelRequestProcess;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserManager;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DuelAcceptCommand implements CommandExecutor {

    private final BDuels plugin;
    private final DuelManager duelManager;
    private final UserManager userManager;

    private final List<String> timer = new ArrayList<>();

    public DuelAcceptCommand(BDuels plugin) {
        this.plugin = plugin;
        this.duelManager = plugin.getDuelManager();
        this.userManager = plugin.getUserManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.getMessage("only-players", sender));
            return true;
        }

        Player player = (Player) sender;
        User playerUser = userManager.getOrLoadUser(player);
        DuelRequestProcess process = playerUser.getRequestProcess();
        if (process == null) {
            player.sendMessage(Utils.getMessage("duel.no-request", player));
            return true;
        }

        String name = player.getName();
        if (timer.contains(name)) {
            if (!process.isRequestAccepted()) {
                duelManager.acceptDuelRequest(process, timer);
            }
            return true;
        }

        timer.add(name);
        player.sendMessage(Utils.getMessage("duel.use-again", player));
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> timer.remove(name), 200);
        return true;
    }

}