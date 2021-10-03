package io.github.bilektugrul.bduels.duels;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DuelRewards {

    private int moneyBet = 0;
    private List<ItemStack> itemsBet = new ArrayList<>();

    public int getMoneyBet() {
        return moneyBet;
    }

    public List<ItemStack> getItemsBet() {
        return itemsBet;
    }

    public void setMoneyBet(int moneyBet) {
        this.moneyBet = moneyBet;
    }

    public void addMoneyToBet(int amount) {
        this.moneyBet += amount;
    }

    public void setItemsBet(List<ItemStack> itemsBet) {
        this.itemsBet = itemsBet;
    }

    public void addItemToBet(ItemStack itemStack) {
        itemsBet.add(itemStack);
    }

    public void removeItem(ItemStack itemStack) {
        itemsBet.remove(itemStack);
    }

    public boolean containsItem(ItemStack itemStack) {
        return itemsBet.contains(itemStack);
    }

}
