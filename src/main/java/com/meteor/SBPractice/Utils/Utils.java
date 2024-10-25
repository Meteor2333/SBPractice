package com.meteor.SBPractice.Utils;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
            else if (Bukkit.getPlayer(plot.getPlayer().getName()) == null) resetPlot(plot);
            for (SBPPlayer player : plot.getGuests()) {
                if (Bukkit.getPlayer(player.getName()) == null) plot.removeGuest(player);
            }
        }
    }

    public static void resetPlot(Plot plot) {
        Region region = plot.getRegion();
        for (Entity entity : plot.getSpawnPoint().getWorld().getEntities()) {
            if (!region.isInside(entity.getLocation(), false)) continue;
            if (entity.getType() == EntityType.PLAYER) continue;
            entity.remove();
        }
        new Region(
                new Location(region.getWorld(), region.getXMax(), region.getYMax() + 1, region.getZMax()),
                new Location(region.getWorld(), region.getXMin(), region.getYMin(), region.getZMin())
        ).fill(Material.AIR);
        new Region(
                new Location(region.getWorld(), region.getXMax(), region.getYMin() - 1, region.getZMax()),
                new Location(region.getWorld(), region.getXMin(), region.getYMin() - 1, region.getZMin())
        ).fill(Material.valueOf(Main.getPlugin().getConfig().getString("default-platform-block")));
        plot.setPlotStatus(Plot.PlotStatus.NOT_OCCUPIED);
        plot.outAction();
        plot.stopTimer();
        plot.setTime(0D);
        plot.setCountdown(0);
        plot.setCanStart(true);
        plot.setPlayer(null);
        plot.getGuests().clear();
    }

    public static void refreshAllPlayerVisibility() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            SBPPlayer player = SBPPlayer.getPlayer(p);
            if (player == null) continue;
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
            ).isInside(player.getPlayer().getLocation(), true)) NMSSupport.showPlayer(p, false);
            else NMSSupport.hidePlayer(p, false);
        }
    }

    public static void playSound(Player player, Sounds sound, float volume, float pitch) {
        String s = sound.v13;
        switch (NMSSupport.getServerVersion().substring(1).split("_")[1]) {
            case "8":
                s = sound.v8;
                break;
            case "9":
            case "10":
            case "11":
            case "12":
                s = sound.v12;
                break;
        } try {
            player.playSound(player.getLocation(), Sound.valueOf(s), volume, pitch);
        } catch (IllegalArgumentException ignored) {}
    }

    @RequiredArgsConstructor
    public enum Sounds {

        ORB_PICKUP("ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP"),
        NOTE_PLING("NOTE_PLING", "BLOCK_NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING"),
        NOTE_STICKS("NOTE_STICKS", "BLOCK_NOTE_HAT", "BLOCK_NOTE_BLOCK_HAT"),
        LEVEL_UP("LEVEL_UP", "ENTITY_PLAYER_LEVELUP", "ENTITY_PLAYER_LEVELUP"),
        BLAZE_SHOOT("GHAST_FIREBALL", "ENTITY_BLAZE_SHOOT", "ENTITY_BLAZE_SHOOT");

        private final String v8, v12, v13;

    }
}
