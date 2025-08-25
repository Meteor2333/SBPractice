package cc.meteormc.sbpractice.config;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.annotation.ConfigPath;
import cc.carm.lib.configuration.source.ConfigurationHolder;
import cc.carm.lib.configuration.value.standard.ConfiguredValue;
import cc.meteormc.sbpractice.config.adapter.LocationAdapter;
import cc.meteormc.sbpractice.config.adapter.VectorAdapter;
import cc.meteormc.sbpractice.config.adapter.XMaterialAdapter;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@Getter
@ConfigPath(root = true)
public class ArenaConfig implements Configuration {
    private final ConfigurationHolder<?> holder;

    public ArenaConfig(ConfigurationHolder<?> holder) {
        this.holder = holder;
        holder.adapters().register(new LocationAdapter());
        holder.adapters().register(new VectorAdapter());
        holder.adapters().register(new XMaterialAdapter());
        holder.initialize(this);
    }

    public final ConfiguredValue<String> NAME = ConfiguredValue.of(String.class);
    
    public final MAP MAP = new MAP();
    public static class MAP implements Configuration {
        public final ConfiguredValue<Integer> HEIGHT = ConfiguredValue.of(Integer.class);

        public final ConfiguredValue<Vector> AREA_POS1 = ConfiguredValue.of(Vector.class);

        public final ConfiguredValue<Vector> AREA_POS2 = ConfiguredValue.of(Vector.class);

        public final ConfiguredValue<Vector> BUILD_AREA_POS1 = ConfiguredValue.of(Vector.class);

        public final ConfiguredValue<Vector> BUILD_AREA_POS2 = ConfiguredValue.of(Vector.class);

        public final ConfiguredValue<Location> SPAWN = ConfiguredValue.of(Location.class);
    }

    public final SIGN SIGN = new SIGN();
    public static class SIGN implements Configuration {
        public final ConfiguredValue<Vector> GROUND = ConfiguredValue.of(Vector.class);

        public final ConfiguredValue<Vector> RECORD = ConfiguredValue.of(Vector.class);

        public final ConfiguredValue<Vector> CLEAR = ConfiguredValue.of(Vector.class);

        public final ConfiguredValue<Vector> SELECT_ARENA = ConfiguredValue.of(Vector.class);

        public final ConfiguredValue<Vector> MODE = ConfiguredValue.of(Vector.class);

        public final ConfiguredValue<Vector> PRESET = ConfiguredValue.of(Vector.class);

        public final ConfiguredValue<Vector> START = ConfiguredValue.of(Vector.class);

        public final ConfiguredValue<Vector> PREVIEW = ConfiguredValue.of(Vector.class);   
    }
}
