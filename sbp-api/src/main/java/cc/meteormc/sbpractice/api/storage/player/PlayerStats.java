package cc.meteormc.sbpractice.api.storage.player;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
public class PlayerStats {
    private final UUID uuid;
    @Setter
    private int restores;

    public PlayerStats(UUID uuid, int restores) {
        this.uuid = uuid;
        this.restores = restores;
    }
}
