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

    public boolean isPresent(String name) {
        for (Arena arena : arenas) {
            if (arena.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public Arena registerArena(String name) {
        if (!isPresent(name)) {
            Arena arena = new Arena(name);
            arenas.add(arena);
            return arena;
        }
        return null;
    }

    public boolean registerArena(Arena arena) {
        if (!isPresent(arena.getName())) {
            arenas.add(arena);
            return true;
        }
        return false;
    }

    public boolean deleteArena(String name) {
        return arenas.removeIf(arena -> arena.getName().equalsIgnoreCase(name) && arena.getState() == ArenaState.EMPTY);
    }

    public Arena getArena(String name) {
        for (Arena arena : arenas) {
            if (arena.getName().equalsIgnoreCase(name)) {
                return arena;
            }
        }
        return null;
    }

    public boolean isAnyArenaAvailable() {
        return findNextEmptyArenaIfPresent() != null;
    }

    public Arena findNextEmptyArenaIfPresent() {
        for (Arena arena : arenas) {
            if (arena.getState() == ArenaState.EMPTY && arena.isReady()) {
                return arena;
            }
        }
        return null;
    }

}
