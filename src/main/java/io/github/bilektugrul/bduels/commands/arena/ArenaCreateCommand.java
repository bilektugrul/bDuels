package io.github.bilektugrul.bduels.commands.arena;

import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.commands.arena.base.SubCommand;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ArenaCreateCommand extends SubCommand {

    public ArenaCreateCommand(String name, String... aliases) {
        super(name, aliases);
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
        Arena arena = arenaManager.registerArena(arenaName);
        if (arena != null) {
            sender.sendMessage(Utils.getMessage("arenas.created", sender)
                    .replace("%arena%", arenaName));
        } else {
            sender.sendMessage(Utils.getMessage("arenas.already-exist", sender)
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