package io.github.bilektugrul.bduels.users;

import io.github.bilektugrul.bduels.duels.Duel;
import org.bukkit.entity.Player;

import java.util.UUID;

public class User {

    private final Player player;

    private UserState state;
    private Duel duel;

    public User(Player player) {
        this.player = player;
        this.state = UserState.FREE;
    }

    public Player getPlayer() {
        return player;
    }

    public Duel getDuel() {
        return duel;
    }

    public String getName() {
        return player.getName();
    }

    public UUID getUUID() {
        return player.getUniqueId();
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState newState) {
        state = newState;
    }

}
