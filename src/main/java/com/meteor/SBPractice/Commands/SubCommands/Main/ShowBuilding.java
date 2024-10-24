package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowBuilding extends SubCommand {
    public ShowBuilding(MainCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = ((Player) sender);
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            player.sendMessage(Messages.getMessage("cannot-do-that"));
            return;
        }
        if (!plot.getSpawnPoint().getWorld().equals(player.getWorld())) return;
        plot.stopTimer();

        plot.getRegion().setBlocks(plot.getBufferBuildBlock());
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
        player.sendMessage(Messages.getMessage("show-build"));
    }
}
