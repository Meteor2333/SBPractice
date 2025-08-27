package cc.meteormc.sbpractice.api.event;

import cc.meteormc.sbpractice.api.Island;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class PlayerPerfectRestoreEvent extends Event {
    private final Island island;

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
