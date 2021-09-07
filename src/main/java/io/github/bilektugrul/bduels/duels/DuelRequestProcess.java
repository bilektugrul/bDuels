package io.github.bilektugrul.bduels.duels;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class DuelRequestProcess {

    private final Player player, opponent;
    private final HashMap<String, DuelRewards> duelRewards = new HashMap<>();
    private final HashMap<String, Boolean> finished = new HashMap<>();

    public DuelRequestProcess(Player player, Player opponent) {
        this.player = player;
        this.opponent = opponent;
        for (Player p : getPlayers()) {
            String name = p.getName();
            duelRewards.put(name, new DuelRewards());
            finished.put(name, false);
        }
    }

    public Player[] getPlayers() {
        return new Player[]{player, opponent};
    }

    public DuelRewards getRewardsOf(String name) {
        return duelRewards.get(name);
    }

    public HashMap<String, DuelRewards> getDuelRewards() {
        return duelRewards;
    }

    public boolean isFinished(String name) {
        return finished.get(name);
    }

    public void setFinished(String name, boolean isFinished) {
        finished.put(name, isFinished);
    }

    public boolean isBothFinished() {
        for (boolean b : finished.values()) {
            if (!b) return false;
        }
        return true;
    }

    public HashMap<String, Boolean> getFinished() {
        return finished;
    }

    public Player getPlayer() {
        return player;
    }

    public Player getOpponent() {
        return opponent;
    }

}
