package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Listeners.PlayerListener;
import com.meteor.SBPractice.Messages;
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
            if (PlayerListener.HighjumpToggled.getOrDefault(player.getUniqueId(), true)) {
                player.sendMessage(Messages.getMessage("highjump-disabled"));
                PlayerListener.HighjumpToggled.put(player.getUniqueId(), false);
            } else {
                player.sendMessage(Messages.getMessage("highjump-enabled"));
                PlayerListener.HighjumpToggled.put(player.getUniqueId(), true);
            }
        } else if (args[0].equals("intensity")) {
            try {
                if (Double.parseDouble(args[1]) > 100D) return;
                PlayerListener.HighjumpIntensity.put(player.getUniqueId(), Double.parseDouble(args[1]));
            } catch (Exception ignored) {}
        }
    }
}
