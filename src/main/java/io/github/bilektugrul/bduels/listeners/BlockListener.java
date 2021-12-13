package io.github.bilektugrul.bduels.listeners;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.duels.Duel;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFormEvent;

public class BlockListener extends ListenerAdapter {

    public BlockListener(BDuels plugin) {
        super(plugin);
    }

    @EventHandler
    public void onForm(BlockFormEvent e) {
        System.out.println(e.getBlock().getType().name());
        Location location = e.getBlock().getLocation();
        Arena blockArena = null;
        for (Arena arena : arenaManager.getArenas()) {
            if (arena.isInArea(location)) {
                blockArena = arena;
                break;
            }
        }

        if (blockArena == null) {
            return;
        }

        for (Duel duel : duelManager.getOngoingDuels()) {
            if (duel.getArena().equals(blockArena)) {
                duel.addPlacedBlockLocation(location);
                return;
            }
        }
    }

}
