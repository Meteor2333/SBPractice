package cc.meteormc.sbpractice.arena.operation;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.operation.Operation;

public class PreviewOperation implements Operation {
    @Override
    public boolean execute(Island island) {
        island.stopTimer();
        island.setCanStart(false);
        island.getRecordedBlocks().forEach(Main.getNms()::setBlock);
        return true;
    }
}
