package cc.meteormc.sbpractice.arena.operation;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.BuildMode;
import cc.meteormc.sbpractice.api.arena.operation.Operation;

public class ModeOperation implements Operation {
    @Override
    public boolean execute(Island island) {
        BuildMode mode = island.getMode();
        int index = mode.ordinal() + 1;
        if (index >= BuildMode.values().length) index = 0;
        island.setMode(BuildMode.values()[index]);
        island.setActive(false);
        return true;
    }
}
