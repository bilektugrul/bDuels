package io.github.bilektugrul.bduels.commands.arena;

import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.commands.arena.base.SubCommand;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ArenaTeleportCommand extends SubCommand {

    public ArenaTeleportCommand(String name, String... aliases) {
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
        Arena arena = arenaManager.getArena(args[0]);
        if (arena == null) {
            sender.sendMessage(Utils.getMessage("arenas.not-found", sender));
            return;
        }

        Player player = (Player) sender;
        player.teleport(arena.getPlayerLocation());
        player.sendMessage(Utils.getMessage("arenas.teleported", player)
                .replace("%arena%", arena.getName()));
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