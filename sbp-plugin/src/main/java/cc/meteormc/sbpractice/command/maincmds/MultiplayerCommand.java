package cc.meteormc.sbpractice.command.maincmds;

import cc.meteormc.sbpractice.api.command.MainCommand;
import cc.meteormc.sbpractice.command.subcmds.HelpCommand;
import cc.meteormc.sbpractice.command.subcmds.multiplayer.*;
import cc.meteormc.sbpractice.config.Message;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MultiplayerCommand extends MainCommand {
    public MultiplayerCommand() {
        super("multiplayer", "SBPractice plugin multiplayer command.", "", "mp");
        this.getSubCommands().add(new AcceptCommand());
        this.getSubCommands().add(new DenyCommand());
        this.getSubCommands().add(new InviteCommand());
        this.getSubCommands().add(new JoinCommand());
        this.getSubCommands().add(new KickCommand());
        this.getSubCommands().add(new LeaveCommand());
        this.getSubCommands().add(new HelpCommand(this));
    }

    @Override
    public void onNoPermission(@NotNull CommandSender sender) {
        Message.COMMAND.NO_PERMISSION.sendTo(sender);
    }

    @Override
    public List<String> getCommandHelp(@NotNull CommandSender sender) {
        return Message.COMMAND.HELP.MULTIPLAYER.parse(sender);
    }
}
