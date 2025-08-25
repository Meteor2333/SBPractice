package cc.meteormc.sbpractice.api.event;

import cc.meteormc.sbpractice.api.Island;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class PlayerPerfectRestoreEvent extends Event {
    private final Island island;
    @Setter
    private boolean cancelled = false;

    private static final HandlerList HANDLERS = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
