package com.meteor.SBPractice.Commands.SubCommands;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Listener.HighJumpListener;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HighJump extends SubCommand {
    public HighJump(MainCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            HighJumpListener.toggled(player);
        } else if (args[0].equals("intensity")) {
            if (Double.parseDouble(args[1]) > 100D) return;
            HighJumpListener.setIntensity(player, Double.parseDouble(args[1]));
        }
    }
}
