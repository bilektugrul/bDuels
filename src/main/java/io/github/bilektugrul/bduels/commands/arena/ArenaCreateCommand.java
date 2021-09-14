package io.github.bilektugrul.bduels.commands.arena;

import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.arenas.ArenaManager;
import io.github.bilektugrul.bduels.commands.base.SubCommand;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

//TODO: TEST EDİLECEK / BİTİRİLECEK
public class ArenaCreateCommand extends SubCommand {

    private final ArenaManager arenaManager;

    public ArenaCreateCommand(String name) {
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
    public void execute(CommandSender sender, String[] args) throws CommandException {
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
    public CommandType getType() {
        return CommandType.GENERIC;
    }

    @Override
    public SenderType getSenderType() {
        return SenderType.PLAYER;
    }
}
