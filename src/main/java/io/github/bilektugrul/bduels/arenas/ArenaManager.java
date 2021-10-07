package io.github.bilektugrul.bduels.arenas;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.DuelManager;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;

public class ArenaManager {

    private final BDuels plugin;
    private final Set<Arena> arenas = new HashSet<>();

    private FileConfiguration arenasFile;
    private DuelManager duelManager;

    public ArenaManager(BDuels plugin) {
        this.plugin = plugin;
        this.duelManager = plugin.getDuelManager();
        loadArenas();
    }

    public void loadArenas() {
        arenasFile = ConfigUtils.getConfig(plugin, "arenas");
        arenas.clear();
        for (String name : arenasFile.getConfigurationSection("arenas").getKeys(false)) {
            String path = "arenas." + name + ".";

            Location playerLocation = LocationSerializer.fromString(arenasFile.getString(path + "playerLocation"));
            Location opponentLocation = LocationSerializer.fromString(arenasFile.getString(path + "opponentLocation"));
            Location edgeLocation = LocationSerializer.fromString(arenasFile.getString(path + "edgeLocation"));
            Location otherEdgeLocation = LocationSerializer.fromString(arenasFile.getString(path + "otherEdgeLocation"));

            Arena arena = new Arena(name);
            arena.setPlayerLocation(playerLocation);
            arena.setOpponentLocation(opponentLocation);
            arena.setEdge(edgeLocation);
            arena.setOtherEdge(otherEdgeLocation);
            registerArena(arena);
        }
    }

    public void setDuelManager(DuelManager duelManager) {
        this.duelManager = duelManager;
    }

    public Set<Arena> getArenas() {
        return new HashSet<>(arenas);
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
        boolean removed = arenas.removeIf(arena -> arena.getName().equalsIgnoreCase(name) && arena.getState() == ArenaState.EMPTY);
        if (removed) {
            arenasFile.set("arenas." + name, null);
        }
        return removed;
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

    public void save() {
        for (Arena arena : arenas) {
            String playerLocation = LocationSerializer.toString(arena.getPlayerLocation());
            String opponentLocation = LocationSerializer.toString(arena.getOpponentLocation());
            String edgeLocation = LocationSerializer.toString(arena.getEdge());
            String otherEdgeLocation = LocationSerializer.toString(arena.getOtherEdge());

            String name = arena.getName();
            String path = "arenas." + name + ".";

            arenasFile.set(path + "playerLocation", playerLocation);
            arenasFile.set(path + "opponentLocation", opponentLocation);
            arenasFile.set(path + "edgeLocation", edgeLocation);
            arenasFile.set(path + "otherEdgeLocation", otherEdgeLocation);
        }
        ConfigUtils.saveConfig(plugin, arenasFile, "arenas");
    }

}
