package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.ParentCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Help extends SubCommand {
    public Help(ParentCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        SBPPlayer player = SBPPlayer.getPlayer((Player) sender);
        if (player == null) return;
        player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD +
                "þ " + ChatColor.GOLD + Main.getPlugin().getDescription().getName() + " " +
                ChatColor.GRAY + "v" + Main.getPlugin().getDescription().getVersion() + " by " +
                ChatColor.RED + Main.getPlugin().getDescription().getAuthors().get(0));

        player.sendMessage("");
        Messages.COMMAND_HELP.getMessageList().forEach(player::sendMessage);
    }
}
