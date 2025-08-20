package cc.meteormc.sbpractice.api.arena;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.storage.preset.PresetData;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

public interface Arena {
    String getName();

    File getSchematicFile();

    File getPresetsDir();

    World getWorld();

    Island createIsland(Player player) throws RuntimeException;

    List<PresetData> getPresets();

    void unregister();
}
