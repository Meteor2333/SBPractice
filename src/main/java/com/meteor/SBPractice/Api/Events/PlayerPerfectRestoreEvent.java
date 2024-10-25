package com.meteor.SBPractice.Api.Events;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Plot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
@RequiredArgsConstructor
public class PlayerPerfectRestoreEvent extends Event {
    private final Plot plot;
    private final SBPPlayer player;
    private final double time;
    private final boolean countdownMode;

    private boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
