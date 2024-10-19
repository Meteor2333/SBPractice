package com.meteor.SBPractice.Commands.SubCommands;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Plot;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Clear extends SubCommand {
    public Clear(MainCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Plot plot = Plot.getPlotByPlayer(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) return;
        }

        int x1 = Math.min((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
        int y1 = Math.min((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
        int z1 = Math.min((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());
        int x2 = Math.max((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
        int y2 = Math.max((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
        int z2 = Math.max((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());

        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                for (int k = z1; k <= z2; k++) {
                    player.getWorld().getBlockAt(i, j, k).setType(Material.AIR);
                }
            }
        } plot.stopTimer();
        plot.canStart(true);
    }
}
