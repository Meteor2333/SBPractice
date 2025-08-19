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

public class StartCountdownCommand extends SubCommand {
    public StartCountdownCommand() {
        super("start", "", 0);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            PlayerData.getData((Player) sender).ifPresent(data -> {
                Island island = data.getIsland();
                switch (island.getBuildMode()) {
                    case COUNTDOWN_ONCE:
                        island.activateCountdown();
                        break;
                    case COUNTDOWN_CONTINUOUS:
                        if (island.isStartCountdown()) {
                            island.setStartCountdown(false);
                            sender.sendMessage(Messages.PREFIX.getMessage() + Messages.COUNTDOWN_CONTINUOUS_DISABLE.getMessage());
                        } else {
                            island.setStartCountdown(true);
                            sender.sendMessage(Messages.PREFIX.getMessage() + Messages.COUNTDOWN_CONTINUOUS_ENABLE.getMessage());
                        }
                        island.activateCountdown();
                        XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play((Entity) sender);
                        break;
                    default:
                        sender.sendMessage(Messages.PREFIX.getMessage() + Messages.CANNOT_DO_THAT.getMessage());
                        XSound.ENTITY_VILLAGER_NO.play((Entity) sender);
                        break;
                }
            });

            //PlayerData.getData((Player) sender).ifPresent(data -> data.getIsland().startTimer());
        }
    }

    @Override
    public void onNoPermission(@NotNull CommandSender sender) {
        sender.sendMessage(Messages.NO_PERMISSION.getMessage());
    }

    @Override
    public @Nullable String getCommandUsage(@NotNull CommandSender sender) {
        return Messages.COMMAND_USAGE.getMessage().replace("%usage%", "/sbp start");
    }
}
