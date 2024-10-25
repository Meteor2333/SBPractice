package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Utils.NMSSupport;
import com.meteor.SBPractice.Utils.Region;
import com.meteor.SBPractice.Utils.Utils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PreStart extends SubCommand {
    private static final Map<UUID, Long> cooldowns = new HashMap<>();

    public PreStart(MainCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        SBPPlayer player = SBPPlayer.getPlayer((Player) sender);
        if (player == null) return;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            player.sendMessage(Messages.CANNOT_DO_THAT.getMessage());
            return;
        }

        if (cooldowns.containsKey(player.getPlayer().getUniqueId()))
            if (System.currentTimeMillis() - cooldowns.get(player.getPlayer().getUniqueId()) <= 5000) return;
        cooldowns.put(player.getPlayer().getUniqueId(), System.currentTimeMillis());
        plot.getRegion().setBlocks(plot.getBufferBuildBlock());
        new BukkitRunnable() {
            int count = 4;

            public void run() {
                if (--this.count <= 0) {
                    plot.getRegion().fill(Material.AIR);
                    plot.setCanStart(true);
                    plot.setCurrentTime(0L);
                    plot.startTimer();
                    this.cancel();
                } for (SBPPlayer p : SBPPlayer.getPlayers()) {
                    Region region = plot.getRegion();
                    int range = Main.getPlugin().getConfig().getInt("plot-check-add-range");
                    if (new Region(
                            new Location(region.getWorld(), region.getXMax() + range, 0, region.getZMax() + range),
                            new Location(region.getWorld(), region.getXMin() - range, 0, region.getZMin() - range)
                    ).isInside(p.getLocation(), true)) {
                        if (count <= 0) {
                            NMSSupport.sendTitle(p.getPlayer(), null, null, 0, 0, 0);
                            new BukkitRunnable() {
                                int remaining = 3;
                                public void run() {
                                    p.playSound(Utils.Sounds.NOTE_PLING);
                                    if (--this.remaining <= 0) this.cancel();
                                }
                            }.runTaskTimer(Main.getPlugin(), 0, 2);
                        } else {
                            p.playSound(Utils.Sounds.NOTE_STICKS);
                            p.sendMessage(Messages.PRE_START.getMessage().replace("%time%", String.valueOf(count)));
                            NMSSupport.sendTitle(p.getPlayer(), ChatColor.AQUA + "" + ChatColor.BOLD + count, null, 0, 20, 0);
                        }
                    }
                }
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 20L);
    }
}
