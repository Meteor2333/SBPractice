package com.meteor.SBPractice.Commands.SubCommands;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Plot;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowBuild extends SubCommand {
    public ShowBuild(MainCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = ((Player) sender);
        Plot plot = Plot.getPlotByPlayer(player);
        if (plot == null) return;
        if (!plot.getSpawnPoint().getWorld().equals(player.getWorld())) return;
        plot.stopTimer();

        int x1 = Math.min((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
        int y1 = Math.min((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
        int z1 = Math.min((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());
        int x2 = Math.max((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
        int y2 = Math.max((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
        int z2 = Math.max((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());

        int blockId = -1;

        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                for (int k = z1; k <= z2; k++) {
                    blockId++;

                    BlockState blockState = plot.getBufferBuildBlock().get(blockId);
                    //noinspection deprecation
                    player.getWorld().getBlockAt(i, j, k).setTypeIdAndData(blockState.getTypeId(), blockState.getRawData(), false);
                }
            }
        }
    }
}
