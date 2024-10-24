package com.meteor.SBPractice.Commands;

import com.meteor.SBPractice.Commands.SubCommands.Main.Help;
import com.meteor.SBPractice.Commands.SubCommands.Multiplayer.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.*;

public class MultiplayerCommand extends BukkitCommand implements ParentCommand {
    private static List<SubCommand> subCommands = new ArrayList<>();

    public MultiplayerCommand(String name) {
        super(name);
        setAliases(Arrays.asList("multiplayer"));

        new Help(this, "help");

        new Accept(this, "accept");
        new Deny(this, "help");
        new Invite(this, "invite");
        new Join(this, "join");
        new Kick(this, "kick");
        new Leave(this, "leave");
    }

    @Override
    public boolean execute(CommandSender sender, String s,  String[] args) {
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
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            for (SubCommand sb : getSubCommands()) {
                if (sender.isOp()) tab.add(sb.getName());
                else if (!sb.requiresAdmin()) tab.add(sb.getName());
            } tab.removeIf(filter -> !filter.toLowerCase().startsWith(args[0].toLowerCase()));
        } else if (args.length == 2) {
            Bukkit.getOnlinePlayers().forEach(p -> tab.add(p.getName()));
            tab.removeIf(filter -> !filter.toLowerCase().startsWith(args[1].toLowerCase()));
        } return tab;
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