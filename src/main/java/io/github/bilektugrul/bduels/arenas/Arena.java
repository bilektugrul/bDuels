package io.github.bilektugrul.bduels.arenas;

import org.bukkit.Location;

public class Arena {

    private String name;
    private Location startLocation, startLocation2;
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

    public Location getStartLocation() {
        return startLocation;
    }

    public Location getStartLocation2() {
        return startLocation2;
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

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public void setStartLocation2(Location startLocation2) {
        this.startLocation2 = startLocation2;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }
}
