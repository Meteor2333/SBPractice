package cc.meteormc.sbpractice.api.storage;

import cc.meteormc.sbpractice.api.storage.player.PlayerStats;

import java.util.UUID;

public interface Database {
    void initialize();

    PlayerStats getPlayerStats(UUID uuid);

    void setPlayerStats(PlayerStats playerStats);
}
