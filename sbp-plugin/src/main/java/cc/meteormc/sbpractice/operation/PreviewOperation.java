package cc.meteormc.sbpractice.operation;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.helper.Operation;

public class PreviewOperation implements Operation {
    @Override
    public boolean execute(Island island) {
        island.stopTimer();
        island.setCanStart(false);
        island.getRecordedBlocks().forEach(Main.get().getNms()::setBlock);
        return true;
    }
}
