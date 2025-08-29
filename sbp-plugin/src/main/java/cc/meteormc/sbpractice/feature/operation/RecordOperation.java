package cc.meteormc.sbpractice.feature.operation;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.helper.Operation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class RecordOperation implements Operation {
    @Override
    public boolean execute(Island island) {
        island.stopTimer();
        island.setCanStart(false);
        island.getRecordedBlocks().clear();
        World world = island.getZone().getWorld();
        for (Vector point : island.getBuildArea().getPoints()) {
            Location loc = point.toLocation(world);
            island.getRecordedBlocks().put(loc, Main.get().getNms().getBlockDataAt(loc));
        }
        return true;
    }
}
