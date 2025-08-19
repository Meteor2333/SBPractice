package cc.meteormc.sbpractice.command.subcmds.sbpractice;

import cc.meteormc.sbpractice.api.command.SubCommand;
import cc.meteormc.sbpractice.api.storage.player.PlayerData;
import cc.meteormc.sbpractice.config.Messages;
import com.cryptomorin.xseries.XSound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleHighjumpCommand extends SubCommand {
    public ToggleHighjumpCommand() {
        super("highjump", "", 0);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            PlayerData.getData((Player) sender).ifPresent(data -> {
                if (data.isEnableHighjump()) {
                    data.setEnableHighjump(false);
                    sender.sendMessage(Messages.PREFIX.getMessage() + Messages.TOGGLE_HIGHJUMP_DISABLE.getMessage());
                } else {
                    data.setEnableHighjump(true);
                    sender.sendMessage(Messages.PREFIX.getMessage() + Messages.TOGGLE_HIGHJUMP_ENABLE.getMessage());
                }
                XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play((Entity) sender);
            });
        }
    }

    @Override
    public void onNoPermission(@NotNull CommandSender sender) {
        sender.sendMessage(Messages.NO_PERMISSION.getMessage());
    }

    @Override
    public @Nullable String getCommandUsage(@NotNull CommandSender sender) {
        return Messages.COMMAND_USAGE.getMessage().replace("%usage%", "/sbp highjump");
    }
}
