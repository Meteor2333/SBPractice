package com.meteor.SBPractice.Commands;

import com.meteor.SBPractice.Commands.SubCommands.Main.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainCommand extends BukkitCommand implements ParentCommand {
    private static List<SubCommand> subCommands = new ArrayList<>();

    public MainCommand(String name) {
        super(name);
        setAliases(Arrays.asList("sbpractice", "speedbuilderspractice"));

        new Admin(this, "admin");
        new BufferBuilding(this, "bufferBuild");
        new Clear(this, "clear");
        new HighJump(this, "highjump");
        new AddPlot(this, "addPlot");
        new Help(this, "help");
        new Countdown(this, "countdown");
        new PreStart(this, "prestart");
        new Platform(this, "platform");
        new AddPlot(this, "setup");
        new ShowBuilding(this, "showBuild");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (args.length == 0) {
            Bukkit.dispatchCommand(sender, "sbp help");
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

    @Override
    public void addSubCommand(SubCommand subCommand) {
        subCommands.add(subCommand);
    }

    @Override
    public List<SubCommand> getSubCommands() {
        return subCommands;
    }
}
