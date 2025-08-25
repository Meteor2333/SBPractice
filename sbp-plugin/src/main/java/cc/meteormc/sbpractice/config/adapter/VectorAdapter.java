package cc.meteormc.sbpractice.config.adapter;

import cc.carm.lib.configuration.adapter.ValueAdapter;
import cc.carm.lib.configuration.adapter.ValueType;
import cc.carm.lib.configuration.source.section.ConfigureSection;
import org.bukkit.util.Vector;

import java.util.Map;

public class VectorAdapter extends ValueAdapter<Vector> {
    public VectorAdapter() {
        super(
                ValueType.of(Vector.class),
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
                        return Vector.deserialize(map);
                    } else {
                        return null;
                    }
                }
        );
    }
}
