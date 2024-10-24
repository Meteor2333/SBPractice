package com.meteor.SBPractice.Commands;

import org.bukkit.command.CommandSender;

public abstract class SubCommand {

    private String name;
    private boolean perm;

    public SubCommand(ParentCommand parent, String name, boolean perm) {
        this.name = name;
        this.perm = perm;
        parent.addSubCommand(this);
    }

    public abstract void execute(CommandSender sender, String[] args);

    public boolean requiresAdmin() {
        return perm;
    }

    public String getName() {
        return name;
    }
}
