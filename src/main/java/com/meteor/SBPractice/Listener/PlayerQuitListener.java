package com.meteor.SBPractice.Listener;

import com.meteor.SBPractice.Commands.SubCommands.Admin;
import com.meteor.SBPractice.Commands.SubCommands.Spectator;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.PlotStatus;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Plot plot = Plot.getPlotByPlayer(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot != null) {
                plot.removeGuest(player);
            } return;
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
                    if (j == y1) {
                        plot.getSpawnPoint().getWorld().getBlockAt(i, j - 1, k).setType(Material.valueOf(Main.getPlugin().getConfig().getString("default-platform-block")));
                    } plot.getSpawnPoint().getWorld().getBlockAt(i, j, k).setType(Material.AIR);
                }
            }
        } plot.setPlotStatus(PlotStatus.NOT_OCCUPIED);
        plot.outAction();
        plot.stopTimer();
        plot.setTime(0D);
        plot.canStart(true);
        plot.setPlayer(null);
        Admin.removeFromAdminList(player.getUniqueId());
        Spectator.removeFromSpecList(player.getUniqueId());

        List<Player> guests = plot.getGuests();
        if (guests == null || guests.isEmpty()) return;
        for (Player guest : guests) {
            plot.removeGuest(guest);

        }
    }
}
