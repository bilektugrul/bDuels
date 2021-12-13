package io.github.bilektugrul.bduels.duels;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.arenas.ArenaState;
import io.github.bilektugrul.bduels.features.stats.StatisticType;
import io.github.bilektugrul.bduels.stuff.PlayerType;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserState;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Duel {

    private static final BDuels plugin = JavaPlugin.getPlugin(BDuels.class);

    private final Arena arena;
    private final User[] players;
    private final User player, opponent;
    private final Map<User, DuelRewards> duelRewards;
    private final Map<User, Location> preDuelLocations = new HashMap<>();
    private final List<Location> placedBlocks = new ArrayList<>();

    private User winner, loser;
    private DuelStartingTask startingTask;

    public Duel(DuelRequestProcess requestProcess, Arena arena) {
        this.player = requestProcess.getPlayer();
        this.opponent = requestProcess.getOpponent();
        this.players = requestProcess.getPlayers();

        this.arena = arena;
        this.duelRewards = requestProcess.getDuelRewards();

        this.player.setDuel(this);
        this.opponent.setDuel(this);
        this.player.addStat(StatisticType.TOTAL_MATCHES, 1);
        this.opponent.addStat(StatisticType.TOTAL_MATCHES, 1);

        preDuelLocations.put(player, player.getBase().getLocation());
        preDuelLocations.put(opponent, opponent.getBase().getLocation());
    }

    public void startCountdown() {
        for (User user : players) {
            user.setState(UserState.STARTING_MATCH);
        }
        setStartingTask(new DuelStartingTask(this));
        startingTask.runTaskTimer(plugin, 0, 20L);
    }

    public void start() {
        Player requestSender = player.getBase();
        requestSender.setHealth(requestSender.getMaxHealth());
        requestSender.teleport(arena.getPlayerLocation());
        player.setState(UserState.IN_MATCH);

        Player opponentPlayer = opponent.getBase();
        opponentPlayer.setHealth(opponentPlayer.getMaxHealth());
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

    public List<Location> getPlacedBlocks() {
        return placedBlocks;
    }

    public void addPlacedBlockLocation(Location location) {
        if (!doesPlacedBlockExist(location)) {
            placedBlocks.add(location);
        }
    }

    public void removePlacedBlockLocation(Location location) {
        placedBlocks.remove(location);
    }

    public boolean doesPlacedBlockExist(Location location) {
        return placedBlocks.contains(location);
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
            if (!user2.equals(user)) {
                return user2;
            }
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