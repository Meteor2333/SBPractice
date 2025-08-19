package cc.meteormc.sbpractice.command.subcmds.sbpractice;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.command.SubCommand;
import cc.meteormc.sbpractice.api.storage.player.PlayerData;
import cc.meteormc.sbpractice.config.Messages;
import com.cryptomorin.xseries.XSound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleBuildModeCommand extends SubCommand {
    public ToggleBuildModeCommand() {
        super("mode", "", 0);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            PlayerData.getData((Player) sender).ifPresent(data -> {
                Island island = data.getIsland();
                if (island.getOwner().equals(sender)) {
                    switch (island.toggleBuildMode()) {
                        case DEFAULT:
                            sender.sendMessage(Messages.PREFIX.getMessage() + Messages.TOGGLE_BUILD_MODE_DEFAULT.getMessage());
                            break;
                        case COUNTDOWN_ONCE:
                            sender.sendMessage(Messages.PREFIX.getMessage() + Messages.TOGGLE_BUILD_MODE_COUNTDOWN_ONCE.getMessage());
                            break;
                        case COUNTDOWN_CONTINUOUS:
                            sender.sendMessage(Messages.PREFIX.getMessage() + Messages.TOGGLE_BUILD_MODE_COUNTDOWN_CONTINUOUS.getMessage());
                            break;
                    }
                    XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play((Entity) sender);
                } else {
                    sender.sendMessage(Messages.PREFIX.getMessage() + Messages.CANNOT_DO_THAT.getMessage());
                    XSound.ENTITY_VILLAGER_NO.play((Entity) sender);
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
        return Messages.COMMAND_USAGE.getMessage().replace("%usage%", "/sbp mode");
    }
}
