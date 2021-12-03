package io.github.bilektugrul.bduels.economy;

import io.github.bilektugrul.bduels.BDuels;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class VaultEconomy implements EconomyAdapter {

    private final Economy economy;

    public VaultEconomy(BDuels plugin) {
        economy = plugin.getVaultManager().getEconomy();
    }

    @Override
    public void addMoney(String playerName, double amount) {
        economy.depositPlayer(playerName, amount);
    }

    @Override
    public void addMoney(Player player, double amount) {
        economy.depositPlayer(player, amount);
    }

    @Override
    public void addMoney(OfflinePlayer offlinePlayer, double amount) {
        economy.depositPlayer(offlinePlayer, amount);
    }

    @Override
    public void removeMoney(String playerName, double amount) {
        economy.withdrawPlayer(playerName, amount);
    }

    @Override
    public void removeMoney(Player player, double amount) {
        economy.withdrawPlayer(player, amount);
    }

    @Override
    public void removeMoney(OfflinePlayer offlinePlayer, double amount) {
        economy.withdrawPlayer(offlinePlayer, amount);
    }

    @Override
    public double getMoney(String playerName) {
        return economy.getBalance(playerName);
    }

    @Override
    public double getMoney(Player player) {
        return economy.getBalance(player);
    }

    @Override
    public double getMoney(OfflinePlayer offlinePlayer) {
        return economy.getBalance(offlinePlayer);
    }
    
}