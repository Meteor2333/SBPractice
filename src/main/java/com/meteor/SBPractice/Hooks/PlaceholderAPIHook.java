package com.meteor.SBPractice.Hooks;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;
import com.meteor.SBPractice.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "sbp";
    }

    @Override
    public @NotNull String getAuthor() {
        return " ";
    }

    @Override
    public @NotNull String getVersion() {
        return Main.getPlugin().getDescription().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        SBPPlayer sbpPlayer = SBPPlayer.getPlayer((Player) player);
        Plot plot = null;
        if (sbpPlayer != null) {
            plot = Plot.getPlotByOwner(sbpPlayer);
            if (plot == null) {
                plot = Plot.getPlotByGuest(sbpPlayer);
            }
        }

        switch (params) {
            case "total-break-blocks":
                if (sbpPlayer != null) return String.valueOf(sbpPlayer.getStats().getBreakBlocks());
                return String.valueOf(Main.getRemoteDatabase().getPlayerStats(player.getUniqueId()).getBreakBlocks());
            case "total-place-blocks":
                if (sbpPlayer != null) return String.valueOf(sbpPlayer.getStats().getPlaceBlocks());
                return String.valueOf(Main.getRemoteDatabase().getPlayerStats(player.getUniqueId()).getPlaceBlocks());
            case "total-jumps":
                if (sbpPlayer != null) return String.valueOf(sbpPlayer.getStats().getJumps());
                return String.valueOf(Main.getRemoteDatabase().getPlayerStats(player.getUniqueId()).getJumps());
            case "total-restores":
                if (sbpPlayer != null) return String.valueOf(sbpPlayer.getStats().getRestores());
                return String.valueOf(Main.getRemoteDatabase().getPlayerStats(player.getUniqueId()).getRestores());
            case "total-online-times":
                if (sbpPlayer != null) return String.valueOf(sbpPlayer.getStats().getOnlineTimes());
                return String.valueOf(Main.getRemoteDatabase().getPlayerStats(player.getUniqueId()).getOnlineTimes());
            case "plot-total":
                return String.valueOf(Plot.getPlots().size());
            case "plot-total-occupied":
                int sum1 = 0;
                for (Plot p : Plot.getPlots()) {
                    if (p.getPlotStatus().equals(Plot.PlotStatus.OCCUPIED)) sum1++;
                } return String.valueOf(sum1);
            case "plot-total-not-occupied":
                int sum2 = 0;
                for (Plot p : Plot.getPlots()) {
                    if (p.getPlotStatus().equals(Plot.PlotStatus.NOT_OCCUPIED)) sum2++;
                } return String.valueOf(sum2);
            case "plot-owner":
                if (plot != null) return String.valueOf(plot.getPlayer().getName());
                else return "None";
            case "plot-total-player":
                if (plot != null) return String.valueOf(plot.getGuests().size() + 1);
                else return "None";
            case "current-time":
                if (plot != null) return String.format("%.3f", plot.getTime()) + (plot.getCountdown() == 0 ? "" : " " + Messages.COUNTDOWN_MODE.getMessage());
                else return String.format("%.3f", 0F);
            case "current-building-blocks":
                int num = 0;
                if (plot != null) {
                    for (BlockState bs : plot.getBufferBuildBlock()) {
                        if (bs != null && bs.getType() != Material.AIR) num++;
                    }
                } return String.valueOf(num);
            default: return null;
        }
    }
}
