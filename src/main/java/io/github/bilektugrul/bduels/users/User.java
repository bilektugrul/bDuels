package io.github.bilektugrul.bduels.users;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.duels.Duel;
import io.github.bilektugrul.bduels.duels.DuelRequestProcess;
import io.github.bilektugrul.bduels.features.stats.StatisticType;
import io.github.bilektugrul.bduels.users.data.DatabaseType;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class User {

    private final static BDuels plugin = JavaPlugin.getPlugin(BDuels.class);

    private final Map<StatisticType, Integer> stats = new EnumMap<>(StatisticType.class);
    private final Player base;

    private UserState state;
    private Duel duel;
    private DuelRequestProcess requestProcess;
    private FileConfiguration data;

    public User(Player player) {
        this.base = player;
        this.state = UserState.FREE;
        if (plugin.getUsedDatabaseType() == DatabaseType.FLAT) {
            this.data = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder() + "/players/" + player.getUniqueId() + ".yml"));
        }
    }

    public Player getBase() {
        return base;
    }

    public Duel getDuel() {
        return duel;
    }

    public void setDuel(Duel duel) {
        this.duel = duel;
    }

    public DuelRequestProcess getRequestProcess() {
        return requestProcess;
    }

    public void setRequestProcess(DuelRequestProcess requestProcess) {
        this.requestProcess = requestProcess;
    }

    public String getName() {
        return base.getName();
    }

    public UUID getUUID() {
        return base.getUniqueId();
    }

    public FileConfiguration getData() {
        return data;
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState newState) {
        state = newState;
    }

    public boolean isInMatch() {
        return state == UserState.IN_MATCH || state == UserState.STARTING_MATCH;
    }

    public boolean doesAcceptDuelRequests() {
        return getStat(StatisticType.DUEL_REQUESTS) == 1;
    }

    public int getStat(StatisticType statisticType) {
        Integer statistic = stats.get(statisticType);

        if (statistic == null) {
            stats.put(statisticType, 0);
            return 0;
        }

        return statistic;
    }

    public void setStat(StatisticType stat, int i) {
        if (stat == StatisticType.DUEL_REQUESTS) {
            i = i >= 1 ? 1 : 0;
        }
        stats.put(stat, i);
    }

    public void addStat(StatisticType stat, int i) {
        if (stat == StatisticType.DUEL_REQUESTS) {
            return;
        }
        stats.put(stat, getStat(stat) + i);
    }

    public void sendMessage(String message) {
        base.sendMessage(Utils.getMessage(message, base));
    }

}