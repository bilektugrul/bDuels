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
    }

}
