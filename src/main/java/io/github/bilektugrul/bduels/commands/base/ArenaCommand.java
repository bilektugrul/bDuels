//Copyright (C) 2020 Despical
package io.github.bilektugrul.bduels.commands.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.commands.arena.ArenaCreateCommand;
import io.github.bilektugrul.bduels.utils.Utils;
import me.despical.commons.string.StringMatcher;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 22.06.2020
 */
public class ArenaCommand implements CommandExecutor {

    private final BDuels plugin;

    private final List<SubCommand> subCommands = new ArrayList<>();

    public ArenaCommand(BDuels plugin) {
        this.plugin = plugin;
        registerSubCommand(new ArenaCreateCommand("oluştur"));
    }

    public void registerSubCommand(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    public List<SubCommand> getSubCommands() {
        return new ArrayList<>(subCommands);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.getMessage("only-players", sender));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bduels.arena.admin")) {
            Utils.noPermission(player);
            return true;
        }

        if (args.length <= 1) {
            player.sendMessage(Utils.getMessage("arenas.help-message", player));
            return true;
        }

        for (SubCommand subCommand : subCommands) {
            if (subCommand.isValidTrigger(args[0])) {
                if (!subCommand.hasPermission(sender)) {
                    Utils.noPermission(sender);
                    return true;
                }

                if (subCommand.getSenderType() == SubCommand.SenderType.PLAYER && !(sender instanceof Player)) {
                    sender.sendMessage(Utils.getMessage("only-players", sender));
                    return false;
                }

                if (args.length - 1 >= subCommand.getMinimumArguments()) {
                    try {
                        subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
                    } catch (CommandException e) {
                        sender.sendMessage(ChatColor.RED + e.getMessage());
                    }
                } else {
                    if (subCommand.getType() == SubCommand.CommandType.GENERIC) {
                        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " " + subCommand.getName() + " " + (subCommand.getPossibleArguments().length() > 0 ? subCommand.getPossibleArguments() : ""));
                    }
                }

                return true;
            }
        }

        List<StringMatcher.Match> matches = StringMatcher.match(args[0], subCommands.stream().map(SubCommand::getName).collect(Collectors.toList()));

        if (!matches.isEmpty()) {
            return true;
        }

        return true;
    }
}
