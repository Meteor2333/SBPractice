package cc.meteormc.sbpractice.config.adapter;

import cc.carm.lib.configuration.adapter.ValueAdapter;
import cc.carm.lib.configuration.adapter.ValueType;
import cc.carm.lib.configuration.source.section.ConfigureSection;
import cc.meteormc.sbpractice.api.helper.Area;

import java.util.LinkedHashMap;
import java.util.Map;

public class AreaAdapter extends ValueAdapter<Area> {
    public AreaAdapter() {
        super(
                ValueType.of(Area.class),
                (holder, type, value) -> value.serialize(),
                (holder, type, data) -> {
                    Map<String, Object> map = null;
                    if (data instanceof Map) {
                        //noinspection unchecked
                        map = (Map<String, Object>) data;
                    } else if (data instanceof ConfigureSection) {
                        map = unpackSection((ConfigureSection) data);
                    }

                    if (map != null) {
                        return Area.deserialize(map);
                    } else {
                        return null;
                    }
                }
        );
    }

    private static Map<String, Object> unpackSection(ConfigureSection section) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : section.values().entrySet()) {
            if (entry.getValue() instanceof ConfigureSection) {
                result.put(entry.getKey(), unpackSection((ConfigureSection) entry.getValue()));
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
