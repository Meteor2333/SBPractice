package cc.meteormc.sbpractice.api.storage.player;

import cc.meteormc.sbpractice.api.Island;
import lombok.Data;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Data
public class PlayerData {
    private Island island;
    private boolean hidden = false;
    private boolean enableHighjump = true;
    private long highjumpCooldown = System.currentTimeMillis();

    private final Player player;
    private final PlayerStats stats;

    private static final Map<UUID, PlayerData> PLAYERS = new HashMap<>();

    public void register() {
        PLAYERS.put(player.getUniqueId(), this);
    }

    public void unregister() {
        PLAYERS.remove(this.player.getUniqueId());
    }

    public static Optional<PlayerData> getData(Player player) {
        return Optional.ofNullable(PLAYERS.getOrDefault(player.getUniqueId(), null));
    }
}
