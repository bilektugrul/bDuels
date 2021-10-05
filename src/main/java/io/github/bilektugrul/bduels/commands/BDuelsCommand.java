package io.github.bilektugrul.bduels.commands;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BDuelsCommand implements CommandExecutor {

    private final BDuels plugin;

    public BDuelsCommand(BDuels plugin) {
        this.plugin = plugin;
        plugin.getCommand("bduels").setTabCompleter(new BDuelsCommandTabCompleter());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("bduels.admin")) {
            Utils.noPermission(sender);
            return true;
        }

        if (args.length <= 0) {
            sender.sendMessage(Utils.getMessage("main-command.wrong-usage", sender));
            return true;
        }

        String execute = args[0].toLowerCase(Locale.ROOT);
        switch (execute) {
            case "reload":
                plugin.reload();
                sender.sendMessage(Utils.getMessage("main-command.reloaded", sender));
                return true;
            case "save":
                plugin.save();
                sender.sendMessage(Utils.getMessage("main-command.saved", sender));
                return true;
            case "save-stats":
                if (plugin.isDatabaseEnabled()) {
                    if (plugin.saveAllUserStatistics())
                        sender.sendMessage(Utils.getMessage("main-command.saved-stats", sender));
                    else
                        sender.sendMessage(Utils.getMessage("main-command.could-not-saved", sender));
                }
                return true;
        }
        return true;
    }
    
    private class BDuelsCommandTabCompleter implements TabCompleter {

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
            return Arrays.asList("reload", "save", "save-stats");
        }

    }

}
