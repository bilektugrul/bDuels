package io.github.bilektugrul.bduels.economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface EconomyAdapter {

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