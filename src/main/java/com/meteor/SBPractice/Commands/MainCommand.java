package com.meteor.SBPractice.Commands;

import com.meteor.SBPractice.Commands.SubCommands.*;
import com.meteor.SBPractice.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand extends BukkitCommand {
    private static List<SubCommand> subCommands = new ArrayList<>();

    public MainCommand(String name) {
        super(name);
        setAliases(Arrays.asList("sbpractice", "speedbuilderspractice"));
        new Admin(this, "admin");
        new BufferBuild(this, "bufferBuild");

        //Add #4
        new Clear(this, "clear");
        new HighJump(this, "highjump");

        //Add #2
        new Platform(this, "platform");
        new SetupSpawnPoint(this, "spawnPoint");
        new SetupBuildArea(this, "buildArea");
        new ShowBuild(this, "showBuild");

        //Add #3
        new Spectator(this, "spec");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (args.length == 0) {
            sender.sendMessage(ChatColor.DARK_GRAY + "> " + ChatColor.BLUE + ChatColor.BOLD +
                    Main.getPlugin().getDescription().getName() + ChatColor.GOLD +
                    " v" + Main.getPlugin().getDescription().getVersion());
            return true;
        } for (SubCommand subCommand : getSubCommands()) {
            if (subCommand.getName().equalsIgnoreCase(args[0])) {
                if (sender.isOp() || !subCommand.requiresAdmin()) {
                    subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
                } return true;
            }
        } return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;
        if (args.length == 1) {
            List<String> tab = new ArrayList<>();
            for (SubCommand sb : getSubCommands()) {
                if (sender.isOp()) tab.add(sb.getName());
                else if (!sb.requiresAdmin()) tab.add(sb.getName());
            } tab.removeIf(filter -> !filter.toLowerCase().startsWith(args[0].toLowerCase()));
            return tab;
        } return null;
    }

    public void addSubCommand(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    public List<SubCommand> getSubCommands() {
        return subCommands;
    }
}
