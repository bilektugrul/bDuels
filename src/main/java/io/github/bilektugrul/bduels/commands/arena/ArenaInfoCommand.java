package io.github.bilektugrul.bduels.commands.arena;

import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.commands.arena.base.SubCommand;
import io.github.bilektugrul.bduels.utils.Utils;
import me.despical.commons.serializer.LocationSerializer;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ArenaInfoCommand extends SubCommand {

    public ArenaInfoCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public String getPossibleArguments() {
        return null;
    }

    @Override
    public int getMinimumArguments() {
        return 0;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) throws CommandException {
        String arenaName = args[0];
        Arena arena = arenaManager.getArena(arenaName);

        if (arena == null) {
            sender.sendMessage(Utils.getMessage("arenas.not-found", sender));
            return;
        }

        String message = Utils.getMessage("arenas.info", sender)
                .replace("%arena%", arena.getName())
                .replace("%p1loc%", LocationSerializer.toString(arena.getPlayerLocation()))
                .replace("%p2loc%", LocationSerializer.toString(arena.getOpponentLocation()))
                .replace("%edge1loc%", LocationSerializer.toString(arena.getEdge()))
                .replace("%edge2loc%", LocationSerializer.toString(arena.getOtherEdge()))
                .replace("%state%", Utils.getMessage("arenas.states." + arena.getState().name()));
        sender.sendMessage(message);
    }

    @Override
    public List<String> getTutorial() {
        return null;
    }

    @Override
    public CommandType getType() {
        return CommandType.HIDDEN;
    }

    @Override
    public SenderType getSenderType() {
        return SenderType.PLAYER;
    }

}