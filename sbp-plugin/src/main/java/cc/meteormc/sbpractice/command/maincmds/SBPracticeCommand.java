package cc.meteormc.sbpractice.command.maincmds;

import cc.meteormc.sbpractice.api.command.MainCommand;
import cc.meteormc.sbpractice.command.subcmds.HelpCommand;
import cc.meteormc.sbpractice.command.subcmds.sbpractice.*;
import cc.meteormc.sbpractice.config.Message;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SBPracticeCommand extends MainCommand {
    public SBPracticeCommand() {
        super("sbpractice", "SBPractice plugin main command.", "", "sbp");
        this.getSubCommands().add(new GroundCommand());
        this.getSubCommands().add(new RecordCommand());
        this.getSubCommands().add(new ClearCommand());
        this.getSubCommands().add(new SelectArenaCommand());
        this.getSubCommands().add(new PresetCommand());
        this.getSubCommands().add(new SetupCommand());
        this.getSubCommands().add(new StartCommand());
        this.getSubCommands().add(new ModeCommand());
        this.getSubCommands().add(new HighjumpCommand());
        this.getSubCommands().add(new PreviewCommand());
        this.getSubCommands().add(new HelpCommand(this));
    }

    @Override
    public void onNoPermission(@NotNull CommandSender sender) {
        Message.COMMAND.NO_PERMISSION.sendTo(sender);
    }

    @Override
    public List<String> getCommandHelp(@NotNull CommandSender sender) {
        return Message.COMMAND.HELP.MAIN.parse(sender);
    }
}
