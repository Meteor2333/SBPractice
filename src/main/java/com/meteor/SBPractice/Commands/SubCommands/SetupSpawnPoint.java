package com.meteor.SBPractice.Commands.SubCommands;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetupSpawnPoint extends SubCommand {
    public SetupSpawnPoint(MainCommand parent, String name) {
        super(parent, name, true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "请输入 0 到 " + Integer.MAX_VALUE + " 的连续整数来表示一个岛屿代号！");
            return;
        }
        Location loc = ((Player) sender).getLocation();
        int id = Integer.parseInt(args[0]);
        if (id < 0) {
            sender.sendMessage(ChatColor.RED + "请输入 0 到 " + Integer.MAX_VALUE + " 的整数来表示一个岛屿代号！");
            return;
        } sender.sendMessage(ChatColor.GREEN + "设置成功！将在插件重新加载时生效！");
        Main.getPlugin().getConfig().set("Plot." + id + ".SpawnPoint", loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch());
        Main.getPlugin().saveConfig();
        Main.addPlot(id);
    }
}
