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
import cc.meteormc.sbpractice.feature.operation.ClearOperation;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
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
            if (MainConfig.ISLAND_GENERATE.PRE_GENERATE.resolve()) {
                int amount = MainConfig.ISLAND_GENERATE.AMOUNT.resolve();
                if (amount <= 0) {
                    if (!MainConfig.ISLAND_GENERATE.WIDTH.isDefault()) {
                        // 如果没有岛屿数量限制 就使用宽度*宽度的值
                        int width = MainConfig.ISLAND_GENERATE.WIDTH.resolve();
                        amount = width * width;
                    } else {
                        // 如果生成宽度也没有设置 就默认12
                        amount = 12;
                    }
                }

                for (int i = 0; i < amount; i++) {
                    this.schematic.paste(this.getReference(i).toLocation(world.getWorld()));
                }
            }
            return this;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load zone " + this.name + "!", e);
        }
    }

    @Override
    public boolean isFull() {
        int maxCount = MainConfig.ISLAND_GENERATE.AMOUNT.resolve();
        return maxCount > 0 && this.islands.stream().filter(Objects::nonNull).count() >= maxCount;
    }

    @Override
    public Island createIsland(Player player) {
        if (this.isFull()) throw new RuntimeException("The zone is full!");

        int index = this.islands.indexOf(null);
        if (index == -1) {
            this.islands.add(null);
            index = this.islands.size() - 1;
        }

        Vector reference = this.getReference(index);

        // Not performant, but prevents players with no island
        this.schematic.paste(reference.toLocation(world.getWorld()));

        Location spawn = config.MAP.SPAWN.resolve().clone();
        spawn.setWorld(this.getWorld());
        Island island = new SimpleIsland(
                player,
                this,
                config.MAP.AREA.resolve().add(reference),
                config.MAP.BUILD_AREA.resolve().add(reference),
                spawn.add(reference),
                new SignData(
                        this.getWorld(),
                        addAll(config.SIGN.CLEAR.resolve(), reference),
                        addAll(config.SIGN.GROUND.resolve(), reference),
                        addAll(config.SIGN.MODE.resolve(), reference),
                        addAll(config.SIGN.PRESET.resolve(), reference),
                        addAll(config.SIGN.PREVIEW.resolve(), reference),
                        addAll(config.SIGN.RECORD.resolve(), reference),
                        addAll(config.SIGN.START.resolve(), reference),
                        addAll(config.SIGN.TOGGLE_ZONE.resolve(), reference)
                )
        );
        this.islands.set(index, island);
        PlayerData.getData(player).ifPresent(data -> data.setIsland(island));
        island.refreshSigns();
        island.resetPlayer(player);
        return island;
    }

    private Vector getReference(int index) {
        Area area = config.MAP.AREA.resolve();
        int width = Math.max(1, MainConfig.ISLAND_GENERATE.WIDTH.resolve());
        int distance = Math.max(0, MainConfig.ISLAND_GENERATE.DISTANCE.resolve());
        int x = index % width, z = index / width;
        return new Vector(
                x * (distance + area.getWidth()),
                config.MAP.HEIGHT.resolve(),
                z * (distance + area.getLength())
        );
    }

    private static List<Vector> addAll(List<Vector> vectors, Vector add) {
        return vectors.stream()
                .map(Vector::clone)
                .map(vec -> vec.add(add))
                .collect(Collectors.toList());
    }

    @Override
    public void removeIsland(Island island) {
        if (this.islands.contains(island)) {
            this.islands.set(this.islands.indexOf(island), null);
            this.islands.remove(island);
        }

        island.executeOperation(new ClearOperation());
        if (!MainConfig.ISLAND_GENERATE.PRE_GENERATE.resolve()) {
            for (Vector point : island.getArea().getPoints()) {
                Block block = point.toLocation(world.getWorld()).getBlock();
                if (block.getType() == Material.AIR) continue;
                block.setType(Material.AIR);
            }
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
