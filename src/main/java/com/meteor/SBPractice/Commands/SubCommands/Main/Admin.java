package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class Admin extends SubCommand {
    private static final ArrayList<UUID> adminList = new ArrayList<>();

    public Admin(MainCommand parent, String name) {
        super(parent, name, true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        SBPPlayer player = SBPPlayer.getPlayer((Player) sender);
        if (player == null) return;
        player.playSound(Utils.Sounds.ORB_PICKUP);
        if (adminList.contains(player.getPlayer().getUniqueId())) {
            if (Plot.autoAddPlayerFromPlot(player, null, false)) {
                adminList.remove(player.getPlayer().getUniqueId());
                player.sendMessage(Messages.ADMIN_MODE_DISABLED.getMessage());
            } else player.sendMessage(Messages.PLOT_FULL.getMessage());
        } else {
            adminList.add(player.getPlayer().getUniqueId());
            player.getPlayer().setAllowFlight(true);
            player.sendMessage(Messages.ADMIN_MODE_ENABLED.getMessage());
            Plot.autoRemovePlayerFromPlot(player);
        }
    }

    public static boolean check(SBPPlayer player) {
        return adminList.contains(player.getPlayer().getUniqueId());
    }

    public static void removeFromAdminList(UUID key) {
        adminList.remove(key);
    }
}
