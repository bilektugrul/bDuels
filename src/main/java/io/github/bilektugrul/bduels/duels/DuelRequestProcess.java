package io.github.bilektugrul.bduels.duels;

import io.github.bilektugrul.bduels.stuff.PlayerType;
import io.github.bilektugrul.bduels.users.User;

import java.util.HashMap;
import java.util.Map;

public class DuelRequestProcess {

    private final User player, opponent;
    private final User[] players;

    private final Map<User, DuelRewards> duelRewards = new HashMap<>();
    private final Map<User, Boolean> finished = new HashMap<>();

    private boolean requestAccepted = false;

    public DuelRequestProcess(User player, User opponent) {
        this.player = player;
        this.opponent = opponent;
        this.players = new User[]{player, opponent};

        for (User user : players) {
            duelRewards.put(user, new DuelRewards());
            finished.put(user, false);
        }
    }

    public User[] getPlayers() {
        return players;
    }

    public PlayerType getPlayerType(User user) {
        return user.equals(player)
                ? PlayerType.PLAYER
                : PlayerType.OPPONENT;
    }

    public DuelRewards getRewardsOf(User name) {
        return duelRewards.get(name);
    }

    public User getOpponentOf(User user) {
        for (User user2 : getPlayers()) {
            if (!user2.equals(user)) return user2;
        }
        return null;
    }

    public Map<User, DuelRewards> getDuelRewards() {
        return duelRewards;
    }

    public boolean isFinished(User user) {
        return finished.get(user);
    }

    public void setFinished(User user, boolean isFinished) {
        finished.put(user, isFinished);
    }

    public boolean isRequestAccepted() {
        return requestAccepted;
    }

    public void setRequestAccepted(boolean requestAccepted) {
        this.requestAccepted = requestAccepted;
    }

    public boolean isBothFinished() {
        for (boolean b : finished.values()) {
            if (!b) return false;
        }
        return true;
    }

    public Map<User, Boolean> getFinished() {
        return finished;
    }

    public User getPlayer() {
        return player;
    }

    public User getOpponent() {
        return opponent;
    }

}
