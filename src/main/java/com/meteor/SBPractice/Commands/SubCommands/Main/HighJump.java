package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HighJump extends SubCommand {
    public HighJump(MainCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        SBPPlayer player = SBPPlayer.getPlayer((Player) sender);
        if (player == null) return;
        if (args.length == 0) {
            if (player.isEnableHighjump()) {
                player.sendMessage(Messages.HIGHJUMP_DISABLED.getMessage());
                player.setEnableHighjump(false);
            } else {
                player.sendMessage(Messages.HIGHJUMP_ENABLED.getMessage());
                player.setEnableHighjump(true);
            }
        } else if (args[0].equals("intensity")) {
            try {
                if (Double.parseDouble(args[1]) > 100D) return;
                player.setHighjumpIntensity(Double.parseDouble(args[1]));
            } catch (Exception ignored) {}
        }
    }
}
