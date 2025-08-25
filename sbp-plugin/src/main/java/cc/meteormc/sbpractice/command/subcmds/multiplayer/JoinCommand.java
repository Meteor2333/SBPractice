package cc.meteormc.sbpractice.command.subcmds.multiplayer;

import cc.meteormc.sbpractice.api.command.SubCommand;
import cc.meteormc.sbpractice.arena.session.MultiplayerSession;
import cc.meteormc.sbpractice.config.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JoinCommand extends SubCommand {
    public JoinCommand() {
        super("join", "", 1);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                Message.BASIC.PLAYER_NOT_FOUND.sendTo(sender);
                return;
            }

            if (target.getName().equalsIgnoreCase(player.getName())) {
                Message.BASIC.CANNOT_DO_THAT.sendTo(sender);
                return;
            }

            MultiplayerSession.getSession(player).joinPlayer(target);
        }
    }

    @Override
    public void onNoPermission(@NotNull CommandSender sender) {
        Message.COMMAND.NO_PERMISSION.sendTo(sender);
    }

    @Override
    public @Nullable String getCommandUsage(@NotNull CommandSender sender) {
        return Message.COMMAND.USAGE.parseLine(sender, "/mp join <player>");
    }
}
