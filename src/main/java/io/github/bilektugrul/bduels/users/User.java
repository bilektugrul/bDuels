package io.github.bilektugrul.bduels.users;

import io.github.bilektugrul.bduels.duels.Duel;
import io.github.bilektugrul.bduels.duels.DuelRequestProcess;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class User {

    private final Player base;

    private UserState state;
    private Duel duel;
    private DuelRequestProcess requestProcess;
    private Location respawnLocation;

    public User(Player player) {
        this.base = player;
        this.state = UserState.FREE;
    }

    public Player getBase() {
        return base;
    }

    public Duel getDuel() {
        return duel;
    }

    public void setDuel(Duel duel) {
        this.duel = duel;
    }

    public DuelRequestProcess getRequestProcess() {
        return requestProcess;
    }

    public void setRequestProcess(DuelRequestProcess requestProcess) {
        this.requestProcess = requestProcess;
    }

    public Location getRespawnLocation() {
        return respawnLocation;
    }

    public void setRespawnLocation(Location respawnLocation) {
        this.respawnLocation = respawnLocation;
    }

    public String getName() {
        return base.getName();
    }

    public UUID getUUID() {
        return base.getUniqueId();
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState newState) {
        state = newState;
    }

    public void sendMessage(String message) {
        base.sendMessage(Utils.getMessage(message, base));
    }

}
