package cc.meteormc.sbpractice.api.storage.player;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.storage.preset.PresetData;
import lombok.Data;
import org.bukkit.entity.Player;

import java.util.*;

@Data
public class PlayerData {
    private Island island;
    private boolean hidden = false;
    private boolean enableHighjump = true;
    private long highjumpCooldown = System.currentTimeMillis();
    private Map<Arena, List<PresetData>> presets = new HashMap<>();

    private final UUID uuid;
    private final PlayerStats stats;

    private static final Map<UUID, PlayerData> PLAYERS = new HashMap<>();

    public void register() {
        PLAYERS.put(this.uuid, this);
    }

    public void unregister() {
        PLAYERS.remove(this.uuid);
    }

    public static Optional<PlayerData> getData(Player player) {
        return Optional.ofNullable(PLAYERS.getOrDefault(player.getUniqueId(), null));
    }
}
