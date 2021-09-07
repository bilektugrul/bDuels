package io.github.bilektugrul.bduels.duels;

import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.arenas.ArenaState;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;

public class Duel {

    private final User player, opponent;
    private final Arena arena;
    private final HashMap<User, DuelRewards> duelRewards;

    private final HashMap<User, PreDuelData> preDuelData = new HashMap<>();

    public Duel(DuelRequestProcess requestProcess, Arena arena) {
        this.player = requestProcess.getPlayer();
        this.opponent = requestProcess.getOpponent();
        this.arena = arena;
        this.duelRewards = requestProcess.getDuelRewards();

        Player player = this.player.getPlayer();
        Player opponentPlayer = this.opponent.getPlayer();

        PlayerInventory playerInventory = player.getInventory();
        PlayerInventory opponentInventory = opponentPlayer.getInventory();

        PreDuelData playerData = new PreDuelData(player.getLocation(), playerInventory.getContents(), playerInventory.getArmorContents());
        PreDuelData opponentData = new PreDuelData(opponentPlayer.getLocation(), opponentInventory.getContents(), opponentInventory.getArmorContents());
        preDuelData.put(this.player, playerData);
        preDuelData.put(this.opponent, opponentData);
    }

    public void start() {
        Player player1 = this.player.getPlayer();
        player1.teleport(arena.getPlayerLocation());
        player.setState(UserState.IN_MATCH);

        Player opponentPlayer = this.opponent.getPlayer();
        opponentPlayer.teleport(arena.getOpponentLocation());
        opponent.setState(UserState.IN_MATCH);

        arena.setState(ArenaState.IN_MATCH);
    }

    public HashMap<User, DuelRewards> getDuelRewards() {
        return duelRewards;
    }

    public HashMap<User, PreDuelData> getPreDuelData() {
        return preDuelData;
    }

    public User[] getPlayers() {
        return new User[]{player, opponent};
    }

    public User getOpponentOf(User user) {
        for (User user2 : getPlayers()) {
            if (!user2.equals(user)) return user2;
        }
        return null;
    }

    public Arena getArena() {
        return arena;
    }

    public User getPlayer() {
        return player;
    }

    public User getOpponent() {
        return opponent;
    }

}
