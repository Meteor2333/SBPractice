package cc.meteormc.sbpractice.config.adapter;

import cc.carm.lib.configuration.adapter.ValueAdapter;
import cc.carm.lib.configuration.adapter.ValueType;
import cc.carm.lib.configuration.source.section.ConfigureSection;
import org.bukkit.Location;
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
                        try {
                            return Location.deserialize(map);
                        } catch (IllegalArgumentException ignored) {
                            assert false;
                            return new Location(
                                    null,
                                    NumberConversions.toDouble(map.get("x")),
                                    NumberConversions.toDouble(map.get("y")),
                                    NumberConversions.toDouble(map.get("z")),
                                    NumberConversions.toFloat(map.get("yaw")),
                                    NumberConversions.toFloat(map.get("pitch"))
                            );
                        }
                    } else {
                        return null;
                    }
                }
        );
    }
}
