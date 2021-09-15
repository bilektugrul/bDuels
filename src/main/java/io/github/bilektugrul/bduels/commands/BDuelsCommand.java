package io.github.bilektugrul.bduels.commands;

import io.github.bilektugrul.bduels.BDuels;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

//TODO: save ve reload komutlarını ekle
public class BDuelsCommand implements CommandExecutor {

    private final BDuels bDuels;

    public BDuelsCommand(BDuels bDuels) {
        this.bDuels = bDuels;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return true;
    }

}
