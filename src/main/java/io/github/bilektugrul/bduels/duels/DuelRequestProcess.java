package io.github.bilektugrul.bduels.duels;

import io.github.bilektugrul.bduels.users.User;

import java.util.HashMap;

public class DuelRequestProcess {

    private final User player, opponent;
    private final HashMap<User, DuelRewards> duelRewards = new HashMap<>();
    private final HashMap<User, Boolean> finished = new HashMap<>();

    public DuelRequestProcess(User player, User opponent) {
        this.player = player;
        this.opponent = opponent;
        for (User user : getPlayers()) {
            duelRewards.put(user, new DuelRewards());
            finished.put(user, false);
        }
    }

    public User[] getPlayers() {
        return new User[]{player, opponent};
    }

    public DuelRewards getRewardsOf(String name) {
        return duelRewards.get(name);
    }

    public HashMap<User, DuelRewards> getDuelRewards() {
        return duelRewards;
    }

    public boolean isFinished(User user) {
        return finished.get(user);
    }

    public void setFinished(User user, boolean isFinished) {
        finished.put(user, isFinished);
    }

    public boolean isBothFinished() {
        for (boolean b : finished.values()) {
            if (!b) return false;
        }
        return true;
    }

    public HashMap<User, Boolean> getFinished() {
        return finished;
    }

    public User getPlayer() {
        return player;
    }

    public User getOpponent() {
        return opponent;
    }

}
