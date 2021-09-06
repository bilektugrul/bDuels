package io.github.bilektugrul.bduels.duels;

import io.github.bilektugrul.bduels.arenas.Arena;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Duel {

    private final Player player, opponent;
    private Arena arena;
    private final HashMap<Player, DuelRewards> duelRewards;

    public Duel(Player player, Player opponent, Arena arena, HashMap<Player, DuelRewards> duelRewards) {
        this.player = player;
        this.opponent = opponent;
        this.arena = arena;
        this.duelRewards = duelRewards;
    }

}
