package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Utils.VersionSupport;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Admin extends SubCommand {

    @Getter
    private static final ArrayList<String> adminList = new ArrayList<>();

    public Admin(MainCommand parent, String name) {
        super(parent, name, true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        SBPPlayer player = SBPPlayer.getPlayer((Player) sender);
        if (player == null) return;
        player.playSound(VersionSupport.SOUND_ORB_PICKUP.getForCurrentVersionSupport());
        if (adminList.contains(player.getPlayer().getName())) {
            if (Plot.autoAddPlayerFromPlot(player, null, false)) {
                adminList.remove(player.getPlayer().getName());
                player.sendMessage(Messages.ADMIN_MODE_DISABLED.getMessage());
            } else player.sendMessage(Messages.PLOT_FULL.getMessage());
        } else {
            adminList.add(player.getPlayer().getName());
            player.resetPlayer();
            player.sendMessage(Messages.ADMIN_MODE_ENABLED.getMessage());
            Plot.autoRemovePlayerFromPlot(player);
        }
    }

}
