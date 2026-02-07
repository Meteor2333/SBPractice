package cc.meteormc.sbpractice.api;

import cc.meteormc.sbpractice.api.storage.data.PresetData;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;

public interface Zone {
    String getName();

    File getPresetFolder();

    File getSchematicFile();

    World getWorld();

    boolean isFull();

    Island createIsland(Player player);

    void removeIsland(Island island);

    List<PresetData> getPresets();

    void unregister();
}
