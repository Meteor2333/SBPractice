package cc.meteormc.sbpractice.api.storage;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.Zone;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
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
    private long highjumpCooldown = System.currentTimeMillis();
    private Map<Zone, List<PresetData>> presets = new HashMap<>();

    private final UUID uuid;
    private final PlayerSettings settings;
    private final PlayerStats stats;

    private static final Map<UUID, PlayerData> PLAYERS = new ConcurrentHashMap<>();

    public static Optional<PlayerData> getData(OfflinePlayer player) {
        return Optional.ofNullable(PLAYERS.get(player.getUniqueId()));
    }

    public void register() {
        PLAYERS.put(this.uuid, this);
    }

    public void unregister() {
        PLAYERS.remove(this.uuid);
    }

    @Data
    @DatabaseTable(tableName = "sbp_settings")
    public static class PlayerSettings {
        @DatabaseField(id = true)
        private UUID uuid;
        @DatabaseField(columnName = "highjump_height")
        private int highjumpHeight = 7;
    }

    @Data
    @DatabaseTable(tableName = "sbp_stats")
    public static class PlayerStats {
        @DatabaseField(id = true)
        private UUID uuid;
        @DatabaseField(columnName = "restores")
        private int restores;
    }
}
