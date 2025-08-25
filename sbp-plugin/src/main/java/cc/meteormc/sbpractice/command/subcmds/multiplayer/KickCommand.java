package cc.meteormc.sbpractice.command.subcmds.multiplayer;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.command.SubCommand;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.config.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KickCommand extends SubCommand {
    public KickCommand() {
        super("kick", "", 1);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                Message.BASIC.PLAYER_NOT_FOUND.sendTo(player);
                return;
            }

            if (target.getName().equalsIgnoreCase(player.getName())) {
                Message.BASIC.CANNOT_DO_THAT.sendTo(player);
                return;
            }

            PlayerData.getData(player).ifPresent(data -> {
                Island island = data.getIsland();
                if (island.getOwner().equals(player)) {
                    if (island.getGuests().contains(target)) {
                        island.removeGuest(target);
                        island.getArena().createIsland(target);
                        Message.MULTIPLAYER.KICK.ACTIVE.sendTo(player, target.getName());
                        Message.MULTIPLAYER.KICK.PASSIVE.sendTo(target, player.getName());
                    } else Message.BASIC.PLAYER_NOT_FOUND.sendTo(player);
                } else Message.BASIC.CANNOT_DO_THAT.sendTo(player);
            });
        }
    }

    @Override
    public void onNoPermission(@NotNull CommandSender sender) {
        Message.COMMAND.NO_PERMISSION.sendTo(sender);
    }

    @Override
    public @Nullable String getCommandUsage(@NotNull CommandSender sender) {
        return Message.COMMAND.USAGE.parseLine(sender, "/mp kick <player>");
    }
}
