package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Utils.VersionSupport;
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
        SBPPlayer player = SBPPlayer.getPlayer((Player) sender);
        if (player == null) return;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            player.sendMessage(Messages.CANNOT_DO_THAT.getMessage());
            return;
        } plot.stopTimer();

        List<BlockState> blocks = new ArrayList<>();
        for (Block block : plot.getRegion().getBlocks()) {
            blocks.add(block.getState());
        } plot.setBufferBuildBlock(blocks);
        player.sendMessage(Messages.SUCCESSFUL_BUFFER.getMessage());
        player.playSound(VersionSupport.SOUND_ORB_PICKUP.getForCurrentVersionSupport());
    }
}
