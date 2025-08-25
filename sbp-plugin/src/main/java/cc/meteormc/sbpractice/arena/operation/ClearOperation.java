package cc.meteormc.sbpractice.arena.operation;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.operation.Operation;
import cc.meteormc.sbpractice.api.util.Region;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class ClearOperation implements Operation {
    @Override
    public boolean execute(Island island) {
        island.stopTimer();
        island.setCanStart(true);
        Region buildArea = island.getBuildArea();
        for (Entity entity : island.getSpawnPoint().getWorld().getEntities()) {
            if (!buildArea.clone().outset(1).isInside(entity.getLocation())) continue;
            if (entity.getType().equals(EntityType.PLAYER)) continue;
            entity.remove();
        }

        World world = island.getArena().getWorld();
        for (Vector vector : buildArea.getVectors()) {
            Block block = vector.toLocation(world).getBlock();
            if (block.getType() != Material.AIR) {
                block.setType(Material.AIR);
            }
        }
        return true;
    }
}
