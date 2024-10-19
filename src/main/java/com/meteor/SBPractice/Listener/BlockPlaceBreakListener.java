package com.meteor.SBPractice.Listener;

import com.meteor.SBPractice.Commands.SubCommands.Admin;
import com.meteor.SBPractice.Commands.SubCommands.Spectator;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Utils.Message;
import com.meteor.SBPractice.Utils.NMSSupport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class BlockPlaceBreakListener implements Listener {
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Player player = e.getPlayer();
        Plot plot = Plot.getPlotByPlayer(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                if (!Admin.check(player)) e.setCancelled(true);
                return;
            }
        }
        if (!plot.getSpawnPoint().getWorld().equals(player.getWorld())) return;
        if (checkArea(plot, e.getBlockClicked().getRelative(e.getBlockFace()).getLocation())) {
            e.setCancelled(true);
            return;
        }

        plot.startTimer();
        checkBuild(player, plot);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        Player player = e.getPlayer();
        Plot plot = Plot.getPlotByPlayer(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                if (!Admin.check(player)) e.setCancelled(true);
                return;
            }
        }
        if (!plot.getSpawnPoint().getWorld().equals(player.getWorld())) return;
        if (checkArea(plot, e.getBlockClicked().getRelative(e.getBlockFace()).getLocation())) {
            e.setCancelled(true);
            return;
        }

        Plot finalPlot = plot;
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (checkFull(player, finalPlot, e.getBlockClicked().getRelative(e.getBlockFace()))) {
                finalPlot.stopTimer();
                finalPlot.canStart(true);
            } checkBuild(player, finalPlot);
        }, 1L);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Plot plot = Plot.getPlotByPlayer(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                if (!Admin.check(player)) e.setCancelled(true);
                return;
            }
        }
        if (!plot.getSpawnPoint().getWorld().equals(player.getWorld())) return;
        if (checkArea(plot, e.getBlockPlaced().getLocation())) {
            e.setCancelled(true);
            return;
        }

        plot.startTimer();
        checkBuild(player, plot);
        Main.getRemoteDatabase().setPlacements(player.getUniqueId(), Main.getRemoteDatabase().getPlacements(player.getUniqueId()) + 1);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Plot plot = Plot.getPlotByPlayer(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                if (!Admin.check(player)) e.setCancelled(true);
                return;
            }
        }
        if (!plot.getSpawnPoint().getWorld().equals(player.getWorld())) return;
        if (checkArea(plot, e.getBlock().getLocation())) {
            e.setCancelled(true);
            return;
        }

        Plot finalPlot = plot;
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (checkFull(player, finalPlot, e.getBlock())) {
                finalPlot.stopTimer();
                finalPlot.canStart(true);
            } checkBuild(player, finalPlot);
        }, 1L);
        Main.getRemoteDatabase().setDestructions(player.getUniqueId(), Main.getRemoteDatabase().getDestructions(player.getUniqueId()) + 1);
    }

    private static boolean checkArea(Plot plot, Location location) {
        int x1 = Math.min((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
        int y1 = Math.min((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
        int z1 = Math.min((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());
        int x2 = Math.max((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
        int y2 = Math.max((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
        int z2 = Math.max((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());
        return location.getBlockX() > x2 || location.getBlockX() < x1 || location.getBlockY() > y2 || location.getBlockY() < y1 || location.getBlockZ() > z2 || location.getBlockZ() < z1;
    }

    private static boolean checkFull(Player player, Plot plot, Block notCheckedBlock) {
        int x1 = Math.min((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
        int y1 = Math.min((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
        int z1 = Math.min((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());
        int x2 = Math.max((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
        int y2 = Math.max((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
        int z2 = Math.max((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());

        boolean isFull = true;

        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                for (int k = z1; k <= z2; k++) {
                    if (!player.getWorld().getBlockAt(i, j, k).getType().equals(Material.AIR) && !player.getWorld().getBlockAt(i, j, k).equals(notCheckedBlock)) isFull = false;
                }
            }
        } return isFull;
    }

    private static void checkBuild(Player pl, Plot plot) {
        if (plot.timeIsNull()) return;
        Player player = plot.getPlayer();
        if (player == null) return;
        int x1 = Math.min((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
        int y1 = Math.min((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
        int z1 = Math.min((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());
        int x2 = Math.max((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
        int y2 = Math.max((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
        int z2 = Math.max((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());

        int blockId = -1;

        //Bugs #6
        boolean isFull = true;
        boolean isPerfect = true;

        for (int i = x1; i <= x2; i++) {
            for (int j = y1; j <= y2; j++) {
                for (int k = z1; k <= z2; k++) {
                    blockId++;

                    BlockState blockState = player.getWorld().getBlockAt(i, j, k).getState();
                    if (blockState == null) continue;

                    if (!blockState.getType().equals(Material.AIR)) isFull = false;
                    if (blockState.getData().equals(plot.getBufferBuildBlock().get(blockId).getData())) continue;

                    isPerfect = false;
                }
            }
        }

        if (isPerfect && !isFull) {
            //Bugs #3
            plot.canStart(false);
            plot.stopTimer();

            //Add #5
            for (Entity en : player.getWorld().getNearbyEntities(player.getLocation(), 15D, 15D, 15D)) {
                if (en instanceof Player p) {
                    if (p.equals(player) || Spectator.check(p) || Admin.check(p) || plot.getGuests().contains(p)) {
                        p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
                        p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);
                        NMSSupport.sendTitle(p, Message.getMessage("perfect-match-title"), Message.getMessage("perfect-match-subtitle").replace("%time%", String.format("%.3f", plot.getTime())), 0, 40, 10);
                    }
                }
            } Main.getRemoteDatabase().setRestores(pl.getUniqueId(), Main.getRemoteDatabase().getRestores(pl.getUniqueId()) + 1);
        }
    }
}
