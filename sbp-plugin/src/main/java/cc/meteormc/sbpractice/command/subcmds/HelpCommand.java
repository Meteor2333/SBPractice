package cc.meteormc.sbpractice.command.subcmds;

import cc.meteormc.sbpractice.api.command.MainCommand;
import cc.meteormc.sbpractice.api.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HelpCommand extends SubCommand {
    private final MainCommand parent;

    public HelpCommand(MainCommand parent) {
        super("help", "", 0);
        this.parent = parent;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.parent.sendCommandHelp(sender);
    }

    @Override
    public void onNoPermission(@NotNull CommandSender sender) {

    }

    @Override
    public @Nullable String getCommandUsage(@NotNull CommandSender sender) {
        return null;
    }
}
