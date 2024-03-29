package io.github.bilektugrul.bduels.commands.arena;

import io.github.bilektugrul.bduels.commands.arena.base.SubCommand;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ArenaDeleteCommand extends SubCommand {

    public ArenaDeleteCommand(String name) {
        super(name);
    }

    @Override
    public String getPossibleArguments() {
        return "";
    }

    @Override
    public int getMinimumArguments() {
        return 1;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) throws CommandException {
        String arenaName = args[0];
        if (!arenaManager.isPresent(arenaName)) {
            sender.sendMessage(Utils.getMessage("arenas.not-found", sender));
            return;
        }

        if (arenaManager.deleteArena(arenaName)) {
            sender.sendMessage(Utils.getMessage("arenas.deleted", sender)
                    .replace("%arena%", arenaName));
        } else {
            sender.sendMessage(Utils.getMessage("arenas.could-not-deleted", sender)
                    .replace("%arena%", arenaName));
        }
    }

    @Override
    public List<String> getTutorial() {
        return null;
    }

    @Override
    public SenderType getSenderType() {
        return SenderType.PLAYER;
    }

}