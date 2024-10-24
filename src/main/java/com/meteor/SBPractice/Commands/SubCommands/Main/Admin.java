package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class Admin extends SubCommand {
    private static ArrayList<UUID> adminList = new ArrayList<>();

    public Admin(MainCommand parent, String name) {
        super(parent, name, true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = ((Player) sender);
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
        if (adminList.contains(player.getUniqueId())) {
            if (Plot.autoAddPlayerFromPlot(player, null, false)) {
                adminList.remove(player.getUniqueId());
                player.sendMessage(Messages.getMessage("admin-mode-disabled"));
            } else player.sendMessage(Messages.getMessage("plot-full"));
        } else {
            adminList.add(player.getUniqueId());
            player.setAllowFlight(true);
            player.sendMessage(Messages.getMessage("admin-mode-enabled"));
            Plot.autoRemovePlayerFromPlot(player);
        }
    }

    public static boolean check(Player player) {
        return adminList.contains(player.getUniqueId());
    }

    public static void removeFromAdminList(UUID key) {
        adminList.remove(key);
    }
}
