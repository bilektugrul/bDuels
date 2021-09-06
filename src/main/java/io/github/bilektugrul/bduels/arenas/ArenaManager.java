package io.github.bilektugrul.bduels.arenas;

import java.util.HashSet;
import java.util.Set;

public class ArenaManager {

    public Set<Arena> arenas = new HashSet<>();

    public Arena findNextEmptyArena() {
        for (Arena arena : arenas) {
            if (arena.getState() == ArenaState.EMPTY) {
                return arena;
            }
        }
        return null;
    }

}
