package cc.meteormc.sbpractice.config.adapter;

import cc.carm.lib.configuration.adapter.ValueAdapter;
import cc.carm.lib.configuration.adapter.ValueType;
import cc.carm.lib.configuration.source.section.ConfigureSection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;

import java.util.Map;

public class LocationAdapter extends ValueAdapter<Location> {
    public LocationAdapter() {
        super(
                ValueType.of(Location.class),
                (holder, type, value) -> value.serialize(),
                (holder, type, data) -> {
                    Map<String, Object> map = null;
                    if (data instanceof Map) {
                        //noinspection unchecked
                        map = (Map<String, Object>) data;
                    } else if (data instanceof ConfigureSection) {
                        map = ((ConfigureSection) data).values();
                    }

                    if (map != null) {
                        World world = Bukkit.getWorld((String) map.getOrDefault("world", ""));
                        return new Location(
                                world,
                                NumberConversions.toDouble(map.getOrDefault("x", 0)),
                                NumberConversions.toDouble(map.getOrDefault("y", 0)),
                                NumberConversions.toDouble(map.getOrDefault("z", 0)),
                                NumberConversions.toFloat(map.getOrDefault("yaw", 0)),
                                NumberConversions.toFloat(map.getOrDefault("pitch", 0))
                        );
                    } else {
                        return null;
                    }
                }
        );
    }
}
