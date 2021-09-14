package io.github.bilektugrul.bduels.commands.arena;

import io.github.bilektugrul.bduels.arenas.ArenaManager;
import io.github.bilektugrul.bduels.commands.base.SubCommand;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ArenaDeleteCommand extends SubCommand {

    private final ArenaManager arenaManager;

    public ArenaDeleteCommand(String name) {
        super(name);
        this.arenaManager = plugin.getArenaManager();
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
        boolean deleted = arenaManager.deleteArena(arenaName);
        if (deleted) {
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
    public CommandType getType() {
        return CommandType.GENERIC;
    }

    @Override
    public SenderType getSenderType() {
        return SenderType.PLAYER;
    }

}
