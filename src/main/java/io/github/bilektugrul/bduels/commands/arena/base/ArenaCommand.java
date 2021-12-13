//Copyright (C) 2020 Despical
package io.github.bilektugrul.bduels.commands.arena.base;

import io.github.bilektugrul.bduels.commands.arena.*;
import io.github.bilektugrul.bduels.utils.Utils;
import me.despical.commons.string.StringMatcher;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class ArenaCommand implements CommandExecutor {

    private final List<SubCommand> subCommands = new ArrayList<>();

    public ArenaCommand() {
        registerSubCommand(new ArenaDeleteCommand("sil"));
        registerSubCommand(new ArenaCreateCommand("oluştur", "yarat"));
        registerSubCommand(new DefinePlayerLocationCommand("p1", "p2"));
        registerSubCommand(new DefineEdgeLocationCommand("edge1", "edge2"));
        registerSubCommand(new ArenaListCommand("list", "liste"));
        registerSubCommand(new ArenaTeleportCommand("teleport", "ışınlan"));
        registerSubCommand(new ArenaInfoCommand("info", "bilgi"));
    }

    public void registerSubCommand(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.getMessage("only-players", sender));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bduels.arena.admin")) {
            Utils.noPermission(player);
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Utils.getMessage("arenas.help-message", player));
            return true;
        }

        String toExecute = args[0];
        for (SubCommand subCommand : subCommands) {
            if (subCommand.isValidTrigger(toExecute)) {
                if (!subCommand.hasPermission(sender)) {
                    Utils.noPermission(sender);
                    return true;
                }

                if (args.length - 1 >= subCommand.getMinimumArguments()) {
                    try {
                        subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length), toExecute);
                    } catch (CommandException e) {
                        sender.sendMessage(ChatColor.RED + e.getMessage());
                    }
                } else {
                    sender.sendMessage(Utils.getMessage("wrong-usage", sender)
                            .replace("%usage%", "/arena " + toExecute + " <arena>"));
                }

                return true;
            }
        }

        List<StringMatcher.Match> matches = StringMatcher.match(args[0], subCommands.stream().map(SubCommand::getName).collect(Collectors.toList()));

        if (!matches.isEmpty()) {
            sender.sendMessage(Utils.getMessage("did-you-mean").replace("%command%", label + " " + matches.get(0).getMatch()));
        }

        return true;
    }

    public List<SubCommand> getSubCommands() {
        return new ArrayList<>(subCommands);
    }

}