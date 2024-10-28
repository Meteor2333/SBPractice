package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Utils.Region;
import com.meteor.SBPractice.Utils.VersionSupport;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Platform extends SubCommand {
    public Platform(MainCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        SBPPlayer player = SBPPlayer.getPlayer((Player) sender);
        if (player == null) return;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                player.sendMessage(Messages.CANNOT_DO_THAT.getMessage());
                return;
            }
        }

        Region region = plot.getRegion();
        List<BlockState> blocks = new ArrayList<>();
        new Region(
                new Location(region.getWorld(), region.getXMax(), region.getYMin(), region.getZMax()),
                new Location(region.getWorld(), region.getXMin(), region.getYMin(), region.getZMin())
        ).getBlocks().forEach(block -> {
            BlockState blockState = block.getState();
            if (blockState.getType() == Material.AIR) blockState.setType(Material.valueOf(Main.getPlugin().getConfig().getString("default-platform-block")));
            blocks.add(blockState);
        });
        new Region(
                new Location(region.getWorld(), region.getXMax(), region.getYMin() - 1, region.getZMax()),
                new Location(region.getWorld(), region.getXMin(), region.getYMin() - 1, region.getZMin())
        ).setBlocks(blocks);

        player.playSound(VersionSupport.SOUND_ORB_PICKUP.getForCurrentVersionSupport());
        player.sendMessage(Messages.PLATFORM_ADAPT.getMessage());
    }
}
