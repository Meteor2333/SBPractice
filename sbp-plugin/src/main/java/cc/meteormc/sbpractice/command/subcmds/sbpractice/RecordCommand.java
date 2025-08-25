package cc.meteormc.sbpractice.command.subcmds.sbpractice;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.command.SubCommand;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.arena.operation.RecordOperation;
import cc.meteormc.sbpractice.config.Message;
import com.cryptomorin.xseries.XSound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecordCommand extends SubCommand {
    public RecordCommand() {
        super("record", "", 0);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            PlayerData.getData((Player) sender).ifPresent(data -> {
                Island island = data.getIsland();
                if (island.getOwner().equals(sender)) {
                    data.getIsland().executeOperation(new RecordOperation());
                    XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play((Entity) sender);
                } else {
                    Message.BASIC.CANNOT_DO_THAT.sendTo(sender);
                    XSound.ENTITY_VILLAGER_NO.play((Entity) sender);
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
        return Message.COMMAND.USAGE.parseLine(sender, "/sbp record");
    }
}
