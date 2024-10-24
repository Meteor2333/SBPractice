package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
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
        Player player = (Player) sender;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                player.sendMessage(Messages.getMessage("cannot-do-that"));
                return;
            }
        }

        for (Entity entity : plot.getSpawnPoint().getWorld().getEntities()) {
            if (!plot.getRegion().isInside(entity.getLocation(), false)) continue;
            EntityType type = entity.getType();
            if (type == EntityType.PLAYER || type == EntityType.ITEM_FRAME || type == EntityType.PAINTING) {
                continue;
            } entity.remove();
        }

        plot.getRegion().fill(Material.AIR);
        plot.stopTimer();
        plot.canStart(true);
    }
}
