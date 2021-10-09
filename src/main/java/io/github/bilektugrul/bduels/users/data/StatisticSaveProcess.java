package io.github.bilektugrul.bduels.users.data;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.features.leaderboards.LeaderboardManager;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitRunnable;

public class StatisticSaveProcess extends BukkitRunnable {

    private final BDuels plugin;

    public StatisticSaveProcess(BDuels plugin) {
        this.plugin = plugin;
    }

    public void start() {
        plugin.getLogger().info("Oyuncu verilerinin otomatik kayıt süreci başlatılıyor...");
        int i = plugin.getConfig().getInt("database.auto-save-interval");
        runTaskTimerAsynchronously(plugin, 0, (i * 60L) * 20);
    }

    @Override
    public void run() {
        ConsoleCommandSender console = plugin.getServer().getConsoleSender();
        console.sendMessage(Utils.getMessage("saving-statistics", console));
        plugin.saveAllUserStatistics();
        LeaderboardManager leaderboardManager = plugin.getLeaderboardManager();
        leaderboardManager.sortEveryLeaderboard();
        console.sendMessage(Utils.getMessage("main-command.saved-stats", console));
    }

}