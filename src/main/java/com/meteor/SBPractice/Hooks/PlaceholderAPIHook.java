package com.meteor.SBPractice.Hooks;

import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.PlotStatus;
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

        return switch (params) {
            case "destroyed" -> String.valueOf(Main.getRemoteDatabase().getDestructions(player.getUniqueId()));
            case "placed" -> String.valueOf(Main.getRemoteDatabase().getPlacements(player.getUniqueId()));
            case "restored" -> String.valueOf(Main.getRemoteDatabase().getRestores(player.getUniqueId()));
            case "plot-total" -> String.valueOf(Plot.getPlots().size());
            case "plot-occupied" -> {
                int sum = 0;
                for (Plot plot : Plot.getPlots()) {
                    if (plot.getPlotStatus().equals(PlotStatus.OCCUPIED)) sum++;
                } yield String.valueOf(sum);
            }
            case "current-time" -> {
                Plot plot = Plot.getPlotByPlayer((Player) player);
                if (plot == null) plot = Plot.getPlotByGuest((Player) player);
                if (plot == null) yield String.format("%.3f", 0F);
                else yield String.format("%.3f", plot.getTime());
            } case "current-blocks" -> {
                int num = 0;
                Plot plot = Plot.getPlotByPlayer((Player) player);
                if (plot == null) plot = Plot.getPlotByGuest((Player) player);
                if (plot != null) {
                    for (BlockState bs : plot.getBufferBuildBlock()) {
                        if (bs != null && bs.getType() != Material.AIR) num++;
                    }
                } yield String.valueOf(num);
            } default -> null;
        };
    }
}
