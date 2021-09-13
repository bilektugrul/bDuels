package io.github.bilektugrul.bduels.economy;

import io.github.bilektugrul.bduels.BDuels;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultManager {

    private final BDuels plugin;
    private Economy economy;

    public VaultManager(BDuels plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    public void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        economy = rsp.getProvider();
    }

    public Economy getEconomy() {
        return economy;
    }

}
