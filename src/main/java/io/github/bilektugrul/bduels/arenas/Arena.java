package io.github.bilektugrul.bduels.arenas;

import org.apache.commons.lang.math.IntRange;
import org.bukkit.Location;

public class Arena {

    private final String name;
    private Location playerLocation, opponentLocation, edge, otherEdge;
    private ArenaState state;

    public Arena(String name) {
        this.name = name;
        this.state = ArenaState.EMPTY;
    }

    public String getName() {
        return name;
    }

    public Location getEdge() {
        return edge;
    }

    public Location getOtherEdge() {
        return otherEdge;
    }

    public Location getPlayerLocation() {
        return playerLocation;
    }

    public Location getOpponentLocation() {
        return opponentLocation;
    }

    public ArenaState getState() {
        return state;
    }

    public boolean isInArea(Location origin) {
        System.out.println("anneni md5 " + origin);
        return (new IntRange(edge.getX(), otherEdge.getX()).containsDouble(origin.getX())
                && new IntRange(edge.getY(), otherEdge.getY()).containsDouble(origin.getY())
                && new IntRange(edge.getZ(), otherEdge.getZ()).containsDouble(origin.getZ()));
    }

    public Arena setEdge(Location edge) {
        this.edge = edge;
        return this;
    }

    public Arena setOtherEdge(Location otherEdge) {
        this.otherEdge = otherEdge;
        return this;
    }

    public Arena setPlayerLocation(Location playerLocation) {
        this.playerLocation = playerLocation;
        return this;
    }

    public Arena setOpponentLocation(Location opponentLocation) {
        this.opponentLocation = opponentLocation;
        return this;
    }

    public Arena setState(ArenaState state) {
        this.state = state;
        return this;
    }

    public boolean isReady() {
        return playerLocation != null && opponentLocation != null
                && edge != null && otherEdge != null;
    }

}