package cc.meteormc.sbpractice.feature.operation;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.helper.Area;
import cc.meteormc.sbpractice.api.helper.Operation;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class ClearOperation implements Operation {
    @Override
    public boolean execute(Island island) {
        Area buildArea = island.getBuildArea();
        for (Entity entity : island.getSpawnPoint().getWorld().getEntities()) {
            if (!buildArea.outset(1).isInside(entity.getLocation())) continue;
            if (entity.getType().equals(EntityType.PLAYER)) continue;
            entity.remove();
        }

        World world = island.getZone().getWorld();
        for (Vector point : buildArea.getPoints()) {
            Block block = point.toLocation(world).getBlock();
            if (block.getType() != Material.AIR) {
                block.setType(Material.AIR);
            }
        }

        island.stopTimer();
        island.setCanStart(true);
        return true;
    }
}
