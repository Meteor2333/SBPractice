package cc.meteormc.sbpractice.feature.operation;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.Zone;
import cc.meteormc.sbpractice.api.helper.Area;
import cc.meteormc.sbpractice.api.helper.Operation;
import cc.meteormc.sbpractice.api.storage.data.BlockData;
import cc.meteormc.sbpractice.api.version.NMS;
import cc.meteormc.sbpractice.config.MainConfig;
import org.bukkit.Location;
import org.bukkit.World;

public class GroundOperation implements Operation {
    @Override
    public boolean execute(Island island) {
        NMS nms = Main.get().getNms();
        Zone zone = island.getZone();
        World world = zone.getWorld();
        Area area = island.getBuildArea();
        for (int x = area.getMinimumPos().getBlockX(); x <= area.getMaximumPos().getBlockX(); x++) {
            for (int z = area.getMinimumPos().getBlockZ(); z <= area.getMaximumPos().getBlockZ(); z++) {
                Location location = new Location(world, x, area.getYMin(), z);
                Location down = location.clone().subtract(0, 1, 0);
                if (MainConfig.AUTO_TO_FULL_BLOCK.resolve()) {
                    BlockData fullBlock = nms.toFullBlock(location.getBlock());
                    if (fullBlock != null) {
                        nms.setBlock(down, fullBlock);
                        continue;
                    }
                }

                nms.setBlock(down, BlockData.of(MainConfig.MATERIAL.GROUND_BLOCK.resolve()));
            }
        }
        return true;
    }
}
