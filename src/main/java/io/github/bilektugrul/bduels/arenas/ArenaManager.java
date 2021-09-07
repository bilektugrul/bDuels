package io.github.bilektugrul.bduels.arenas;

import java.util.HashSet;
import java.util.Set;

public class ArenaManager {

    public Set<Arena> arenas = new HashSet<>();

    public ArenaManager() {
        loadArenas();
    }

    public void loadArenas() {
    }

    public boolean isAnyArenaAvailable() {
        return findNextEmptyArenaIfPresent() != null;
    }

    public Arena findNextEmptyArenaIfPresent() {
        for (Arena arena : arenas) {
            if (arena.getState() == ArenaState.EMPTY) {
                return arena;
            }
        }
        return null;
    }

}
