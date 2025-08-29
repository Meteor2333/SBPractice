package cc.meteormc.sbpractice.feature.operation;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.helper.Operation;
import cc.meteormc.sbpractice.config.Message;
import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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

        refreshCountdown(island);
        return result;
    }

    public static void refreshCountdown(Island island) {
        BukkitTask task = island.getModeTask();
        if (task != null) task.cancel();
        island.setModeTask(null);

        if (island.getMode() == Island.BuildMode.CONTINUOUS && !island.isActive()) {
            return;
        }

        if (island.getMode() == Island.BuildMode.DEFAULT) {
            return;
        }

        island.setCanStart(false);
        island.executeOperation(new PreviewOperation());
        island.setModeTask(
                new BukkitRunnable() {
                    private int times = 3;

                    @Override
                    public void run() {
                        if (this.times-- <= 0) {
                            this.cancel();
                            island.setModeTask(null);
                            for (Player player : island.getNearbyPlayers()) {
                                Message.TITLE.START.send(player);
                                XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
                            }

                            island.executeOperation(new ClearOperation());
                            island.setCanStart(true);
                            island.startTimer();
                        } else {
                            for (Player player : island.getNearbyPlayers()) {
                                Message.TITLE.COUNTDOWN.send(player, this.times + 1);
                                XSound.BLOCK_NOTE_BLOCK_PLING.play(player, 0.5F, 1.0F - 0.2F * times);
                            }
                        }
                    }
                }.runTaskTimer(Main.get(), 0L, 10L)
        );
    }
}
