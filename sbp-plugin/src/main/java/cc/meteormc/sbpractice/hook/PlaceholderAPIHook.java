package cc.meteormc.sbpractice.hook;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.storage.player.PlayerData;
import cc.meteormc.sbpractice.config.Messages;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "sbp";
    }

    @Override
    public @NotNull String getAuthor() {
        return Main.getPlugin().getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return Main.getPlugin().getDescription().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        Optional<PlayerData> data = PlayerData.getData(player.getPlayer());
        if (data.isPresent()) {
            switch (params) {
                case "total-restores":
                    return String.valueOf(data.get().getStats().getRestores());
                case "island-owner":
                    return data.get().getIsland().getOwner().getName();
                case "island-total-player":
                    return String.valueOf(data.get().getIsland().getGuests().size() + 1);
                case "current-time":
                    return Messages.CURRENT_TIME.getMessage().replace("%time%", data.get().getIsland().getFormattedTime());
                case "current-building-blocks":
                    int num = 0;
                    for (BlockState state : data.get().getIsland().getRecordedBlocks().values()) {
                        if (state != null && state.getType() != Material.AIR) num++;
                    }
                    return String.valueOf(num);
                default: return null;
            }
        } else return "None";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        return onRequest(player, params);
    }
}
