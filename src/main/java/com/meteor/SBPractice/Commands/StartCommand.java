package com.meteor.SBPractice.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

public class StartCommand extends BukkitCommand {
    public StartCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Bukkit.dispatchCommand(sender, "sbp prestart");
        return true;
    }
}
