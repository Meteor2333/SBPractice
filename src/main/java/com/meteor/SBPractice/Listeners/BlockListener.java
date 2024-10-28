package com.meteor.SBPractice.Listeners;

import com.meteor.SBPractice.Api.Events.PlayerPerfectRestoreEvent;
import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.SubCommands.Main.Admin;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Utils.VersionSupport;
import com.meteor.SBPractice.Utils.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.material.Bed;

public class BlockListener implements Listener {
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        SBPPlayer player = SBPPlayer.getPlayer(e.getPlayer());
        if (player == null) return;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                if (!Admin.getAdminList().contains(player.getName())) e.setCancelled(true);
                return;
            }
        } if (!checkArea(plot, e.getBlockClicked().getRelative(e.getBlockFace()))) {
            e.setCancelled(true);
            return;
        }

        plot.startTimer();
        checkBuild(player, plot);
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        SBPPlayer player = SBPPlayer.getPlayer(e.getPlayer());
        if (player == null) return;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                if (!Admin.getAdminList().contains(player.getName())) e.setCancelled(true);
                return;
            }
        } if (!checkArea(plot, e.getBlockClicked().getRelative(e.getBlockFace()))) {
            e.setCancelled(true);
            return;
        }

        Plot finalPlot = plot;
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (checkFull(finalPlot, e.getBlockClicked().getRelative(e.getBlockFace()))) {
                finalPlot.stopTimer();
                finalPlot.setCanStart(true);
            } checkBuild(player, finalPlot);
        }, 1L);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        SBPPlayer player = SBPPlayer.getPlayer(e.getPlayer());
        if (player == null) return;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                if (!Admin.getAdminList().contains(player.getName())) e.setCancelled(true);
                return;
            }
        } if (!checkArea(plot, e.getBlockPlaced())) {
            e.setCancelled(true);
            return;
        }

        plot.startTimer();
        checkBuild(player, plot);
        player.getStats().setPlaceBlocks(player.getStats().getPlaceBlocks() + 1);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        SBPPlayer player = SBPPlayer.getPlayer(e.getPlayer());
        if (player == null) return;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                if (!Admin.getAdminList().contains(player.getName())) e.setCancelled(true);
                return;
            }
        } if (!checkArea(plot, e.getBlock())) {
            e.setCancelled(true);
            return;
        }

        Plot finalPlot = plot;
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (checkFull(finalPlot, e.getBlock())) {
                finalPlot.stopTimer();
                finalPlot.setCanStart(true);
            } checkBuild(player, finalPlot);
        }, 1L);
        player.getStats().setBreakBlocks(player.getStats().getBreakBlocks() + 1);
    }

    @EventHandler
    public void onBlockExtendPiston(BlockPistonExtendEvent e) {
        if (e.getBlock().getWorld().getPlayers() != null) e.setCancelled(true);
    }

    @EventHandler
    public void onBlockRetractPiston(BlockPistonRetractEvent e) {
        if (e.getBlock().getWorld().getPlayers() != null) e.setCancelled(true);
    }

    @EventHandler
    public void onLiquidFlow(BlockFromToEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent e) {
        e.setCancelled(true);
    }

    private static boolean checkArea(Plot plot, Block block) {

        //Fix bed
        if (plot.getRegion().isInside(block.getLocation(), false)) {
            if (block.getState().getData() instanceof Bed) {
                Bed bed = (Bed) block.getState().getData();
                switch (bed.getFacing()) {
                    case NORTH:
                        return plot.getRegion().isInside(block.getLocation().add(0, 0, -1), false);
                    case EAST:
                        return plot.getRegion().isInside(block.getLocation().add(1, 0, 0), false);
                    case SOUTH:
                        return plot.getRegion().isInside(block.getLocation().add(0, 0, 1), false);
                    case WEST:
                        return plot.getRegion().isInside(block.getLocation().add(-1, 0, 0), false);
                    default: return false;
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

    private static void checkBuild(SBPPlayer pl, Plot plot) {
        if (plot.getCurrentTime() == 0L) return;
        SBPPlayer player = plot.getPlayer();
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
            PlayerPerfectRestoreEvent event;
            Bukkit.getPluginManager().callEvent(event = new PlayerPerfectRestoreEvent(plot, pl, Double.parseDouble(String.format("%.3f", (plot.getTime() < 0 && plot.getCountdown() != 0) ? Math.abs(plot.getTime()) : plot.getTime())), plot.getCountdown() != 0));
            if (event.isCancelled()) return;

            plot.setCanStart(false);
            plot.stopTimer();

            for (SBPPlayer p : SBPPlayer.getPlayers()) {
                Region region = plot.getRegion();
                int range = Main.getPlugin().getConfig().getInt("plot-check-add-range");
                if (new Region(
                        new Location(region.getWorld(), region.getXMax() + range, 0, region.getZMax() + range),
                        new Location(region.getWorld(), region.getXMin() - range, 0, region.getZMin() - range)
                ).isInside(p.getLocation(), true)) {
                    p.playSound(VersionSupport.SOUND_LEVEL_UP.getForCurrentVersionSupport());
                    p.playSound(VersionSupport.SOUND_NOTE_PLING.getForCurrentVersionSupport());
                    VersionSupport.sendTitle(p.getPlayer(), Messages.PERFECT_MATCH_TITLE.getMessage(), (plot.getTime() < 0 && plot.getCountdown() != 0) ? (Messages.TIMEOUT_PERFECT_MATCH_SUBTITLE.getMessage()).replace("%time%", String.format("%.3f", Math.abs(plot.getTime()))) : (Messages.PERFECT_MATCH_SUBTITLE.getMessage().replace("%time%", String.format("%.3f", plot.getTime())) + (plot.getCountdown() == 0 ? "" : " " + Messages.COUNTDOWN_MODE.getMessage())), 0, 40, 10);
                }
            } pl.getStats().setRestores(pl.getStats().getRestores() + 1);
        }
    }
}
