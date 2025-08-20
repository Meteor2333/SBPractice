package cc.meteormc.sbpractice.command.maincmds;

import cc.meteormc.sbpractice.SBPractice;
import cc.meteormc.sbpractice.api.command.MainCommand;
import cc.meteormc.sbpractice.command.subcmds.HelpCommand;
import cc.meteormc.sbpractice.command.subcmds.multiplayer.*;
import cc.meteormc.sbpractice.config.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MultiplayerCommand extends MainCommand {
    public MultiplayerCommand() {
        super("multiplayer", "SBPractice plugin multiplayer command.", "", "mp");
        super.getSubCommands().add(new AcceptCommand());
        super.getSubCommands().add(new DenyCommand());
        super.getSubCommands().add(new InviteCommand());
        super.getSubCommands().add(new JoinCommand());
        super.getSubCommands().add(new KickCommand());
        super.getSubCommands().add(new LeaveCommand());
        super.getSubCommands().add(new HelpCommand(this));
    }

    @Override
    public void onNoPermission(@NotNull CommandSender sender) {
        sender.sendMessage(Messages.NO_PERMISSION.getMessage());
    }

    @Override
    public void sendCommandHelp(@NotNull CommandSender sender) {
        sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD
                + "þ " + ChatColor.GOLD + SBPractice.getPlugin().getDescription().getName() + " "
                + ChatColor.GRAY + "v" + SBPractice.getPlugin().getDescription().getVersion() + " by "
                + ChatColor.RED + SBPractice.getPlugin().getDescription().getAuthors().get(0));

        sender.sendMessage("");
        Messages.MULTIPLAYER_COMMAND_HELP.getMessageList().forEach(sender::sendMessage);
    }
}
