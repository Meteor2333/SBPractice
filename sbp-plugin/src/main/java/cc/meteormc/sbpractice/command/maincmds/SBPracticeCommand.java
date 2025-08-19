package cc.meteormc.sbpractice.command.maincmds;

import cc.meteormc.sbpractice.Main;
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
        super.getSubCommands().add(new AdaptGroundCommand());
        super.getSubCommands().add(new CachingBuildingCommand());
        super.getSubCommands().add(new ClearBuildingCommand());
        super.getSubCommands().add(new SelectArenaCommand());
        super.getSubCommands().add(new SelectPresetCommand());
        super.getSubCommands().add(new SetupArenaCommand());
        super.getSubCommands().add(new StartCountdownCommand());
        super.getSubCommands().add(new ToggleBuildModeCommand());
        super.getSubCommands().add(new ToggleHighjumpCommand());
        super.getSubCommands().add(new ViewBuildingCommand());
        super.getSubCommands().add(new HelpCommand(this));
    }

    @Override
    public void onNoPermission(@NotNull CommandSender sender) {
        sender.sendMessage(Messages.NO_PERMISSION.getMessage());
    }

    @Override
    public void sendCommandHelp(@NotNull CommandSender sender) {
        sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD
                + "þ " + ChatColor.GOLD + Main.getPlugin().getDescription().getName() + " "
                + ChatColor.GRAY + "v" + Main.getPlugin().getDescription().getVersion() + " by "
                + ChatColor.RED + Main.getPlugin().getDescription().getAuthors().get(0));

        sender.sendMessage("");
        Messages.MAIN_COMMAND_HELP.getMessageList().forEach(sender::sendMessage);
    }
}
