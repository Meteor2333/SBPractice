package cc.meteormc.sbpractice.arena;

import cc.meteormc.sbpractice.DefaultIsland;
import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.arena.exception.ArenaCreationException;
import cc.meteormc.sbpractice.api.config.ConfigManager;
import cc.meteormc.sbpractice.api.config.paths.ArenaConfigPath;
import cc.meteormc.sbpractice.api.manager.WorldManager;
import cc.meteormc.sbpractice.api.storage.player.PlayerData;
import cc.meteormc.sbpractice.api.storage.preset.PresetData;
import cc.meteormc.sbpractice.api.storage.schematic.Schematic;
import cc.meteormc.sbpractice.api.storage.sign.SignGroup;
import cc.meteormc.sbpractice.api.util.ItemBuilder;
import cc.meteormc.sbpractice.api.util.Region;
import cc.meteormc.sbpractice.api.util.Utils;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.config.Messages;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public class DefaultArena extends ConfigManager implements Arena {
    private Schematic schematic;
    private final String name;
    private final File schematicFile;
    private final File presetsDir;
    private final WorldManager world;
    private final List<Island> islands = new ArrayList<>();
    private final List<PresetData> presets = new ArrayList<>();

    public DefaultArena(String name) {
        super(Main.getPlugin(), Main.getPlugin().getDataFolder() + "/Arenas/" + name, "config");
        this.name = name;
        this.schematicFile = new File(super.getFile().getParentFile(), "schematic.dat");
        this.presetsDir = new File(super.getFile().getParentFile(), "Persets");
        this.world = new WorldManager(name, World.Environment.NORMAL, WorldType.FLAT);
    }

    public DefaultArena load() throws ArenaCreationException {
        try {
            this.world.load(true);
            World world = this.world.getWorld();
            if (world != null) {
                world.setAutoSave(false);
                world.setGameRuleValue("announceAdvancements", "false");
                world.setGameRuleValue("doDaylightCycle", "false");
                world.setGameRuleValue("doFireTick", "false");
                world.setGameRuleValue("doTileDrops", "false");
                world.setGameRuleValue("doWeatherCycle", "false");
                world.setGameRuleValue("randomTickSpeed", "0");
            }

            this.schematic = Schematic.load(this.schematicFile);

            File[] presetFiles = presetsDir.listFiles((dir, name) -> name.endsWith(".perset"));
            if (presetFiles != null) {
                this.presets.addAll(
                        Arrays.stream(presetFiles)
                                .map(PresetData::load)
                                .collect(Collectors.toList())
                );
            }

            return this;
        } catch (IOException e) {
            throw new ArenaCreationException("Failed to create arena " + this.name + " because of the schematic file!", e);
        }
    }

    @Override
    public Island createIsland(Player player) throws RuntimeException {
        int index = this.islands.indexOf(null);
        if (index == -1) {
            this.islands.add(null);
            index = this.islands.size() - 1;
        }

        Location reference = new Location(this.world.getWorld(), index * MainConfig.ISLAND_DISTANCE_INTERVAL.getInt(), super.getInt(ArenaConfigPath.HEIGHT), 0D);
        this.schematic.pasteSchematic(reference);

        Island island = new DefaultIsland(player, this,

                new Region(this.world.getWorld(),
                        super.getLocation(ArenaConfigPath.MAP_AREA_POS1).toVector().add(reference.toVector()),
                        super.getLocation(ArenaConfigPath.MAP_AREA_POS2).toVector().add(reference.toVector())
                ),
                new Region(this.world.getWorld(),
                        super.getLocation(ArenaConfigPath.MAP_BUILD_AREA_POS1).toVector().add(reference.toVector()),
                        super.getLocation(ArenaConfigPath.MAP_BUILD_AREA_POS2).toVector().add(reference.toVector())
                ),

                super.getLocation(ArenaConfigPath.MAP_SPAWN, this.world.getWorld()).add(reference.toVector()),
                new SignGroup(
                        super.getLocation(ArenaConfigPath.GROUND_SIGN, this.world.getWorld()).add(reference.toVector()),
                        super.getLocation(ArenaConfigPath.RECORD_SIGN, this.world.getWorld()).add(reference.toVector()),
                        super.getLocation(ArenaConfigPath.CLEAR_SIGN, this.world.getWorld()).add(reference.toVector()),
                        super.getLocation(ArenaConfigPath.SELECT_ARENA_SIGN, this.world.getWorld()).add(reference.toVector()),
                        super.getLocation(ArenaConfigPath.MODE_SIGN, this.world.getWorld()).add(reference.toVector()),
                        super.getLocation(ArenaConfigPath.PRESET_SIGN, this.world.getWorld()).add(reference.toVector()),
                        super.getLocation(ArenaConfigPath.START_SIGN, this.world.getWorld()).add(reference.toVector()),
                        super.getLocation(ArenaConfigPath.PREVIEW_SIGN, this.world.getWorld()).add(reference.toVector())
                )
        );
        this.islands.set(index, island);
        PlayerData.getData(player).ifPresent(data -> data.setIsland(island));
        island.refreshSigns();
        Utils.resetPlayer(player);
        player.getInventory().setItem(7, new ItemBuilder(MainConfig.START_ITEM.getMaterial()
                .orElse(XMaterial.AIR))
                .setDisplayName(Messages.START_ITEM_NAME.getMessage())
                .build());
        player.getInventory().setItem(8, new ItemBuilder(MainConfig.CLEAR_ITEM.getMaterial()
                .orElse(XMaterial.AIR))
                .setDisplayName(Messages.CLEAR_ITEM_NAME.getMessage())
                .build());
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10, 0, false, false));
        player.teleport(island.getSpawnPoint());
        return island;
    }

    public void removeIsland(Island island) {
        if (this.islands.contains(island)) {
            island.getArea().fill(XMaterial.AIR);
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
}
