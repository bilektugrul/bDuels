package io.github.bilektugrul.bduels.duels;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MoneyBetSettings {

    private final ItemStack item;
    private final int moneyToAdd;

    public MoneyBetSettings(ItemStack item, int moneyToAdd, List<String> lore, String name) {
        this.item = item;
        this.moneyToAdd = moneyToAdd;

        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        meta.setDisplayName(name);
        item.setItemMeta(meta);
    }

    public ItemStack getItem() {
        return item;
    }

    public int getMoneyToAdd() {
        return moneyToAdd;
    }

}