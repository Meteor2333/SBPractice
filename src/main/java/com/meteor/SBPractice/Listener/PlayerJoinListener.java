package com.meteor.SBPractice.Listener;

import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.PlotStatus;
import com.meteor.SBPractice.Utils.ItemStackBuilder;
import com.meteor.SBPractice.Utils.Message;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        for (Plot plot : Plot.getPlots()) {
            if (plot.getPlotStatus().equals(PlotStatus.NOT_OCCUPIED)) {
                plot.setPlotStatus(PlotStatus.OCCUPIED);
                plot.setPlayer(player);
                plot.stopTimer();
                plot.setTime(0D);
                plot.canStart(true);
                player.getInventory().setArmorContents(null);
                player.getInventory().clear();
                player.getInventory().setItem(8, new ItemStackBuilder(Material.SNOW_BALL).toItemStack());
                player.updateInventory();
                player.setAllowFlight(true);
                player.setExp(0.0F);
                player.setFireTicks(0);
                player.setFlying(false);
                player.setFoodLevel(20);
                player.setGameMode(GameMode.CREATIVE);
                player.setHealth(20.0D);
                player.setLevel(0);
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
                plot.displayAction();
                player.teleport(plot.getSpawnPoint());
                return;
            }
        } player.setAllowFlight(true);
        player.setExp(0.0F);
        player.setFireTicks(0);
        player.setFlying(false);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(20.0D);
        player.setLevel(0);
        player.sendMessage(Message.getMessage("plot-full"));
        player.teleport(player.getWorld().getSpawnLocation());
    }
}
