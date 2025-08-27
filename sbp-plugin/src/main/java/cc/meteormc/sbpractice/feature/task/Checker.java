package cc.meteormc.sbpractice.feature.task;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.event.PlayerPerfectRestoreEvent;
import cc.meteormc.sbpractice.api.storage.data.BlockData;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.config.Message;
import cc.meteormc.sbpractice.operation.StartOperation;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Checker implements Runnable {
    private final Island island;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        // Starts a thread which indefinitely sleeps to force the JVM to enable high resolution timers on Windows
        Thread hack = new Thread(() -> {
            while (true) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException ignored) {
                }
            }
        }, "SBPractice-TimerHack");
        hack.setDaemon(true);
        hack.start();
    }

    public Checker(Island island) {
        this.island = island;
        this.scheduler.scheduleAtFixedRate(this, 3L, 10L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        try {
            this.check();
        } catch (Throwable ignored) {
        }
    }

    public void shutdown() {
        this.scheduler.shutdownNow();
    }

    private synchronized void check() {
        boolean isEmpty = true, isPerfect = true;
        for (Vector point : island.getBuildArea().getPoints()) {
            Location location = point.toLocation(island.getZone().getWorld());
            BlockData currentBlock = Main.get().getNms().getBlockDataAt(location);

            if (isEmpty && currentBlock.getType() != Material.AIR) {
                isEmpty = false;
            }

            if (isPerfect) {
                BlockData recordedBlock = island.getRecordedBlocks().get(location);
                if (recordedBlock == null || !Main.get().getNms().isSimilarBlock(currentBlock, recordedBlock)) {
                    isPerfect = false;
                }
            }
        }

        if (island.getMode() == Island.BuildMode.DEFAULT) {
            if (isEmpty) {
                island.setCanStart(true);
                island.stopTimer();
            } else if (!island.isStarted()) {
                island.startTimer();
            }
        }

        if (isPerfect && island.isStarted()) {
            this.onPerfectMatch();
            island.setCanStart(false);
            island.stopTimer();
        }
    }

    private void onPerfectMatch() {
        Bukkit.getScheduler().runTask(Main.get(), () -> {
            PlayerPerfectRestoreEvent call = new PlayerPerfectRestoreEvent(island);
            Bukkit.getPluginManager().callEvent(call);

            for (Player player : island.getAllPlayers()) {
                PlayerData.getData(player).ifPresent(data -> {
                    PlayerData.PlayerStats stats = data.getStats();
                    stats.setRestores(stats.getRestores() + 1);
                });
            }

            for (Player player : island.getNearbyPlayers()) {
                XSound.ENTITY_PLAYER_LEVELUP.play(player);
                XSound.BLOCK_NOTE_BLOCK_PLING.play(player);
                Message.TITLE.PERFECT_MATCH.send(player, island.getFormattedTime());
            }
        });

        if (island.getMode() == Island.BuildMode.CONTINUOUS && island.isActive()) {
            Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
                StartOperation.refreshCountdown(island);
            }, 20L);
        }
    }
}
