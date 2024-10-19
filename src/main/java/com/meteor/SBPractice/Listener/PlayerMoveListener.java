package com.meteor.SBPractice.Listener;

import com.meteor.SBPractice.Commands.SubCommands.Admin;
import com.meteor.SBPractice.Commands.SubCommands.Spectator;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Utils.Message;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Collection;

public class PlayerMoveListener implements Listener {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (Admin.check(player)) return;
        if (Spectator.check(player)) return;
        Plot plot = Plot.getPlotByPlayer(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) return;
        }

        Collection<Entity> entities = player.getWorld().getNearbyEntities(plot.getSpawnPoint(), 20D, 20D, 20D);
        if (entities.contains(player)) return;
        player.teleport(plot.getSpawnPoint());
        player.sendMessage(Message.getMessage("you-cannot-leave"));
        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1F, 1F);
    }
}
