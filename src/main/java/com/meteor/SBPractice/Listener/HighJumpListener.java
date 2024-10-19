package com.meteor.SBPractice.Listener;

import com.meteor.SBPractice.Commands.SubCommands.Admin;
import com.meteor.SBPractice.Commands.SubCommands.Spectator;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HighJumpListener implements Listener {
    private static final ArrayList<String> cooldowns = new ArrayList<>();
    private static Map<UUID, Double> intensity = new HashMap<>();
    private static Map<UUID, Boolean> flyToggled = new HashMap<>();

    @EventHandler
    public static void onPlayerToggleFly(PlayerToggleFlightEvent e) {
        if (!e.isFlying()) return;
        Player player = e.getPlayer();
        Plot plot = Plot.getPlotByPlayer(e.getPlayer());
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) return;
        }
        if (Admin.check(player)) return;
        if (Spectator.check(player)) return;

        Location loc = player.getLocation();
        int x1 = Math.min((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX()) - 3;
        int y1 = Math.min((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
        int z1 = Math.min((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ()) - 3;
        int x2 = Math.max((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX()) + 3;
        int y2 = Math.max((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY()) + 2;
        int z2 = Math.max((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ()) + 3;
        if (!(loc.getBlockX() <= x2 && loc.getBlockX() >= x1 && loc.getBlockY() <= y2 && loc.getBlockY() >= y1 && loc.getBlockZ() <= z2 && loc.getBlockZ() >= z1))
            return;

        if (flyToggled.getOrDefault(player.getUniqueId(), true)) {
            if (!cooldowns.contains(player.getName())) {
                cooldowns.add(player.getName());
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> cooldowns.remove(player.getName()), 20L);

                //Bugs #2
                //wtf bukkit?
                player.playEffect(player.getLocation(), Effect.BLAZE_SHOOT, null);
                player.setVelocity(new Vector(0, -Math.sin(Math.toRadians(-90)), 0).multiply(intensity.getOrDefault(player.getUniqueId(), 1.1D)));
                e.setCancelled(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ());
                        if (block.getType() == Material.AIR) player.setAllowFlight(false);
                        else {
                            player.setAllowFlight(true);
                            this.cancel();
                        }
                    }
                }.runTaskTimer(Main.getPlugin(), 0L, 2L);
            }
        }
    }

    public static void setIntensity(Player player, double value) {
        intensity.put(player.getUniqueId(), value);
    }

    public static void toggled(Player player) {
        if (flyToggled.getOrDefault(player.getUniqueId(), true)) {
            player.sendMessage(Message.getMessage("highjump-disabled"));
            flyToggled.put(player.getUniqueId(), false);
        } else {
            player.sendMessage(Message.getMessage("highjump-enabled"));
            flyToggled.put(player.getUniqueId(), true);
        }
    }
}
