package cc.meteormc.sbpractice.arena.operation;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.arena.operation.Operation;
import cc.meteormc.sbpractice.api.storage.data.BlockData;
import cc.meteormc.sbpractice.api.util.Region;
import cc.meteormc.sbpractice.api.version.NMS;
import cc.meteormc.sbpractice.config.MainConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public class GroundOperation implements Operation {
    @Override
    public boolean execute(Island island) {
        NMS nms = Main.getNms();
        Arena arena = island.getArena();
        World world = arena.getWorld();
        Region area = island.getBuildArea();
        for (int x = area.getMinimumPos().getBlockX(); x <= area.getMaximumPos().getBlockX(); x++) {
            for (int z = area.getMinimumPos().getBlockZ(); z <= area.getMaximumPos().getBlockZ(); z++) {
                Location location = new Location(world, x, area.getYMin(), z);
                BlockData block = nms.getBlockDataAt(location);
                Location down = location.clone().subtract(0, 1, 0);
                if (block.getType() != Material.AIR) {
                    nms.setBlock(down, block);
                    if (MainConfig.AUTO_TO_FULL_BLOCK.resolve() && nms.toFullBlock(down.getBlock())) {
                        continue;
                    }
                }

                nms.setBlock(down, BlockData.of(MainConfig.MATERIAL.GROUND_BLOCK.resolve()));
            }
        }
        return true;
    }
}
