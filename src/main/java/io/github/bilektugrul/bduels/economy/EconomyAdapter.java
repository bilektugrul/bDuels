package io.github.bilektugrul.bduels.economy;

import io.github.bilektugrul.bduels.BDuels;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface EconomyAdapter {

    default void register() {
        JavaPlugin.getPlugin(BDuels.class).setEconomyAdapter(this);
    }

    void addMoney(String playerName, double amount);

    void addMoney(Player player, double amount);

    void addMoney(OfflinePlayer offlinePlayer, double amount);

    void removeMoney(String playerName, double amount);

    void removeMoney(Player player, double amount);

    void removeMoney(OfflinePlayer offlinePlayer, double amount);

    double getMoney(String playerName);

    double getMoney(Player player);

    double getMoney(OfflinePlayer offlinePlayer);

}