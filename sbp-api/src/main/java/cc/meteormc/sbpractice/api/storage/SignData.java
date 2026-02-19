package cc.meteormc.sbpractice.api.storage;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class SignData {
    private final EnumMap<Type, List<Location>> signs = new EnumMap<>(Type.class);

    public SignData(
            World world,
            List<Vector> clear,
            List<Vector> ground,
            List<Vector> mode,
            List<Vector> preset,
            List<Vector> preview,
            List<Vector> record,
            List<Vector> start,
            List<Vector> toggleZone
    ) {
        this.signs.put(Type.CLEAR, toLocation(world, clear));
        this.signs.put(Type.GROUND, toLocation(world, ground));
        this.signs.put(Type.MODE, toLocation(world, mode));
        this.signs.put(Type.PRESET, toLocation(world, preset));
        this.signs.put(Type.PREVIEW, toLocation(world, preview));
        this.signs.put(Type.RECORD, toLocation(world, record));
        this.signs.put(Type.START, toLocation(world, start));
        this.signs.put(Type.TOGGLE_ZONE, toLocation(world, toggleZone));
    }

    private static List<Location> toLocation(World world, List<Vector> vectors) {
        return vectors.stream()
                .map(v -> v.toLocation(world))
                .map(Location::getBlock)
                .map(Block::getLocation)
                .collect(Collectors.toList());
    }

    public @Nullable Type matchType(Block block) {
        Location location = block.getLocation();
        for (Map.Entry<Type, List<Location>> entry : this.signs.entrySet()) {
            if (entry.getValue().contains(location)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public enum Type {
        CLEAR,
        GROUND,
        MODE,
        PRESET,
        PREVIEW,
        RECORD,
        START,
        TOGGLE_ZONE
    }
}
