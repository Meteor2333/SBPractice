package cc.meteormc.sbpractice.command.subcmds.sbpractice;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.command.SubCommand;
import cc.meteormc.sbpractice.api.storage.player.PlayerData;
import cc.meteormc.sbpractice.config.Messages;
import cc.meteormc.sbpractice.gui.PresetGui;
import com.cryptomorin.xseries.XSound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SelectPresetCommand extends SubCommand {
    public SelectPresetCommand() {
        super("preset", "", 0);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerData.getData(player).ifPresent(data -> {
                Island island = data.getIsland();
                if (island.getOwner().equals(player)) {
                    new PresetGui(island).open(player);
                } else {
                    player.sendMessage(Messages.PREFIX.getMessage() + Messages.CANNOT_DO_THAT.getMessage());
                    XSound.ENTITY_VILLAGER_NO.play(player);
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
        return Messages.COMMAND_USAGE.getMessage().replace("%usage%", "/sbp preset [presetName]");
    }
}
