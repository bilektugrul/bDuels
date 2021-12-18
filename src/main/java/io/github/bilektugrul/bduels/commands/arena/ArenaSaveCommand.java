package io.github.bilektugrul.bduels.commands.arena;

import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.commands.arena.base.SubCommand;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ArenaSaveCommand extends SubCommand {

    public ArenaSaveCommand(String name, String... aliases) {
        super(name, aliases);
    }

    @Override
    public String getPossibleArguments() {
        return null;
    }

    @Override
    public int getMinimumArguments() {
        return 1;
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) throws CommandException {
        String arenaName = args[0];
        Arena arena = arenaManager.getArena(arenaName);

        if (arena == null) {
            sender.sendMessage(Utils.getMessage("arenas.not-found", sender));
            return;
        }

        if (arena.isReady()) {
            arenaManager.save(arena, false);
            sender.sendMessage(Utils.getMessage("arenas.saved-arena", sender)
                    .replace("%arena%", arena.getName()));
        } else {
            sender.sendMessage(Utils.getMessage("arenas.not-ready", sender)
                    .replace("%arena%", arena.getName()));
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