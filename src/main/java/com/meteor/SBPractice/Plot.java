package com.meteor.SBPractice;

import com.meteor.SBPractice.Utils.NMSSupport;
import com.meteor.SBPractice.Utils.Region;
import com.meteor.SBPractice.Utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Plot {
    private double time = 0F;
    private long currentTime = 0L;
    private boolean canStart = true;
    private int countdown = 0;
    private Player player = null;
    private List<Player> guests = new ArrayList<>();
    private Timer displayTask = null;
    private Region region;
    private Location spawnPoint;
    private PlotStatus plotStatus = PlotStatus.NOT_OCCUPIED;
    private List<BlockState> bufferBuildBlock = new ArrayList<>();

    private static List<Plot> plots = new ArrayList<>();

    public Plot(Location spawnPoints, Location firstPoint, Location secondPoint) {
        this.spawnPoint = spawnPoints;
        this.region = new Region(firstPoint, secondPoint);
        plots.add(this);
    }

    public void setTime(double time) {
        this.time = time;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public void canStart(boolean value) {
        this.canStart = value;
    }

    public void setPlayer(@Nullable Player player) {
        this.player = player;
        if (player != null) {
            player.getInventory().setItem(8, new ItemStack(Material.SNOW_BALL));
            player.updateInventory();
            player.setGameMode(GameMode.CREATIVE);
            player.teleport(getSpawnPoint());
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        }
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    public void addGuest(@NotNull Player player) {
        this.guests.add(player);
        player.getInventory().setItem(8, new ItemStack(Material.SNOW_BALL));
        player.updateInventory();
        player.setGameMode(GameMode.CREATIVE);
        player.teleport(getSpawnPoint());
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
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

    public int getCountdown() {
        return this.countdown;
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

    public Region getRegion() {
        return this.region;
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

        this.time = (double) (System.currentTimeMillis() - this.currentTime) / 1000;
        if (this.countdown != 0) this.time = (this.countdown - this.time);

        this.currentTime = 0L;
    }

    public void displayAction() {
        if (this.displayTask != null) return;
        this.displayTask = new Timer(() -> {
            if (this.player != null) {
                if (this.currentTime != 0L && canStart) {
                    this.time = (double) (System.currentTimeMillis() - this.currentTime) / 1000;
                    if (this.countdown != 0) this.time = (this.countdown - this.time);
                } for (Player player : Bukkit.getOnlinePlayers()) {
                    int range = Main.getPlugin().getConfig().getInt("plot-check-add-range");
                    if (new Region(
                            new Location(region.getWorld(), region.getXMax() + range, 0, region.getZMax() + range),
                            new Location(region.getWorld(), region.getXMin() - range, 0, region.getZMin() - range)
                    ).isInside(player.getLocation(), true)) {

                        NMSSupport.playAction(player, Messages.getMessage("actionbar-time").replace("%time%", (this.time < 0 && this.countdown != 0) ? Messages.getMessage("countdown-timeout") : String.format("%.3f", this.time)) + (this.countdown == 0 ? "" : " " + Messages.getMessage("countdown-mode")));
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

    public static Plot getPlotByOwner(Player player) {
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

    public static boolean autoAddPlayerFromPlot(@NotNull Player player, Plot plot, boolean guest) {

        player.setAllowFlight(true);
        player.setExp(0.0F);
        player.setFireTicks(0);
        player.setFlying(false);
        player.setFoodLevel(20);
        player.setHealth(20.0D);
        player.setLevel(0);
        player.getInventory().setArmorContents(null);
        player.getInventory().clear();
        player.updateInventory();
        NMSSupport.showPlayer(player, true);

        if (plot == null) {
            for (Plot p : Plot.getPlots()) {
                if (p.getPlotStatus().equals(PlotStatus.NOT_OCCUPIED)) {
                    p.setPlotStatus(PlotStatus.OCCUPIED);
                    p.stopTimer();
                    p.setTime(0D);
                    p.setCountdown(0);
                    p.canStart(true);

                    List<BlockState> blocks = new ArrayList<>();
                    for (Block block : p.getRegion().getBlocks()) {
                        blocks.add(block.getState());
                    }

                    p.setBufferBuildBlock(blocks);
                    p.displayAction();

                    if (guest) p.addGuest(player);
                    else p.setPlayer(player);
                    return true;
                }
            } return false;
        } else {
            if (guest) plot.addGuest(player);
            else plot.setPlayer(player);
            return true;
        }
    }

    public static void autoRemovePlayerFromPlot(@NotNull Player player) {
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot != null) {
                plot.removeGuest(player);
            } return;
        } Utils.resetPlot(plot);

        List<Player> guests = plot.getGuests();
        if (guests == null || guests.isEmpty()) return;
        for (Player guest : guests) {
            guest.sendMessage(Messages.getMessage("owner-leave"));
            plot.removeGuest(guest);
            if (!Plot.autoAddPlayerFromPlot(guest, null, false)) {
                guest.setGameMode(GameMode.SPECTATOR);
                guest.sendMessage(Messages.getMessage("plot-full"));
                guest.teleport(guest.getWorld().getSpawnLocation());
            }
        }
    }

    public enum PlotStatus {
        OCCUPIED, NOT_OCCUPIED
    }

    public static class SetupSession {
        private final Player player;
        private Location buildPos1, buildPos2, spawnPoint;

        private static List<SetupSession> sessions = new ArrayList<>();

        public SetupSession(Player player) {
            this.player = player;
            sessions.add(this);
        }

        public void setBuildAreaPos1(Location loc) {
            this.buildPos1 = loc.getBlock().getLocation();
        }

        public void setBuildAreaPos2(Location loc) {
            this.buildPos2 = loc.getBlock().getLocation();
        }

        public void setSpawnPoint(Location loc) {
            this.spawnPoint = Utils.simplifyLocation(loc);
        }

        public Location getBuildAreaPos1() {
            return this.buildPos1;
        }

        public Location getBuildAreaPos2() {
            return this.buildPos2;
        }

        public Location getSpawnPoint() {
            return this.spawnPoint;
        }

        public boolean save() {
            if (this.buildPos1 == null) return false;
            if (this.buildPos2 == null) return false;
            if (this.spawnPoint == null) return false;

            this.buildPos1.setWorld(this.spawnPoint.getWorld());
            this.buildPos2.setWorld(this.spawnPoint.getWorld());

            List<String> plots = Main.getPlugin().getConfig().getStringList("plots");
            if (plots == null) plots = new ArrayList<>();
            plots.add(Utils.formatLocation(this.spawnPoint) + ";" + Utils.formatLocation(this.buildPos1) + ";" + Utils.formatLocation(this.buildPos2));
            Main.getPlugin().getConfig().set("plots", plots);
            Main.getPlugin().saveConfig();

            this.player.sendMessage(ChatColor.GREEN + "Added successfully!");
            new Plot(this.spawnPoint, this.buildPos1, this.buildPos2);
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
