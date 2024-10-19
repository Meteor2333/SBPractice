package com.meteor.SBPractice;

import com.meteor.SBPractice.Commands.SubCommands.Admin;
import com.meteor.SBPractice.Commands.SubCommands.Spectator;
import com.meteor.SBPractice.Utils.Message;
import com.meteor.SBPractice.Utils.NMSSupport;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Plot {
    private double time = 0F;
    private long currentTime = 0L;
    private boolean canStart = true;
    private Player player = null;
    private List<Player> guests = new ArrayList<>();
    private Timer displayTask = null;
    private Location spawnPoint, firstPoint, secondPoint;
    private PlotStatus plotStatus = PlotStatus.NOT_OCCUPIED;
    private List<BlockState> bufferBuildBlock = new ArrayList<>();

    private static List<Plot> plots = new ArrayList<>();

    public Plot(Location spawnPoints, Location firstPoint, Location secondPoint) {
        this.spawnPoint = spawnPoints;
        this.firstPoint = firstPoint;
        this.secondPoint = secondPoint;
        plots.add(this);
    }

    public void setTime(double time) {
        this.time = time;
    }

    public void canStart(boolean value) {
        this.canStart = value;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void addGuest(Player player) {
        this.guests.add(player);
    }

    public void removeGuest(Player player) {
        this.guests.remove(player);
    }

    public void setPlotStatus(PlotStatus plotStatus) {
        this.plotStatus = plotStatus;
    }

    public void setBufferBuildBlock(List<BlockState> blocks) {
        this.bufferBuildBlock = blocks;
    }

    public double getTime() {
        return this.time;
    }

    public boolean timeIsNull() {
        return this.currentTime == 0L;
    }

    public Player getPlayer() {
        return this.player;
    }

    public List<Player> getGuests() {
        return this.guests;
    }

    public Location getSpawnPoint() {
        return this.spawnPoint;
    }

    public Location getFirstPoint() {
        return this.firstPoint;
    }

    public Location getSecondPoint() {
        return this.secondPoint;
    }

    public PlotStatus getPlotStatus() {
        return this.plotStatus;
    }

    public List<BlockState> getBufferBuildBlock() {
        return this.bufferBuildBlock;
    }

    public void startTimer() {
        if (this.currentTime != 0L || !canStart) return;
        this.currentTime = System.currentTimeMillis();
        this.time = 0F;
    }

    public void stopTimer() {
        if (this.currentTime == 0L) return;

        //Bugs #6
        this.time = (double) (System.currentTimeMillis() - this.currentTime) / 1000;
        this.currentTime = 0L;
    }

    public void displayAction() {
        if (this.displayTask != null) return;
        this.displayTask = new Timer(() -> {
            if (this.player != null) {
                if (this.currentTime != 0L && canStart) {
                    this.time = (double) (System.currentTimeMillis() - this.currentTime) / 1000;
                } for (Entity en : player.getWorld().getNearbyEntities(player.getLocation(), 15D, 15D, 15D)) {
                    if (en instanceof Player p) {
                        if (p.equals(player) || Spectator.check(p) || Admin.check(p) || getGuests().contains(p)) {
                            NMSSupport.playAction(p, Message.getMessage("actionbar-time").replace("%time%", String.format("%.3f", this.time)));
                        }
                    }
                }
            }
        });
        this.displayTask.startTimer(1L);
    }

    public void outAction() {
        if (this.displayTask == null) return;
        this.displayTask.stopTimer();
        this.displayTask = null;
    }

    public static List<Plot> getPlots() {
        return plots;
    }

    public static Plot getPlotByPlayer(Player player) {
        for (Plot plot : getPlots()) {
            if (plot.getPlayer() == null) continue;
            if (plot.getPlayer().equals(player)) {
                return plot;
            }
        } return null;
    }

    public static Plot getPlotByGuest(Player player) {
        for (Plot plot : getPlots()) {
            if (plot.getPlayer() == null) continue;
            for (Player p : plot.getGuests()) {
                if (p.equals(player)) {
                    return plot;
                }
            }
        } return null;
    }

    private static class Timer extends BukkitRunnable {
        private Runnable runnable;

        public Timer(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            this.runnable.run();
        }

        public void startTimer(long value) {
            this.runTaskTimerAsynchronously(Main.getPlugin(), 0, value);
        }

        public void stopTimer() {
            this.cancel();
        }
    }
}
