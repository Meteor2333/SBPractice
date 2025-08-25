package cc.meteormc.sbpractice.arena.operation;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.operation.Operation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class RecordOperation implements Operation {
    @Override
    public boolean execute(Island island) {
        island.stopTimer();
        island.setCanStart(false);
        island.getRecordedBlocks().clear();
        World world = island.getArena().getWorld();
        for (Vector vector : island.getBuildArea().getVectors()) {
            Location loc = vector.toLocation(world);
            island.getRecordedBlocks().put(loc, Main.getNms().getBlockDataAt(loc));
        }
        return true;
    }
}
