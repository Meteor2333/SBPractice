package cc.meteormc.sbpractice.config;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.annotation.ConfigPath;
import cc.carm.lib.configuration.annotation.HeaderComments;
import cc.carm.lib.configuration.source.ConfigurationHolder;
import cc.carm.lib.configuration.value.standard.ConfiguredList;
import cc.carm.lib.configuration.value.standard.ConfiguredValue;
import cc.meteormc.sbpractice.api.helper.Area;
import cc.meteormc.sbpractice.config.adapter.AreaAdapter;
import cc.meteormc.sbpractice.config.adapter.LocationAdapter;
import cc.meteormc.sbpractice.config.adapter.VectorAdapter;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@ConfigPath(root = true)
@HeaderComments("DO NOT MODIFY ANYTHING HERE!!!")
public class ZoneConfig implements Configuration {
    @Getter
    private final ConfigurationHolder<?> holder;

    public ZoneConfig(ConfigurationHolder<?> holder) {
        this.holder = holder;
        holder.adapters().register(new AreaAdapter());
        holder.adapters().register(new LocationAdapter());
        holder.adapters().register(new VectorAdapter());
        holder.initialize(this);
    }

    public final MAP MAP = new MAP();

    public static class MAP implements Configuration {
        public final ConfiguredValue<Integer> HEIGHT = ConfiguredValue.of(Integer.class);

        public final ConfiguredValue<Area> AREA = ConfiguredValue.of(Area.class);

        public final ConfiguredValue<Area> BUILD_AREA = ConfiguredValue.of(Area.class);

        public final ConfiguredValue<Location> SPAWN = ConfiguredValue.of(Location.class);
    }

    public final SIGN SIGN = new SIGN();

    public static class SIGN implements Configuration {
        public final ConfiguredList<Vector> CLEAR = ConfiguredList.with(Vector.class).build();

        public final ConfiguredList<Vector> GROUND = ConfiguredList.with(Vector.class).build();

        public final ConfiguredList<Vector> MODE = ConfiguredList.with(Vector.class).build();

        public final ConfiguredList<Vector> PRESET = ConfiguredList.with(Vector.class).build();

        public final ConfiguredList<Vector> PREVIEW = ConfiguredList.with(Vector.class).build();

        public final ConfiguredList<Vector> RECORD = ConfiguredList.with(Vector.class).build();

        public final ConfiguredList<Vector> START = ConfiguredList.with(Vector.class).build();

        public final ConfiguredList<Vector> TOGGLE_ZONE = ConfiguredList.with(Vector.class).build();
    }
}
