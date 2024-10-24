package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Utils.NMSSupport;
import com.meteor.SBPractice.Utils.Region;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class PreStart extends SubCommand {
    private static Map<Player, Long> cooldowns = new HashMap<>();

    public PreStart(MainCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            player.sendMessage(Messages.getMessage("cannot-do-that"));
            return;
        }

        if (cooldowns.containsKey(player)) if (System.currentTimeMillis() - cooldowns.get(player) <= 5000) return;
        cooldowns.put(player, System.currentTimeMillis());
        new BukkitRunnable() {
            int count = 4;

            public void run() {
                if (--this.count <= 0) {
                    plot.getRegion().fill(Material.AIR);
                    plot.canStart(true);
                    plot.setCurrentTime(0L);
                    plot.startTimer();
                    this.cancel();
                } for (Player p : Bukkit.getOnlinePlayers()) {
                    Region region = plot.getRegion();
                    int range = Main.getPlugin().getConfig().getInt("plot-check-add-range");
                    if (new Region(
                            new Location(region.getWorld(), region.getXMax() + range, 0, region.getZMax() + range),
                            new Location(region.getWorld(), region.getXMin() - range, 0, region.getZMin() - range)
                    ).isInside(p.getLocation(), true)) {
                        if (count <= 0) {
                            NMSSupport.sendTitle(p, null, null, 0, 0, 0);
                            new BukkitRunnable() {
                                int remaining = 3;
                                public void run() {
                                    p.playSound(p.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);
                                    if (--this.remaining <= 0) this.cancel();
                                }
                            }.runTaskTimer(Main.getPlugin(), 0, 2);
                        } else {
                            p.playSound(p.getLocation(), Sound.NOTE_STICKS, 1.0F, 1.0F);
                            p.sendMessage(Messages.getMessage("pre-start").replace("%time%", String.valueOf(count)));
                            NMSSupport.sendTitle(p, ChatColor.AQUA + "" + ChatColor.BOLD + count, null, 0, 20, 0);
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 20L);
    }
}
