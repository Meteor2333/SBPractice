package cc.meteormc.sbpractice.api.manager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Random;

@Getter
@RequiredArgsConstructor
public class WorldManager {
    @Nullable
    private World world;

    private final String name;
    private final World.Environment environment;
    private final WorldType type;

    public void load(boolean isVoid) {
        World oldWorld = Bukkit.getWorld(this.name);
        if (oldWorld != null) {
            Bukkit.unloadWorld(oldWorld, false);
        }

        File file = new File(Bukkit.getWorldContainer(), this.name);
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        this.world = new WorldCreator(this.name)
                .environment(this.environment)
                .type(this.type)
                .generateStructures(!isVoid)
                .generator(
                        isVoid ? new ChunkGenerator() {
                            @Override
                            public @NotNull ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int x, int z, @NotNull ChunkGenerator.BiomeGrid biome) {
                                return this.createChunkData(world);
                            }
                        } : null
                )
                .createWorld();
        this.world.save();
    }

    public void unload() {
        assert this.world != null;
        this.world.getPlayers().forEach(player -> player.kickPlayer("The world you are in has been unloaded!"));
        Bukkit.unloadWorld(this.world, false);
        try {
            FileUtils.deleteDirectory(this.world.getWorldFolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.world = null;
    }
}
