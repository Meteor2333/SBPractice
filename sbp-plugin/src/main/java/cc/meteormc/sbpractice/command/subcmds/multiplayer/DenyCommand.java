package cc.meteormc.sbpractice.command.subcmds.multiplayer;

import cc.meteormc.sbpractice.api.command.SubCommand;
import cc.meteormc.sbpractice.arena.session.MultiplayerSession;
import cc.meteormc.sbpractice.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DenyCommand extends SubCommand {
    public DenyCommand() {
        super("deny", "", 1);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender, target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(Messages.PREFIX.getMessage() + Messages.PLAYER_NOT_FOUND.getMessage());
                return;
            }

            if (target.getName().equalsIgnoreCase(player.getName())) {
                player.sendMessage(Messages.PREFIX.getMessage() + Messages.CANNOT_DO_THAT.getMessage());
                return;
            }

            MultiplayerSession.getSession(player).denyPlayer(target);
        }
    }

    @Override
    public void onNoPermission(@NotNull CommandSender sender) {
        sender.sendMessage(Messages.NO_PERMISSION.getMessage());
    }

    @Override
    public @Nullable String getCommandUsage(@NotNull CommandSender sender) {
        return Messages.COMMAND_USAGE.getMessage().replace("%usage%", "/sbp deny <player>");
    }
}
