package io.github.bilektugrul.bduels.duels;

import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.arenas.ArenaState;
import io.github.bilektugrul.bduels.stuff.PlayerType;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserState;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class Duel {

    private final User player, opponent;
    private final User[] players;
    private User winner, loser;

    private final Arena arena;
    private DuelStartingTask startingTask;
    private final Map<User, DuelRewards> duelRewards;

    private final Map<User, Location> preDuelLocations = new HashMap<>();

    public Duel(DuelRequestProcess requestProcess, Arena arena) {
        this.player = requestProcess.getPlayer();
        this.opponent = requestProcess.getOpponent();
        this.players = new User[]{player, opponent};

        this.arena = arena;
        this.duelRewards = requestProcess.getDuelRewards();

        this.player.setDuel(this);
        this.opponent.setDuel(this);

        Player player = this.player.getBase();
        Player opponentPlayer = this.opponent.getBase();

        preDuelLocations.put(this.player, player.getLocation());
        preDuelLocations.put(this.opponent, opponentPlayer.getLocation());
    }

    public void start() {
        Player requestSender = player.getBase();
        requestSender.teleport(arena.getPlayerLocation());
        player.setState(UserState.IN_MATCH);

        Player opponentPlayer = opponent.getBase();
        opponentPlayer.teleport(arena.getOpponentLocation());
        opponent.setState(UserState.IN_MATCH);

        arena.setState(ArenaState.IN_MATCH);
    }

    public Map<User, DuelRewards> getDuelRewards() {
        return duelRewards;
    }

    public Map<User, Location> getPreDuelLocations() {
        return preDuelLocations;
    }

    public User[] getPlayers() {
        return players;
    }

    public PlayerType getPlayerType(User user) {
        return user.equals(player)
                ? PlayerType.PLAYER
                : PlayerType.OPPONENT;
    }

    public User getOpponentOf(User user) {
        for (User user2 : getPlayers()) {
            if (!user2.equals(user)) return user2;
        }
        return null;
    }

    public DuelRewards getRewardsOf(User user) {
        return duelRewards.get(user);
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

    public User getWinner() {
        return winner;
    }

    public User getLoser() {
        return loser;
    }

    public void setWinner(User winner) {
        this.winner = winner;
        setLoser(getOpponentOf(winner));
    }

    public void setLoser(User loser) {
        this.loser = loser;
    }

    public DuelStartingTask getStartingTask() {
        return startingTask;
    }

    public void setStartingTask(DuelStartingTask startingTask) {
        this.startingTask = startingTask;
    }

}
