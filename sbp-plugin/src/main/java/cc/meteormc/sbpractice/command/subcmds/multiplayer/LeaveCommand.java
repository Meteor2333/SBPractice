package cc.meteormc.sbpractice.command.subcmds.multiplayer;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.command.SubCommand;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.config.Message;
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
                    Message.BASIC.CANNOT_DO_THAT.sendTo(player);
                } else {
                    island.removeGuest(player);
                    island.getArena().createIsland(player);
                    Message.MULTIPLAYER.LEAVE.ACTIVE.sendTo(player, island.getOwner().getName());
                    Message.MULTIPLAYER.LEAVE.PASSIVE.sendTo(island.getOwner(), player.getName());
                }
            });
        }
    }

    @Override
    public void onNoPermission(@NotNull CommandSender sender) {
        Message.COMMAND.NO_PERMISSION.sendTo(sender);
    }

    @Override
    public @Nullable String getCommandUsage(@NotNull CommandSender sender) {
        return Message.COMMAND.USAGE.parseLine(sender, "/mp leave");
    }
}
