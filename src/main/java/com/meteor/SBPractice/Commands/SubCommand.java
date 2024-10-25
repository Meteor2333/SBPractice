package com.meteor.SBPractice.Commands;

import lombok.Getter;
import org.bukkit.command.CommandSender;

@Getter
public abstract class SubCommand {

    private final String name;
    private final boolean perm;

    public SubCommand(ParentCommand parent, String name, boolean perm) {
        this.name = name;
        this.perm = perm;
        parent.addSubCommand(this);
    }

    public abstract void execute(CommandSender sender, String[] args);

}
