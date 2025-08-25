package cc.meteormc.sbpractice.command.subcmds.sbpractice;

import cc.meteormc.sbpractice.api.command.SubCommand;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.arena.operation.GroundOperation;
import cc.meteormc.sbpractice.config.Message;
import com.cryptomorin.xseries.XSound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GroundCommand extends SubCommand {
    public GroundCommand() {
        super("ground", "", 0);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            PlayerData.getData((Player) sender).ifPresent(data -> {
                data.getIsland().executeOperation(new GroundOperation());
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
        return Message.COMMAND.USAGE.parseLine(sender, "/sbp ground");
    }
}
