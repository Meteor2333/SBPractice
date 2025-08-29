package cc.meteormc.sbpractice.api.storage;

import cc.meteormc.sbpractice.api.storage.data.PlayerData;

import java.util.UUID;

public interface Database {
    void connect();

    PlayerData.PlayerStats getPlayerStats(UUID uuid);

    void setPlayerStats(PlayerData.PlayerStats playerStats);
}
