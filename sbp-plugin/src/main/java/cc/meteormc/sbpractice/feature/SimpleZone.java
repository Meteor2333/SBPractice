package cc.meteormc.sbpractice.feature;

import cc.carm.lib.mineconfiguration.bukkit.MineConfiguration;
import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.Zone;
import cc.meteormc.sbpractice.api.helper.Area;
import cc.meteormc.sbpractice.api.manager.WorldManager;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.api.storage.data.PresetData;
import cc.meteormc.sbpractice.api.storage.data.SchematicData;
import cc.meteormc.sbpractice.api.storage.data.SignData;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.config.ZoneConfig;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class SimpleZone implements Zone {
    private SchematicData schematic;
    private final String name;
    private final File presetFolder;
    private final File schematicFile;
    private final WorldManager world;
    private final ZoneConfig config;
    private final List<Island> islands = new ArrayList<>();
    private final List<PresetData> presets = new ArrayList<>();

    public static final File ZONES_DIR = new File(Main.get().getDataFolder(), "Zones");

    public SimpleZone(String name) {
        File dir = new File(ZONES_DIR, name);
        this.name = name;
        this.presetFolder = new File(dir, "Presets");
        this.schematicFile = new File(dir, "schematic.dat");
        this.config = new ZoneConfig(MineConfiguration.from(new File(dir, "config.yml"), null));
        this.world = new WorldManager(name, World.Environment.NORMAL, WorldType.FLAT);
    }

    public World getWorld() {
        return world.getWorld();
    }

    public SimpleZone load() {
        try {
            this.world.load(true);
            World w = this.world.getWorld();
            if (w != null) {
                w.setAutoSave(false);
                w.setGameRuleValue("announceAdvancements", "false");
                w.setGameRuleValue("doDaylightCycle", "false");
                w.setGameRuleValue("doFireTick", "false");
                w.setGameRuleValue("doTileDrops", "false");
                w.setGameRuleValue("doWeatherCycle", "false");
                w.setGameRuleValue("randomTickSpeed", "0");
            }

            File[] presetFiles = presetFolder.listFiles((dir, fileName) -> fileName.endsWith(".preset"));
            if (presetFiles != null) {
                this.presets.addAll(
                        Arrays.stream(presetFiles)
                                .map(PresetData::load)
                                .collect(Collectors.toList())
                );
            }

            this.schematic = SchematicData.load(this.schematicFile);
            return this;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load zone " + this.name + " because of the schematic file!", e);
        }
    }

    @Override
    public Island createIsland(Player player) {
        int index = this.islands.indexOf(null);
        if (index == -1) {
            this.islands.add(null);
            index = this.islands.size() - 1;
        }

        Area area = config.MAP.AREA.resolve().clone();
        Area buildArea = config.MAP.BUILD_AREA.resolve().clone();
        Location spawn = config.MAP.SPAWN.resolve().clone();
        spawn.setWorld(this.getWorld());
        Vector reference = new Vector(
                (double) index * (MainConfig.ISLAND_GENERATE.DISTANCE.resolve() + area.getWidth()),
                config.MAP.HEIGHT.resolve(),
                0D
        );

        this.schematic.paste(reference.toLocation(world.getWorld()));

        Island island = new SimpleIsland(
                player,
                this,
                area.add(reference),
                buildArea.add(reference),
                spawn.add(reference),
                new SignData(
                        config.SIGN.GROUND.resolve().clone().add(reference).toLocation(world.getWorld()),
                        config.SIGN.RECORD.resolve().clone().add(reference).toLocation(world.getWorld()),
                        config.SIGN.CLEAR.resolve().clone().add(reference).toLocation(world.getWorld()),
                        config.SIGN.TOGGLE_ZONE.resolve().clone().add(reference).toLocation(world.getWorld()),
                        config.SIGN.MODE.resolve().clone().add(reference).toLocation(world.getWorld()),
                        config.SIGN.PRESET.resolve().clone().add(reference).toLocation(world.getWorld()),
                        config.SIGN.START.resolve().clone().add(reference).toLocation(world.getWorld()),
                        config.SIGN.PREVIEW.resolve().clone().add(reference).toLocation(world.getWorld())
                )
        );
        this.islands.set(index, island);
        PlayerData.getData(player).ifPresent(data -> data.setIsland(island));
        island.refreshSigns();
        island.resetPlayer(player);
        return island;
    }

    @Override
    public void removeIsland(Island island) {
        if (this.islands.contains(island)) {
            this.islands.set(this.islands.indexOf(island), null);
            this.islands.remove(island);
        }
    }

    @Override
    public void unregister() {
        Optional.ofNullable(this.world.getWorld())
                .map(World::getPlayers)
                .ifPresent(players -> {
                    players.forEach(player -> player.kickPlayer("The current zone is unloaded!"));
                });
        this.islands.stream()
                .filter(Objects::nonNull)
                .forEach(Island::remove);
        this.world.unload();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SimpleZone && this.name.equals(((SimpleZone) obj).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
