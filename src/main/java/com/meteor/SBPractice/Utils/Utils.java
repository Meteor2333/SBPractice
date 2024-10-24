package com.meteor.SBPractice.Utils;

import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Utils {
    public static Location simplifyLocation(Location location) {
        Location loc = location.clone();
        loc.setX(location.getBlockX() + 0.5);
        loc.setY(Double.parseDouble(String.format("%.1f", location.getY())));
        loc.setZ(location.getBlockZ() + 0.5);
        loc.setYaw(Float.parseFloat(String.format("%.1f", location.getYaw())));
        loc.setPitch(Float.parseFloat(String.format("%.1f", location.getPitch())));
        return loc;
    }

    public static String formatLocation(Location location) {
        StringBuilder builder = new StringBuilder();
        builder.append(location.getWorld().getName()).append(":").append(location.getX()).append(":").append(location.getY()).append(":").append(location.getZ());
        if (location.getYaw() != 0.0F && location.getPitch() != 0.0F) {
            builder.append(":").append(location.getYaw()).append(":").append(location.getPitch());
        }

        return builder.toString();
    }

    public static Location parseLocation(String location) {
        String[] split = location.split(":");
        return split.length == 4 ? new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3])) : new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
    }

    public static void updatePlots() {
        for (Plot plot : Plot.getPlots()) {
            if (plot.getPlayer() == null) resetPlot(plot);
            else if (!plot.getPlayer().isOnline()) resetPlot(plot);
            for (Player player : plot.getGuests()) {
                if (!player.isOnline()) plot.removeGuest(player);
            }
        }
    }

    public static void resetPlot(Plot plot) {
        Region region = plot.getRegion();
        region.fill(Material.AIR);
        new Region(
                new Location(region.getWorld(), region.getXMax(), region.getYMin() - 1, region.getZMax()),
                new Location(region.getWorld(), region.getXMin(), region.getYMin() - 1, region.getZMin())
        ).fill(Material.valueOf(Main.getPlugin().getConfig().getString("default-platform-block")));
        plot.setPlotStatus(Plot.PlotStatus.NOT_OCCUPIED);
        plot.outAction();
        plot.stopTimer();
        plot.setTime(0D);
        plot.setCountdown(0);
        plot.canStart(true);
        plot.setPlayer(null);
    }

    public static void refreshAllPlayerVisibility() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Plot plot = Plot.getPlotByOwner(player);
            if (plot == null) {
                plot = Plot.getPlotByGuest(player);
                if (plot == null) continue;
            }

            Region region = plot.getRegion();
            int range = Main.getPlugin().getConfig().getInt("plot-check-add-range");
            if (new Region(
                    new Location(region.getWorld(), region.getXMax() + range, 0, region.getZMax() + range),
                    new Location(region.getWorld(), region.getXMin() - range, 0, region.getZMin() - range)
            ).isInside(player.getLocation(), true)) NMSSupport.showPlayer(player, false);
            else NMSSupport.hidePlayer(player, false);
        }
    }
}
