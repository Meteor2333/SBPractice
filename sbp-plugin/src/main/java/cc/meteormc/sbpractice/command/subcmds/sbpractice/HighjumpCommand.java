package cc.meteormc.sbpractice.command.subcmds.sbpractice;

import cc.meteormc.sbpractice.api.command.SubCommand;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.config.Message;
import com.cryptomorin.xseries.XSound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HighjumpCommand extends SubCommand {
    public HighjumpCommand() {
        super("highjump", "", 0);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            PlayerData.getData((Player) sender).ifPresent(data -> {
                if (data.isEnableHighjump()) {
                    data.setEnableHighjump(false);
                    Message.OPERATION.HIGHJUMP.DISABLE.sendTo(sender);
                } else {
                    data.setEnableHighjump(true);
                    Message.OPERATION.HIGHJUMP.ENABLE.sendTo(sender);
                }
                XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play((Entity) sender);
            });
        }
    }

    @Override
    public void onNoPermission(@NotNull CommandSender sender) {
        Message.COMMAND.NO_PERMISSION.sendTo(sender);
    }

    @Override
    public @Nullable String getCommandUsage(@NotNull CommandSender sender) {
        return Message.COMMAND.USAGE.parseLine(sender, "/sbp highjump");
    }
}
