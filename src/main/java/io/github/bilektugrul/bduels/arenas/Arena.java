package io.github.bilektugrul.bduels.arenas;

import org.bukkit.Location;

public class Arena {

    private final String name;
    private Location playerLocation, opponentLocation;
    private Location edge, otherEdge;
    private ArenaState state;

    public Arena(String name) {
        this.name = name;
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

    public void setEdge(Location edge) {
        this.edge = edge;
    }

    public void setOtherEdge(Location otherEdge) {
        this.otherEdge = otherEdge;
    }

    public void setPlayerLocation(Location playerLocation) {
        this.playerLocation = playerLocation;
    }

    public void setOpponentLocation(Location opponentLocation) {
        this.opponentLocation = opponentLocation;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }
}
