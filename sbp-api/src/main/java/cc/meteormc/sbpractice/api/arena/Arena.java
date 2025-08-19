package cc.meteormc.sbpractice.api.arena;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.manager.WorldManager;
import cc.meteormc.sbpractice.api.storage.preset.PresetData;
import org.bukkit.entity.Player;

import java.util.List;

public interface Arena {
    String getName();

    WorldManager getWorld();

    Island createIsland(Player player) throws RuntimeException;

    List<PresetData> getPresets();

    void unregister();
}
