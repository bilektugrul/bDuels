package io.github.bilektugrul.bduels.duels;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.arenas.Arena;
import io.github.bilektugrul.bduels.users.User;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class DuelStartingTask extends BukkitRunnable {

    private final BDuels plugin;

    private final Duel duel;
    private final Player player, opponent;
    private final Location playerStartLocation, opponentStartLocation;
    private int time;

    public DuelStartingTask(BDuels plugin, Duel duel) {
        this.plugin = plugin;

        this.duel = duel;
        this.player = duel.getPlayer().getBase();
        this.opponent = duel.getOpponent().getBase();
        this.time = Utils.getInt("start-countdown");

        this.player.setMetadata("god-mode-bduels", new FixedMetadataValue(plugin, true));
        this.opponent.setMetadata("god-mode-bduels", new FixedMetadataValue(plugin, true));
        this.player.setFireTicks(0);
        this.opponent.setFireTicks(0);

        Arena arena = duel.getArena();
        playerStartLocation = arena.getPlayerLocation();
        opponentStartLocation = arena.getOpponentLocation();
        clean();
    }

    @Override
    public void run() {
        if (!Utils.isSameLoc(playerStartLocation, player.getLocation())) {
            player.teleport(playerStartLocation);
        }

        if (player.getHealth() != 20) {
            player.setHealth(20);
        }

        if (!Utils.isSameLoc(opponentStartLocation, opponent.getLocation())) {
            opponent.teleport(opponentStartLocation);
        }

        if (opponent.getHealth() != 20) {
            opponent.setHealth(20);
        }

        if (time == 0) {
            player.removeMetadata("god-mode-bduels", plugin);
            opponent.removeMetadata("god-mode-bduels", plugin);

            for (User user : duel.getPlayers()) {
                Player userPlayer = user.getBase();
                User opponentOf = duel.getOpponentOf(user);
                DuelRewards rewards = duel.getRewardsOf(opponentOf);
                userPlayer.sendMessage(Utils.getMessage("duel.start-message", userPlayer)
                        .replace("%opponent%", opponentOf.getName())
                        .replace("%opponentbet%", String.valueOf(rewards.getMoneyBet()))
                        .replace("%opponentitems%", String.valueOf(rewards.getItemsBet().size())));
            }
            duel.start();
            super.cancel();
            return;
        }

        String timeStr = String.valueOf(time);
        player.sendMessage(Utils.getMessage("duel.countdown", player)
                .replace("%seconds%", timeStr));
        opponent.sendMessage(Utils.getMessage("duel.countdown", opponent)
                .replace("%seconds%", timeStr));
        time--;
    }

    public void clean() {
        for (User user : duel.getPlayers()) {
            DuelRewards rewards = duel.getRewardsOf(user);
            for (ItemStack item : rewards.getItemsBet()) {
                Inventory userInventory = user.getBase().getInventory();
                userInventory.remove(item);
            }
        }
    }

}