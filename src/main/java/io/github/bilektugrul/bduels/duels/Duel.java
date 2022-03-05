package io.github.bilektugrul.bduels.duels;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.arenas.ArenaState;
import io.github.bilektugrul.bduels.features.stats.StatisticType;
import io.github.bilektugrul.bduels.stuff.PlayerType;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.users.UserState;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class Duel extends BukkitRunnable {

    private static final BDuels plugin = JavaPlugin.getPlugin(BDuels.class);

    private final Arena arena;
    private final User[] players;
    private final User player, opponent;
    private final DuelStartingTask startingTask;
    private final Map<User, DuelRewards> duelRewards;
    private final Map<User, Location> preDuelLocations = new HashMap<>();

    private int time;
    private boolean started;
    private User winner, loser;

    public Duel(DuelRequestProcess requestProcess, Arena arena) {
        this.player = requestProcess.getPlayer();
        this.opponent = requestProcess.getOpponent();
        this.players = requestProcess.getPlayers();

        this.arena = arena;
        this.duelRewards = requestProcess.getDuelRewards();

        this.player.setDuel(this);
        this.opponent.setDuel(this);

        this.player.setRequestProcess(null);
        this.opponent.setRequestProcess(null);

        this.player.addStat(StatisticType.TOTAL_MATCHES, 1);
        this.opponent.addStat(StatisticType.TOTAL_MATCHES, 1);

        this.time = Utils.getInt("in-game-settings.match-time");
        this.startingTask = new DuelStartingTask(this);

        preDuelLocations.put(player, player.getBase().getLocation());
        preDuelLocations.put(opponent, opponent.getBase().getLocation());
    }

    public void startCountdown() {
        if (started) {
            return;
        }

        for (User user : players) {
            user.setState(UserState.STARTING_MATCH);
        }

        startingTask.runTaskTimer(plugin, 0, 20L);
    }

    public void start() {
        if (started) {
            return;
        }

        this.started = true;

        Player requestSender = player.getBase();
        requestSender.removeMetadata("god-mode-bduels", plugin);
        requestSender.setHealth(requestSender.getMaxHealth());
        requestSender.teleport(arena.getPlayerLocation());
        this.player.setState(UserState.IN_MATCH);

        Player opponentPlayer = opponent.getBase();
        opponentPlayer.removeMetadata("god-mode-bduels", plugin);
        opponentPlayer.setHealth(opponentPlayer.getMaxHealth());
        opponentPlayer.teleport(arena.getOpponentLocation());
        this.opponent.setState(UserState.IN_MATCH);

        this.arena.setState(ArenaState.IN_MATCH);
        this.runTaskTimer(plugin, 20, 20);
    }

    @Override
    public void run() {
        time--;
        if (time == 0) {
            setWinner(player);
            plugin.getDuelManager().endMatch(this, DuelEndReason.TIME_ENDED, false);
        }
    }

    public Map<User, DuelRewards> getDuelRewards() {
        return duelRewards;
    }

    public Location getPreDuelLocationOf(User user) {
        return preDuelLocations.get(user);
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
        for (User user2 : players) {
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

    public int getTime() {
        return time;
    }

    public String getTimeString() {
        return Utils.formatTime(time * 1000L);
    }

    public boolean isStarted() {
        return started;
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

}