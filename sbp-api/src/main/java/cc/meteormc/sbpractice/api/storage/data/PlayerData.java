package cc.meteormc.sbpractice.api.storage.data;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.Arena;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class PlayerData {
    private Island island;
    private boolean hidden = false;
    private boolean enableHighjump = true;
    private long highjumpCooldown = System.currentTimeMillis();
    private Map<Arena, List<PresetData>> presets = new HashMap<>();

    private final UUID uuid;
    private final PlayerStats stats;

    private static final Map<UUID, PlayerData> PLAYERS = new ConcurrentHashMap<>();

    public void register() {
        PLAYERS.put(this.uuid, this);
    }

    public void unregister() {
        PLAYERS.remove(this.uuid);
    }

    public static Optional<PlayerData> getData(Player player) {
        return Optional.ofNullable(PLAYERS.getOrDefault(player.getUniqueId(), null));
    }

    @Getter
    public static class PlayerStats {
        private final UUID uuid;
        @Setter
        private int restores;

        public PlayerStats(UUID uuid, int restores) {
            this.uuid = uuid;
            this.restores = restores;
        }
    }
}
