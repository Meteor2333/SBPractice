package cc.meteormc.sbpractice.hook;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.storage.data.BlockData;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
        Optional<PlayerData> optionalData = PlayerData.getData(player.getPlayer());
        if (optionalData.isPresent()) {
            PlayerData data = optionalData.get();
            Island island = data.getIsland();
            switch (params) {
                case "restores":
                    return String.valueOf(data.getStats().getRestores());
                case "owner":
                    return island.getOwner().getName();
                case "total-player":
                    return String.valueOf(island.getGuests().size() + 1);
                case "time":
                    return island.getFormattedTime();
                case "blocks":
                    int num = 0;
                    for (BlockData block : island.getRecordedBlocks().values()) {
                        if (block != null && block.getType() != Material.AIR) num++;
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
