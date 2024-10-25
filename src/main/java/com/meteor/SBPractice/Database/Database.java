package com.meteor.SBPractice.Database;

import com.meteor.SBPractice.Api.SBPPlayer;

import java.util.UUID;

public interface Database {
    void initialize();

    SBPPlayer.PlayerStats getPlayerStats(UUID uuid);

    void setPlayerStats(SBPPlayer.PlayerStats playerStats);
}
