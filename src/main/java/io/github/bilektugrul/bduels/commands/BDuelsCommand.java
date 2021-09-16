package io.github.bilektugrul.bduels.commands;

import io.github.bilektugrul.bduels.BDuels;
import io.github.bilektugrul.bduels.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Locale;

public class BDuelsCommand implements CommandExecutor {

    private final BDuels plugin;

    public BDuelsCommand(BDuels plugin) {
        this.plugin = plugin;
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
                try {
                    plugin.save();
                    sender.sendMessage(Utils.getMessage("main-command.saved", sender));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
        }
        return true;
    }

}
