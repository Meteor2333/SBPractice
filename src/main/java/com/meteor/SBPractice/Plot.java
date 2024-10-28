package com.meteor.SBPractice;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Utils.VersionSupport;
import com.meteor.SBPractice.Utils.Region;
import com.meteor.SBPractice.Utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Plot {
    private double time = 0F;
    private long currentTime = 0L;
    private boolean canStart = true;
    private int countdown = 0;
    private SBPPlayer player = null;
    private List<SBPPlayer> guests = new ArrayList<>();
    private Timer displayTask = null;
    private Region region;
    private Location spawnPoint;
    private PlotStatus plotStatus = PlotStatus.NOT_OCCUPIED;
    private List<BlockState> bufferBuildBlock = new ArrayList<>();

    @Getter
    private static List<Plot> plots = new ArrayList<>();

    public Plot(Location spawnPoint, Location firstPoint, Location secondPoint) {
        this.spawnPoint = spawnPoint;
        this.region = new Region(firstPoint, secondPoint);
        plots.add(this);
    }

    public void setPlayer(@Nullable SBPPlayer player) {
        this.player = player;
        if (player != null) {
            player.resetPlayer();
            player.sendPlotItem();
            player.getPlayer().teleport(getSpawnPoint());
        }
    }

    public void addGuest(@NotNull SBPPlayer player) {
        guests.add(player);
        player.resetPlayer();
        player.sendPlotItem();
        player.getPlayer().teleport(getSpawnPoint());
    }

    public void removeGuest(SBPPlayer player) {
        player.resetPlayer();
        guests.remove(player);
    }

    public void startTimer() {
        if (currentTime != 0L || !canStart) return;
        currentTime = System.currentTimeMillis();
        time = 0F;
    }

    public void stopTimer() {
        if (currentTime == 0L) return;

        time = (double) (System.currentTimeMillis() - currentTime) / 1000;
        if (countdown != 0) time = (countdown - time);

        currentTime = 0L;
    }

    public void displayAction() {
        if (displayTask != null) return;
        displayTask = new Timer(() -> {
            if (player != null) {
                if (currentTime != 0L && canStart) {
                    time = (double) (System.currentTimeMillis() - currentTime) / 1000;
                    if (countdown != 0) time = (countdown - time);
                } for (SBPPlayer player : SBPPlayer.getPlayers()) {
                    int range = Main.getPlugin().getConfig().getInt("plot-check-add-range");
                    if (new Region(
                            new Location(region.getWorld(), region.getXMax() + range, 0, region.getZMax() + range),
                            new Location(region.getWorld(), region.getXMin() - range, 0, region.getZMin() - range)
                    ).isInside(player.getLocation(), true)) {
                        VersionSupport.playAction(player.getPlayer(), Messages.ACTIONBAR_TIME.getMessage().replace("%time%", (time < 0 && countdown != 0) ? Messages.COUNTDOWN_TIMEOUT.getMessage() : String.format("%.3f", time)) + (countdown == 0 ? "" : " " + Messages.COUNTDOWN_MODE.getMessage()));
                    }
                }
            }
        });
        displayTask.startTimer(1L);
    }

    public void outAction() {
        if (displayTask == null) return;
        displayTask.stopTimer();
        displayTask = null;
    }

    public void clear() {
        Region region = new Region(
                new Location(getRegion().getWorld(), getRegion().getXMax() + 1, getRegion().getYMax() + 1, getRegion().getZMax() + 1),
                new Location(getRegion().getWorld(), getRegion().getXMin() - 1, getRegion().getYMin(), getRegion().getZMin() - 1)
        );
        for (Entity entity : getSpawnPoint().getWorld().getEntities()) {
            if (!region.isInside(entity.getLocation(), false)) continue;
            if (entity.getType() == EntityType.PLAYER) continue;
            entity.remove();
        } region.fill(Material.AIR);
    }

    public static Plot getPlotByOwner(@NotNull SBPPlayer player) {
        for (Plot plot : getPlots()) {
            if (plot.getPlayer() == null) continue;
            if (plot.getPlayer().equals(player)) {
                return plot;
            }
        } return null;
    }

    public static Plot getPlotByGuest(@NotNull SBPPlayer player) {
        for (Plot plot : getPlots()) {
            if (plot.getPlayer() == null) continue;
            for (SBPPlayer p : plot.getGuests()) {
                if (p.equals(player)) {
                    return plot;
                }
            }
        } return null;
    }

    public static boolean autoAddPlayerFromPlot(@NotNull SBPPlayer player, Plot plot, boolean guest) {
        Utils.updatePlots();
        player.resetPlayer();

        if (plot == null) {
            boolean isFull = true;
            for (Plot p : Plot.getPlots()) {
                if (p.getPlotStatus().equals(PlotStatus.NOT_OCCUPIED)) {
                    p.setPlotStatus(PlotStatus.OCCUPIED);
                    p.stopTimer();
                    p.setTime(0D);
                    p.setCountdown(0);
                    p.setCanStart(true);

                    List<BlockState> blocks = new ArrayList<>();
                    for (Block block : p.getRegion().getBlocks()) {
                        blocks.add(block.getState());
                    }

                    p.setBufferBuildBlock(blocks);
                    p.displayAction();

                    plot = p;
                    isFull = false;
                    break;
                }
            } if (isFull) return false;
        }

        if (guest) plot.addGuest(player);
        else plot.setPlayer(player);
        return true;
    }

    public static void autoRemovePlayerFromPlot(@NotNull SBPPlayer player) {
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot != null) {
                plot.removeGuest(player);
            } return;
        }

        List<SBPPlayer> guests = plot.getGuests();
        if (!(guests == null || guests.isEmpty())) {
            for (SBPPlayer guest : guests) {
                guest.sendMessage(Messages.OWNER_LEAVE.getMessage());
                plot.removeGuest(guest);
                if (!Plot.autoAddPlayerFromPlot(guest, null, false)) {
                    player.setVisibility(false);
                    player.sendMessage(Messages.PLOT_FULL.getMessage());
                    player.getPlayer().teleport(Plot.getPlots().get(0).getSpawnPoint());
                }
            }
        } Utils.resetPlot(plot);
        Utils.updatePlots();
    }

    public enum PlotStatus {
        OCCUPIED, NOT_OCCUPIED
    }

    @Getter
    @Setter
    public static class SetupSession {
        private final Player player;
        private Location buildPos1, buildPos2, spawnPoint;

        private static List<SetupSession> sessions = new ArrayList<>();

        public SetupSession(Player player) {
            this.player = player;
            sessions.add(this);
        }

        public boolean save() {
            if (buildPos1 == null) return false;
            if (buildPos2 == null) return false;
            if (spawnPoint == null) return false;

            buildPos1.setWorld(spawnPoint.getWorld());
            buildPos2.setWorld(spawnPoint.getWorld());

            List<String> plots = Main.getPlugin().getConfig().getStringList("plots");
            if (plots == null) plots = new ArrayList<>();
            plots.add(Utils.formatLocation(spawnPoint) + ";" + Utils.formatLocation(buildPos1) + ";" + Utils.formatLocation(buildPos2));
            Main.getPlugin().getConfig().set("plots", plots);
            Main.getPlugin().saveConfig();

            PlayerInventory inv = player.getPlayer().getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                if (inv.getItem(i) == null) continue;
                if (VersionSupport.getTag(inv.getItem(i), "sbpractice").isEmpty()) continue;
                inv.clear(i);
            }

            player.sendMessage(ChatColor.GREEN + "Added successfully!");
            new Plot(spawnPoint, buildPos1, buildPos2);
            sessions.remove(this);
            return true;
        }

        public static SetupSession getSessionByPlayer(Player player) {
            for (SetupSession session : sessions) {
                if (session.player == player) {
                    return session;
                }
            } return null;
        }
    }

    private static class Timer extends BukkitRunnable {
        private final Runnable runnable;

        public Timer(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            runnable.run();
        }

        public void startTimer(long value) {
            runTaskTimerAsynchronously(Main.getPlugin(), 0, value);
        }

        public void stopTimer() {
            cancel();
        }
    }
}
