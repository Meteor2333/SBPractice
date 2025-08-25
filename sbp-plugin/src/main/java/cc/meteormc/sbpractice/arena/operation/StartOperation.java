package cc.meteormc.sbpractice.arena.operation;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.BuildMode;
import cc.meteormc.sbpractice.api.arena.operation.Operation;
import cc.meteormc.sbpractice.config.Message;
import com.cryptomorin.xseries.XSound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class StartOperation implements Operation {
    @Override
    public boolean execute(Island island) {
        switch (island.getMode()) {
            case CONTINUOUS:
                if (island.isActive()) {
                    island.setActive(false);
                    Message.OPERATION.CONTINUOUS.INACTIVE.sendToAll(island.getNearbyPlayers());
                } else {
                    island.setActive(true);
                    Message.OPERATION.CONTINUOUS.ACTIVE.sendToAll(island.getNearbyPlayers());
                } // Works as expected, I'm sure this is correct
            case ONCE:
                refreshCountdown(island);
                return true;
            default: refreshCountdown(island);
        }

        return false;
    }

    public static void refreshCountdown(Island island) {
        BukkitTask task = island.getModeTask();
        if (task != null) {
            if (!island.isActive() && island.getMode() == BuildMode.CONTINUOUS) {
                task.cancel();
            }
            island.setModeTask(null);
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
                            for (Player player : island.getNearbyPlayers()) {
                                Message.TITLE.START.send(player);
                                XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
                            }

                            island.executeOperation(new ClearOperation());
                            island.setCanStart(true);
                            island.startTimer();
                        } else {
                            for (Player player : island.getNearbyPlayers()) {
                                XSound.BLOCK_NOTE_BLOCK_PLING.play(player);
                                Message.TITLE.COUNTDOWN.send(player, this.times + 1);
                            }
                        }
                    }
                }.runTaskTimer(Main.getPlugin(), 0L, 15L)
        );
    }
}
