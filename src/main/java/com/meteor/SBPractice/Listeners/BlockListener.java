package com.meteor.SBPractice.Listeners;

import com.meteor.SBPractice.Commands.SubCommands.Main.Admin;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Utils.NMSSupport;
import com.meteor.SBPractice.Utils.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.material.Bed;

public class BlockListener implements Listener {
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Player player = e.getPlayer();
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                if (!Admin.check(player)) e.setCancelled(true);
                return;
            }
        }
        if (!plot.getSpawnPoint().getWorld().equals(player.getWorld())) return;
        if (!checkArea(plot, e.getBlockClicked().getRelative(e.getBlockFace()).getLocation())) {
            e.setCancelled(true);
            return;
        }

        plot.startTimer();
        checkBuild(player, plot);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        Player player = e.getPlayer();
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                if (!Admin.check(player)) e.setCancelled(true);
                return;
            }
        }
        if (!plot.getSpawnPoint().getWorld().equals(player.getWorld())) return;
        if (!checkArea(plot, e.getBlockClicked().getRelative(e.getBlockFace()).getLocation())) {
            e.setCancelled(true);
            return;
        }

        Plot finalPlot = plot;
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (checkFull(finalPlot, e.getBlockClicked().getRelative(e.getBlockFace()))) {
                finalPlot.stopTimer();
                finalPlot.canStart(true);
            } checkBuild(player, finalPlot);
        }, 1L);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                if (!Admin.check(player)) e.setCancelled(true);
                return;
            }
        }
        if (!plot.getSpawnPoint().getWorld().equals(player.getWorld())) return;
        if (!checkArea(plot, e.getBlockPlaced().getLocation())) {
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
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                if (!Admin.check(player)) e.setCancelled(true);
                return;
            }
        }
        if (!plot.getSpawnPoint().getWorld().equals(player.getWorld())) return;
        if (!checkArea(plot, e.getBlock().getLocation())) {
            e.setCancelled(true);
            return;
        }

        Plot finalPlot = plot;
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (checkFull(finalPlot, e.getBlock())) {
                finalPlot.stopTimer();
                finalPlot.canStart(true);
            } checkBuild(player, finalPlot);
        }, 1L);
        Main.getRemoteDatabase().setDestructions(player.getUniqueId(), Main.getRemoteDatabase().getDestructions(player.getUniqueId()) + 1);
    }

    private static boolean checkArea(Plot plot, Location location) {
        //Fix bed
        if (plot.getRegion().isInside(location, false)) {
            if (location.getBlock().getType().equals(Material.BED)) {
                Bed bed = (Bed) location.getBlock().getState().getData();
                if (bed.isHeadOfBed()) return true;
                switch (bed.getFacing()) {
                    case NORTH:
                        return plot.getRegion().isInside(location.add(0, 0, -1), false);
                    case EAST:
                        return plot.getRegion().isInside(location.add(1, 0, 0), false);
                    case SOUTH:
                        return plot.getRegion().isInside(location.add(0, 0, 1), false);
                    case WEST:
                        return plot.getRegion().isInside(location.add(-1, 0, 0), false);
                    default:
                }
            } return true;
        } return false;
    }

    private static boolean checkFull(Plot plot, Block notCheckedBlock) {
        boolean isFull = true;

        for (Block block : plot.getRegion().getBlocks()) {
            if (!block.getType().equals(Material.AIR) && !block.equals(notCheckedBlock)) isFull = false;
        }
        return isFull;
    }

    private static void checkBuild(Player pl, Plot plot) {
        if (plot.timeIsNull()) return;
        Player player = plot.getPlayer();
        if (player == null) return;

        boolean isFull = true;
        boolean isPerfect = true;


        for (int i = 0; i < plot.getRegion().getBlockCount(); i++) {
            BlockState blockState = plot.getRegion().getBlocks().get(i).getState();
            if (blockState == null) continue;

            if (!blockState.getType().equals(Material.AIR)) isFull = false;
            if (blockState.getData().equals(plot.getBufferBuildBlock().get(i).getData())) continue;

            isPerfect = false;
        }

        if (isPerfect && !isFull) {
            plot.canStart(false);
            plot.stopTimer();

            for (Player p : Bukkit.getOnlinePlayers()) {
                Region region = plot.getRegion();
                int range = Main.getPlugin().getConfig().getInt("plot-check-add-range");
                if (new Region(
                        new Location(region.getWorld(), region.getXMax() + range, 0, region.getZMax() + range),
                        new Location(region.getWorld(), region.getXMin() - range, 0, region.getZMin() - range)
                ).isInside(p.getLocation(), true)) {
                    p.playSound(p.getLocation(), Sound.LEVEL_UP, 1.0F, 1.0F);
                    p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);
                    NMSSupport.sendTitle(p, Messages.getMessage("perfect-match-title"), (plot.getTime() < 0 && plot.getCountdown() != 0) ? (Messages.getMessage("timeout-perfect-match-subtitle").replace("%time%", String.format("%.3f", Math.abs(plot.getTime())))) : (Messages.getMessage("perfect-match-subtitle").replace("%time%", String.format("%.3f", plot.getTime())) + (plot.getCountdown() == 0 ? "" : " " + Messages.getMessage("countdown-mode"))), 0, 40, 10);
                }
            } Main.getRemoteDatabase().setRestores(pl.getUniqueId(), Main.getRemoteDatabase().getRestores(pl.getUniqueId()) + 1);
        }
    }
}
