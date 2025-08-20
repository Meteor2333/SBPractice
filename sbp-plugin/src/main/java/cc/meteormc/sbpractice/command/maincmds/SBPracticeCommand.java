package cc.meteormc.sbpractice.command.maincmds;

import cc.meteormc.sbpractice.SBPractice;
import cc.meteormc.sbpractice.api.command.MainCommand;
import cc.meteormc.sbpractice.command.subcmds.HelpCommand;
import cc.meteormc.sbpractice.command.subcmds.sbpractice.*;
import cc.meteormc.sbpractice.config.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SBPracticeCommand extends MainCommand {
    public SBPracticeCommand() {
        super("sbpractice", "SBPractice plugin main command.", "", "sbp");
        super.getSubCommands().add(new GroundCommand());
        super.getSubCommands().add(new RecordCommand());
        super.getSubCommands().add(new ClearCommand());
        super.getSubCommands().add(new SelectArenaCommand());
        super.getSubCommands().add(new PresetCommand());
        super.getSubCommands().add(new SetupCommand());
        super.getSubCommands().add(new StartCommand());
        super.getSubCommands().add(new ModeCommand());
        super.getSubCommands().add(new HighjumpCommand());
        super.getSubCommands().add(new PreviewCommand());
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
        Messages.MAIN_COMMAND_HELP.getMessageList().forEach(sender::sendMessage);
    }
}
