package com.meteor.SBPractice.Commands.SubCommands;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Utils.Message;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BufferBuild extends SubCommand {
    public BufferBuild(MainCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Plot plot = Plot.getPlotByPlayer(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot != null) {
                player.sendMessage(Message.getMessage("cannot-buffer-build"));
            } return;
        } plot.stopTimer();

        int x1 = Math.min((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
        int y1 = Math.min((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
        int z1 = Math.min((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());
        int x2 = Math.max((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
        int y2 = Math.max((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
        int z2 = Math.max((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());

        List<BlockState> blocks = new ArrayList<>();
        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                for (int k = z1; k <= z2; k++) {
                    Block block = plot.getSpawnPoint().getWorld().getBlockAt(i, j, k);
                    blocks.add(block.getState());
                }
            }
        } plot.setBufferBuildBlock(blocks);
        player.sendMessage(Message.getMessage("successful-buffer"));
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
    }
}
