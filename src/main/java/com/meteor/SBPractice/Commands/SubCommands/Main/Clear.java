package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Utils.Region;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class Clear extends SubCommand {
    public Clear(MainCommand parent, String name) {
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

        for (Entity entity : plot.getSpawnPoint().getWorld().getEntities()) {
            if (!plot.getRegion().isInside(entity.getLocation(), false)) continue;
            if (entity.getType() == EntityType.PLAYER) continue;
            entity.remove();
        }

        Region region = plot.getRegion();
        new Region(
                new Location(region.getWorld(), region.getXMax(), region.getYMax() + 1, region.getZMax()),
                new Location(region.getWorld(), region.getXMin(), region.getYMin(), region.getZMin())
        ).fill(Material.AIR);
        plot.stopTimer();
        plot.setCanStart(true);
    }
}
