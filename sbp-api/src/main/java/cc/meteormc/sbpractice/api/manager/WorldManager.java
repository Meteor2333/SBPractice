package cc.meteormc.sbpractice.api.manager;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
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
            try {
                Bukkit.unloadWorld(oldWorld, false);
                FileUtils.deleteDirectory(oldWorld.getWorldFolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        WorldCreator wc = new WorldCreator(this.name);
        wc.environment(this.environment).type(this.type).generateStructures(!isVoid);
        if (isVoid) {
            wc.generator(new ChunkGenerator() {
                @Override
                public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid b) {
                    return super.createChunkData(world);
                }
            });
        }

        this.world = wc.createWorld();
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
