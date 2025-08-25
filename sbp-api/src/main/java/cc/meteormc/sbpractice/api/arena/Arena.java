package cc.meteormc.sbpractice.api.arena;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.storage.data.PresetData;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

public interface Arena {
    String getName();

    File getPresetsDir();

    File getSchematicFile();

    World getWorld();

    Island createIsland(Player player);

    List<PresetData> getPresets();

    void unregister();
}
