package io.github.bilektugrul.bduels.economy;

import io.github.bilektugrul.bduels.BDuels;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class VaultEconomy implements EconomyAdapter {

    private final VaultManager vaultHook;

    public VaultEconomy(BDuels plugin) {
        this.vaultHook = plugin.getVaultManager();
        plugin.setEconomyAdapter(this);
    }

    @Override
    public void addMoney(String playerName, double amount) {
        this.vaultHook.getEconomy().depositPlayer(playerName, amount);
    }

    @Override
    public void addMoney(Player player, double amount) {
        this.vaultHook.getEconomy().depositPlayer(player, amount);
    }

    @Override
    public void addMoney(OfflinePlayer offlinePlayer, double amount) {
        this.vaultHook.getEconomy().depositPlayer(offlinePlayer, amount);
    }

    @Override
    public void removeMoney(String playerName, double amount) {
        this.vaultHook.getEconomy().withdrawPlayer(playerName, amount);
    }

    @Override
    public void removeMoney(Player player, double amount) {
        this.vaultHook.getEconomy().withdrawPlayer(player, amount);
    }

    @Override
    public void removeMoney(OfflinePlayer offlinePlayer, double amount) {
        this.vaultHook.getEconomy().withdrawPlayer(offlinePlayer, amount);
    }

    @Override
    public double getMoney(String playerName) {
        return this.vaultHook.getEconomy().getBalance(playerName);
    }

    @Override
    public double getMoney(Player player) {
        return this.vaultHook.getEconomy().getBalance(player);
    }

    @Override
    public double getMoney(OfflinePlayer offlinePlayer) {
        return this.vaultHook.getEconomy().getBalance(offlinePlayer);
    }
}