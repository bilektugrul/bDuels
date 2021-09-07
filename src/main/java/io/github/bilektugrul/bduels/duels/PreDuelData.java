package io.github.bilektugrul.bduels.duels;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class PreDuelData {

    private final Location location;
    private final ItemStack[] inventory, armors;

    public PreDuelData(Location location, ItemStack[] inventory, ItemStack[] armors) {
        this.location = location;
        this.inventory = inventory;
        this.armors = armors;
    }

    public Location getLocation() {
        return location;
    }

    public ItemStack[] getInventory() {
        return inventory;
    }

    public ItemStack[] getArmors() {
        return armors;
    }

}
