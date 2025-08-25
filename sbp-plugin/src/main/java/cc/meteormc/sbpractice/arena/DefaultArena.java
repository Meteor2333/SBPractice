package cc.meteormc.sbpractice.arena;

import cc.carm.lib.mineconfiguration.bukkit.MineConfiguration;
import cc.meteormc.sbpractice.DefaultIsland;
import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.manager.WorldManager;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.api.storage.data.PresetData;
import cc.meteormc.sbpractice.api.storage.data.SchematicData;
import cc.meteormc.sbpractice.api.storage.data.SignData;
import cc.meteormc.sbpractice.api.util.Region;
import cc.meteormc.sbpractice.config.ArenaConfig;
import cc.meteormc.sbpractice.config.MainConfig;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class DefaultArena implements Arena {
    private SchematicData schematic;
    private final String name;
    private final File presetsDir;
    private final File schematicFile;
    private final WorldManager world;
    private final ArenaConfig config;
    private final List<Island> islands = new ArrayList<>();
    private final List<PresetData> presets = new ArrayList<>();

    public DefaultArena(String name) {
        File dir = new File(Main.getPlugin().getDataFolder() + "/Arenas/" + name);
        this.name = name;
        this.presetsDir = new File(dir, "Presets");
        this.schematicFile = new File(dir, "schematic.dat");
        this.config = new ArenaConfig(MineConfiguration.from(new File(dir, "config.yml"), null));
        this.world = new WorldManager(name, World.Environment.NORMAL, WorldType.FLAT);
    }

    public DefaultArena load() {
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

            File[] presetFiles = presetsDir.listFiles((dir, fileName) -> fileName.endsWith(".preset"));
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
            throw new UncheckedIOException("Failed to create arena " + this.name + " because of the schematic file!", e);
        }
    }

    public World getWorld() {
        return world.getWorld();
    }

    @Override
    public Island createIsland(Player player) {
        int index = this.islands.indexOf(null);
        if (index == -1) {
            this.islands.add(null);
            index = this.islands.size() - 1;
        }

        Vector reference = new Vector(
                index * MainConfig.ISLAND_DISTANCE.resolve(),
                config.MAP.HEIGHT.resolve(),
                0D
        );

        this.schematic.pasteSchematic(reference.toLocation(world.getWorld()));

        Location spawn = config.MAP.SPAWN.resolve();
        spawn.setWorld(this.getWorld());

        Island island = new DefaultIsland(
                player,
                this,
                new Region(
                        config.MAP.AREA_POS1.resolve().add(reference),
                        config.MAP.AREA_POS2.resolve().add(reference)
                ),
                new Region(
                        config.MAP.BUILD_AREA_POS1.resolve().add(reference),
                        config.MAP.BUILD_AREA_POS2.resolve().add(reference)
                ),
                spawn.add(reference),
                new SignData(
                        config.SIGN.GROUND.resolve().add(reference).toLocation(world.getWorld()),
                        config.SIGN.RECORD.resolve().add(reference).toLocation(world.getWorld()),
                        config.SIGN.CLEAR.resolve().add(reference).toLocation(world.getWorld()),
                        config.SIGN.SELECT_ARENA.resolve().add(reference).toLocation(world.getWorld()),
                        config.SIGN.MODE.resolve().add(reference).toLocation(world.getWorld()),
                        config.SIGN.PRESET.resolve().add(reference).toLocation(world.getWorld()),
                        config.SIGN.START.resolve().add(reference).toLocation(world.getWorld()),
                        config.SIGN.PREVIEW.resolve().add(reference).toLocation(world.getWorld())
                )
        );
        this.islands.set(index, island);
        PlayerData.getData(player).ifPresent(data -> data.setIsland(island));
        island.refreshSigns();
        island.resetPlayer(player);
        return island;
    }

    public void removeIsland(Island island) {
        if (this.islands.contains(island)) {
            this.islands.set(this.islands.indexOf(island), null);
            this.islands.remove(island);
        }
    }

    public void unregister() {
        Objects.requireNonNull(this.world.getWorld()).getPlayers().forEach(player -> player.kickPlayer("The current arena is unloaded!"));
        for (Island island : this.islands) {
            if (island != null) island.remove();
        }
        this.world.unload();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DefaultArena && this.name.equals(((DefaultArena) obj).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
