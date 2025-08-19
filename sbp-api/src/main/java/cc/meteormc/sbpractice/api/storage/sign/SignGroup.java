package cc.meteormc.sbpractice.api.storage.sign;

import lombok.Data;
import org.bukkit.Location;

@Data
public class SignGroup {
    private final Location ground;
    private final Location record;
    private final Location clear;
    private final Location arena;
    private final Location mode;
    private final Location preset;
    private final Location start;
    private final Location preview;
}
