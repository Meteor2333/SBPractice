package cc.meteormc.sbpractice.operation;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.helper.Operation;

public class ModeOperation implements Operation {
    @Override
    public boolean execute(Island island) {
        Island.BuildMode mode = island.getMode();
        int index = mode.ordinal() + 1;
        if (index >= Island.BuildMode.values().length) index = 0;
        island.setMode(Island.BuildMode.values()[index]);
        island.setActive(false);
        return true;
    }
}
