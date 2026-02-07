package cc.meteormc.sbpractice.api.storage.data;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.Zone;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class PlayerData {
    @Nullable
    private Island island;
    private boolean hidden = false;
    private boolean adminMode = false;
    private int highjumpHeight = 7;
    private long highjumpCooldown = System.currentTimeMillis();
    private Map<Zone, List<PresetData>> presets = new HashMap<>();

    private final UUID uuid;
    private final PlayerStats stats;

    private static final Map<UUID, PlayerData> PLAYERS = new ConcurrentHashMap<>();

    public void register() {
        PLAYERS.put(this.uuid, this);
    }

    public void unregister() {
        PLAYERS.remove(this.uuid);
    }

    public static Optional<PlayerData> getData(OfflinePlayer player) {
        return Optional.ofNullable(PLAYERS.get(player.getUniqueId()));
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
