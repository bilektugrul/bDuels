package io.github.bilektugrul.bduels.duels;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DuelRewards {

    private int moneyBet;
    private List<ItemStack> itemsBet;

    public DuelRewards() {}

    public int getMoneyBet() {
        return moneyBet;
    }

    public List<ItemStack> getItemsBet() {
        return itemsBet;
    }

    public void setMoneyBet(int moneyBet) {
        this.moneyBet = moneyBet;
    }

    public void setItemsBet(List<ItemStack> itemsBet) {
        this.itemsBet = itemsBet;
    }

    public void addItemToBet(ItemStack itemStack) {
        itemsBet.add(itemStack);
    }

}
