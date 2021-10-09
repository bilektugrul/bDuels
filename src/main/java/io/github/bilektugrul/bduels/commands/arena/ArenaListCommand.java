package io.github.bilektugrul.bduels.commands.arena;

import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.arenas.ArenaState;
import io.github.bilektugrul.bduels.commands.arena.base.SubCommand;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

public class ArenaListCommand extends SubCommand {

    public ArenaListCommand(String name, String... aliases) {
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
        Set<Arena> arenas = arenaManager.getArenas();

        long usedAmount = arenas.stream()
                .filter(arena -> arena.getState() != ArenaState.EMPTY)
                .count();
        int totalAmount = arenas.size();
        long emptyAmount = totalAmount - usedAmount;

        String listMessage = Utils.getMessage("arenas.list.start-message", sender)
                .replace("%usedamount%", String.valueOf(usedAmount))
                .replace("%emptyamount%", String.valueOf(emptyAmount))
                .replace("%totalamount%", String.valueOf(totalAmount)
        );
        sender.sendMessage(listMessage);

        String arenaFormat = Utils.getMessage("arenas.list.arena-format", sender);
        for (Arena arena : arenas) {
            sender.sendMessage(arenaFormat
                    .replace("%arena%", arena.getName())
                    .replace("%state%", Utils.getMessage("arenas.states." + arena.getState().name()))
            );
        }
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