package cc.meteormc.sbpractice.api.storage.data;

import lombok.Data;
import org.bukkit.Location;

@Data
public class SignData {
    private final Location ground;
    private final Location record;
    private final Location clear;
    private final Location selectArena;
    private final Location mode;
    private final Location preset;
    private final Location start;
    private final Location preview;
}
