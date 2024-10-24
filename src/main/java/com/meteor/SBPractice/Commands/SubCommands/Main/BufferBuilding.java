package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BufferBuilding extends SubCommand {
    public BufferBuilding(MainCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            Messages.getMessage("cannot-do-that");
            return;
        } plot.stopTimer();

        List<BlockState> blocks = new ArrayList<>();
        for (Block block : plot.getRegion().getBlocks()) {
            blocks.add(block.getState());
        } plot.setBufferBuildBlock(blocks);
        player.sendMessage(Messages.getMessage("successful-buffer"));
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
    }
}
