package com.meteor.SBPractice.Commands.SubCommands;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Utils.ItemStackBuilder;
import com.meteor.SBPractice.Utils.NMSSupport;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class SetupBuildArea extends SubCommand {
    private int plotId;
    private Location firstPoint, secondPoint;

    public SetupBuildArea(MainCommand parent, String name) {
        super(parent, name, true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "请输入 0 到 " + Integer.MAX_VALUE + " 的整数来表示一个岛屿代号！");
            return;
        }
        Player player = (Player) sender;
        int id = Integer.parseInt(args[0]);
        if (id < 0) {
            sender.sendMessage(ChatColor.RED + "请输入 0 到 " + Integer.MAX_VALUE + " 的连续整数来表示一个岛屿代号！");
            return;
        } sender.sendMessage(ChatColor.GREEN + "使用快捷栏第一格的小铁斧来选取两个点！");
        player.getInventory().setItem(0, NMSSupport.setTag(new ItemStackBuilder(Material.IRON_AXE)
                .setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "《小铁斧》")
                .toItemStack(), "sbp-setup", "setup-build-area"));
        this.plotId = id;
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            void onSetupBuildArea(PlayerInteractEvent e) {
                if (e.isCancelled()) return;
                if (NMSSupport.getTag(e.getItem(), "sbp-setup") == null) return;
                if (NMSSupport.getTag(e.getItem(), "sbp-setup").equals("setup-build-area")) {
                    e.setCancelled(true);
                    if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                        sender.sendMessage(ChatColor.GREEN + "已选择第一个点！");
                        firstPoint = e.getClickedBlock().getLocation();
                    } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                        sender.sendMessage(ChatColor.GREEN + "已选择第二个点！");
                        secondPoint = e.getClickedBlock().getLocation();
                    } if (firstPoint != null && secondPoint != null) {
                        player.getInventory().setItem(0, new ItemStack(Material.AIR));
                        sender.sendMessage(ChatColor.GREEN + "设置成功！将在插件重新加载时生效！");
                        Main.getPlugin().getConfig().set("Plot." + plotId + ".BuildArea", firstPoint.getX() + "," + firstPoint.getY() + "," + firstPoint.getZ() + "," + secondPoint.getX() + "," + secondPoint.getY() + "," + secondPoint.getZ());
                        Main.getPlugin().saveConfig();
                        firstPoint = null;
                        secondPoint = null;
                        Main.addPlot(id);
                        HandlerList.unregisterAll(this);
                    }
                }
            }
            @EventHandler
            void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
                HandlerList.unregisterAll(this);
            }

            @EventHandler
            void onPlayerQuit(PlayerQuitEvent e) {
                HandlerList.unregisterAll(this);
            }
        }, Main.getPlugin());
    }
}
