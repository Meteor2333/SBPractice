package cc.meteormc.sbpractice.command.subcmds.sbpractice;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.command.SubCommand;
import cc.meteormc.sbpractice.api.storage.player.PlayerData;
import cc.meteormc.sbpractice.config.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectArenaCommand extends SubCommand {
    public SelectArenaCommand() {
        super("arena", "", 1);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            for (Arena arena : Main.getArenas()) {
                if (arena.getName().equals(args[0])) {
                    PlayerData.getData(player).ifPresent(data -> {
                        Island island = data.getIsland();
                        if (island.getOwner().equals(player)) island.remove();
                        else {
                            island.removeGuest(player);
                            island.getOwner().sendMessage(Messages.PREFIX.getMessage() + Messages.LEAVE_PASSIVE.getMessage().replace("%player%", player.getName()));
                        }
                        arena.createIsland(player);
                    });
                    break;
                }
            }
        }
    }

    @Override
    public void onNoPermission(@NotNull CommandSender sender) {
        sender.sendMessage(Messages.NO_PERMISSION.getMessage());
    }

    @Override
    public @Nullable String getCommandUsage(@NotNull CommandSender sender) {
        return Messages.COMMAND_USAGE.getMessage().replace("%usage%", "/sbp arena <arenaName>");
    }
}
