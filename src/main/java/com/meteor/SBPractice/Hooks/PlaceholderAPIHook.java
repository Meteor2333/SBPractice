package com.meteor.SBPractice.Hooks;

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
        Plot plot = Plot.getPlotByOwner((Player) player);
        if (plot == null) {
            plot = Plot.getPlotByGuest((Player) player);
        }

        switch (params) {
            case "destroyed":
                return String.valueOf(Main.getRemoteDatabase().getDestructions(player.getUniqueId()));
            case "placed":
                return String.valueOf(Main.getRemoteDatabase().getPlacements(player.getUniqueId()));
            case "restored":
                return String.valueOf(Main.getRemoteDatabase().getRestores(player.getUniqueId()));
            case "owner":
                if (plot != null) return String.valueOf(plot.getPlayer().getName());
                else return "None";
            case "plot-total":
                return String.valueOf(Plot.getPlots().size());
            case "plot-total-player":
                if (plot != null) return String.valueOf(plot.getGuests().size() + 1);
                else return "None";
            case "plot-occupied":
                int sum = 0;
                for (Plot p : Plot.getPlots()) {
                    if (p.getPlotStatus().equals(Plot.PlotStatus.OCCUPIED)) sum++;
                } return String.valueOf(sum);
            case "current-time":
                if (plot != null) return String.format("%.3f", plot.getTime()) + (plot.getCountdown() == 0 ? "" : " " + Messages.getMessage("countdown-mode"));
                else return String.format("%.3f", 0F);
            case "current-blocks":
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
