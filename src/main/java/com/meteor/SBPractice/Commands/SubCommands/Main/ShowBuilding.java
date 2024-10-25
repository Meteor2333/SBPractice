package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowBuilding extends SubCommand {
    public ShowBuilding(MainCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        SBPPlayer player = SBPPlayer.getPlayer((Player) sender);
        if (player == null) return;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            player.sendMessage(Messages.CANNOT_DO_THAT.getMessage());
            return;
        } plot.stopTimer();

        plot.getRegion().setBlocks(plot.getBufferBuildBlock());
        player.playSound(Utils.Sounds.ORB_PICKUP);
        player.sendMessage(Messages.SHOW_BUILD.getMessage());
    }
}
