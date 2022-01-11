package io.github.bilektugrul.bduels.economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class EmptyEconomy implements EconomyAdapter {

    @Override
    public void addMoney(String playerName, double amount) {}

    @Override
    public void addMoney(Player player, double amount) {}

    @Override
    public void addMoney(OfflinePlayer offlinePlayer, double amount) {}

    @Override
    public void removeMoney(String playerName, double amount) {}

    @Override
    public void removeMoney(Player player, double amount) {}

    @Override
    public void removeMoney(OfflinePlayer offlinePlayer, double amount) {}

    @Override
    public double getMoney(String playerName) {
        return 0;
    }

    @Override
    public double getMoney(Player player) {
        return 0;
    }

    @Override
    public double getMoney(OfflinePlayer offlinePlayer) {
        return 0;
    }

}