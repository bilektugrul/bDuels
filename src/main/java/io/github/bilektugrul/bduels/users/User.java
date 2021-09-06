package io.github.bilektugrul.bduels.users;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class User {

    private final UUID uuid;
    private final String name;
    private UserState state;

    public User(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.state = UserState.FREE;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState newState) {
        state = newState;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

}
