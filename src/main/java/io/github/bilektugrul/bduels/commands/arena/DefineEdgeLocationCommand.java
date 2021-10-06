package io.github.bilektugrul.bduels.commands.arena;

import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.commands.arena.base.SubCommand;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DefineEdgeLocationCommand extends SubCommand {

    public DefineEdgeLocationCommand(String name, String aliases) {
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
        int whichEdge = Integer.parseInt(label.split("")[4]);

        Arena arena = arenaManager.getArena(arenaName);
        if (arena == null) {
            sender.sendMessage(Utils.getMessage("arenas.not-found", sender)
                    .replace("%arena%", arenaName));
            return;
        }

        Player player = (Player) sender;
        Location location = player.getLocation();

        switch (whichEdge) {
            case 1:
                arena.setEdge(location);
                break;
            case 2:
                arena.setOtherEdge(location);
                break;
        }
        sender.sendMessage(Utils.getMessage("arenas.edge-location-set", sender)
                .replace("%no%", String.valueOf(whichEdge))
                .replace("%arena%", arenaName));
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
