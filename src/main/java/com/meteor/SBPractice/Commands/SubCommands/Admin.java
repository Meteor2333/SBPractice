package com.meteor.SBPractice.Commands.SubCommands;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.PlotStatus;
import com.meteor.SBPractice.Utils.ItemStackBuilder;
import com.meteor.SBPractice.Utils.Message;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Admin extends SubCommand {
    private static ArrayList<UUID> adminList = new ArrayList<>();

    public Admin(MainCommand parent, String name) {
        super(parent, name, true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = ((Player) sender);
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
        if (adminList.contains(player.getUniqueId())) {
            for (Plot plot : Plot.getPlots()) {
                if (plot.getPlotStatus().equals(PlotStatus.NOT_OCCUPIED)) {
                    plot.setPlotStatus(PlotStatus.OCCUPIED);
                    plot.setPlayer(player);
                    plot.stopTimer();
                    plot.setTime(0D);
                    plot.canStart(true);
                    player.getInventory().setArmorContents(null);
                    player.getInventory().clear();
                    player.getInventory().setItem(8, new ItemStackBuilder(Material.SNOW_BALL).toItemStack());
                    player.updateInventory();
                    player.setAllowFlight(true);
                    player.setExp(0.0F);
                    player.setFireTicks(0);
                    player.setFlying(false);
                    player.setFoodLevel(20);
                    player.setGameMode(GameMode.CREATIVE);
                    player.setHealth(20.0D);
                    player.setLevel(0);
                    int x1 = Math.min((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
                    int y1 = Math.min((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
                    int z1 = Math.min((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());
                    int x2 = Math.max((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
                    int y2 = Math.max((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
                    int z2 = Math.max((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());

                    List<BlockState> blocks = new ArrayList<>();
                    for (int i = x1; i <= x2; i++) {
                        for (int j = y1; j <= y2; j++) {
                            for (int k = z1; k <= z2; k++) {
                                Block block = plot.getSpawnPoint().getWorld().getBlockAt(i, j, k);
                                blocks.add(block.getState());
                            }
                        }
                    } plot.setBufferBuildBlock(blocks);
                    plot.displayAction();
                    player.teleport(plot.getSpawnPoint());
                    player.sendMessage(Message.getMessage("admin-mode-disabled"));
                    adminList.remove(player.getUniqueId());
                    return;
                }
            } player.sendMessage(Message.getMessage("plot-full"));
        } else {
            adminList.add(player.getUniqueId());
            player.setAllowFlight(true);
            player.sendMessage(Message.getMessage("admin-mode-enabled"));

            Plot plot = Plot.getPlotByPlayer(player);
            if (plot == null) {
                plot = Plot.getPlotByGuest(player);
                if (plot != null) {
                    plot.removeGuest(player);
                } return;
            }
            int x1 = Math.min((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
            int y1 = Math.min((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
            int z1 = Math.min((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());
            int x2 = Math.max((int) plot.getFirstPoint().getX(), (int) plot.getSecondPoint().getX());
            int y2 = Math.max((int) plot.getFirstPoint().getY(), (int) plot.getSecondPoint().getY());
            int z2 = Math.max((int) plot.getFirstPoint().getZ(), (int) plot.getSecondPoint().getZ());
            for (int i = x1; i <= x2; i++) {
                for (int j = y1; j <= y2; j++) {
                    for (int k = z1; k <= z2; k++) {

                        //Bugs #5
                        if (j == y1) {
                            plot.getSpawnPoint().getWorld().getBlockAt(i, j - 1, k).setType(Material.valueOf(Main.getPlugin().getConfig().getString("default-platform-block")));
                        } plot.getSpawnPoint().getWorld().getBlockAt(i, j, k).setType(Material.AIR);
                    }
                }
            } plot.setPlotStatus(PlotStatus.NOT_OCCUPIED);
            plot.outAction();
            plot.stopTimer();
            plot.setTime(0D);
            plot.canStart(true);
            plot.setPlayer(null);
        }
    }

    public static boolean check(Player player) {
        return adminList.contains(player.getUniqueId());
    }

    public static void removeFromAdminList(UUID key) {
        adminList.remove(key);
    }
}
