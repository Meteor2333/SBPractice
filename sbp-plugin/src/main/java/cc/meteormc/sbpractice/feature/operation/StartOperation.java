package cc.meteormc.sbpractice.feature.operation;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.helper.Operation;
import cc.meteormc.sbpractice.config.Message;

public class StartOperation implements Operation {
    @Override
    public boolean execute(Island island) {
        boolean result = true;
        switch (island.getMode()) {
            case CONTINUOUS:
                if (island.isActive()) {
                    island.setActive(false);
                    Message.OPERATION.CONTINUOUS.INACTIVE.sendToAll(island.getNearbyPlayers());
                } else {
                    island.setActive(true);
                    Message.OPERATION.CONTINUOUS.ACTIVE.sendToAll(island.getNearbyPlayers());
                }
                break;
            case ONCE:
                break;
            default:
                result = false;
                break;
        }

        island.refreshCountdown();
        return result;
    }
}
