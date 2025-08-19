package cc.meteormc.sbpractice.command.subcmds.multiplayer;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.command.SubCommand;
import cc.meteormc.sbpractice.api.storage.player.PlayerData;
import cc.meteormc.sbpractice.config.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LeaveCommand extends SubCommand {
    public LeaveCommand() {
        super("leave", "", 0);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerData.getData(player).ifPresent(data -> {
                Island island = data.getIsland();
                if (island.getOwner().equals(player)) {
                    player.sendMessage(Messages.PREFIX.getMessage() + Messages.CANNOT_DO_THAT.getMessage());
                } else {
                    island.removeGuest(player);
                    island.getArena().createIsland(player);
                    player.sendMessage(Messages.PREFIX.getMessage() + Messages.LEAVE_ACTIVE.getMessage().replace("%player%", island.getOwner().getName()));
                    island.getOwner().sendMessage(Messages.PREFIX.getMessage() + Messages.LEAVE_PASSIVE.getMessage().replace("%player%", player.getName()));
                }
            });
        }
    }

    @Override
    public void onNoPermission(@NotNull CommandSender sender) {
        sender.sendMessage(Messages.NO_PERMISSION.getMessage());
    }

    @Override
    public @Nullable String getCommandUsage(@NotNull CommandSender sender) {
        return null;
    }
}
