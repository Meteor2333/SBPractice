package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Platform extends SubCommand {
    public Platform(MainCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                player.sendMessage(Messages.getMessage("cannot-do-that"));
                return;
            }
        }

        for (int i = plot.getRegion().getXMin(); i <= plot.getRegion().getXMax(); i++) {
            for (int j = plot.getRegion().getYMin(); j <= plot.getRegion().getXMax(); j++) {
                for (int k = plot.getRegion().getZMin(); k <= plot.getRegion().getZMax(); k++) {
                    if (j == plot.getRegion().getYMin()) {
                        World world = plot.getSpawnPoint().getWorld();
                        BlockState blockState = world.getBlockAt(i, j, k).getState();
                        if (blockState.getType().equals(Material.AIR)) blockState.setType(Material.valueOf(Main.getPlugin().getConfig().getString("default-platform-block")));
                        //noinspection deprecation
                        world.getBlockAt(i, j - 1, k).setTypeIdAndData(blockState.getTypeId(), blockState.getRawData(), false);
                    }
                }
            }
        } player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
        player.sendMessage(Messages.getMessage("platform-adapt"));
    }
}
