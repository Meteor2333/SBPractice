package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.arena.BuildMode;
import cc.meteormc.sbpractice.api.event.PlayerPerfectRestoreEvent;
import cc.meteormc.sbpractice.api.storage.player.PlayerData;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.config.Messages;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Optional;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        event.setJoinMessage(null);
        if (Main.getArenas().isEmpty()) {
            player.sendMessage(Messages.PREFIX.getMessage() + ChatColor.RED + "The plugin is not set");
            if (player.hasPermission("sbp.setup")) {
                player.setGameMode(GameMode.CREATIVE);
                player.setFlying(true);
                player.sendMessage(Messages.PREFIX.getMessage() + ChatColor.YELLOW + "Please enter '/sbp setup <arenaName>' to setup a arena");
            }
        } else {
            PlayerData data = new PlayerData(player, Main.getRemoteDatabase().getPlayerStats(player.getUniqueId()));
            try {
                data.register();
                Main.getArenas().get(0).createIsland(player);
            } catch (Throwable e) {
                data.unregister();
                player.kickPlayer(e.toString());
                e.printStackTrace();
                Main.getPlugin().getLogger().severe("Failed to create island for player " + player.getName() + ".");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (island.getOwner().equals(player)) island.remove();
            else {
                island.removeGuest(player);
                island.getOwner().sendMessage(Messages.PREFIX.getMessage() + Messages.LEAVE_PASSIVE.getMessage().replace("%player%", player.getName()));
            }
            data.unregister();
            Main.getRemoteDatabase().setPlayerStats(data.getStats());
        });
        Main.getNms().fixOtherPlayerTab(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            Block block = event.getClickedBlock();
            if (data.isHidden()) event.setCancelled(true);
            else if (block != null && block.getState() instanceof Sign && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (island.getSigns().getGround().equals(block.getLocation())) {
                    XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                    island.adaptGround();
                    player.sendMessage(Messages.PREFIX.getMessage() + Messages.GROUND.getMessage());
                }
                if (island.getSigns().getRecord().equals(block.getLocation())) {
                    XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                    if (island.getOwner().equals(player)) {
                        island.cachingBuilding();
                        player.sendMessage(Messages.PREFIX.getMessage() + Messages.RECORD.getMessage());
                    } else player.sendMessage(Messages.PREFIX.getMessage() + Messages.CANNOT_DO_THAT.getMessage());
                }
                if (island.getSigns().getClear().equals(block.getLocation())) {
                    XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                    island.clearBuilding();
                    player.sendMessage(Messages.PREFIX.getMessage() + Messages.CLEAR.getMessage());
                }
                if (island.getSigns().getArena().equals(block.getLocation())) {
                    XSound.BLOCK_NOTE_BLOCK_HAT.play(player);

                    List<Arena> arenas = Main.getArenas();
                    if (arenas.contains(island.getArena())) {
                        int index = arenas.indexOf(island.getArena()) + 1;
                        if (index >= arenas.size()) index = 0;

                        if (island.getOwner().equals(player)) island.remove();
                        else {
                            island.removeGuest(player);
                            island.getOwner().sendMessage(Messages.PREFIX.getMessage() + Messages.LEAVE_PASSIVE.getMessage().replace("%player%", player.getName()));
                        }
                        arenas.get(index).createIsland(player);
                    }
                }
                if (island.getSigns().getMode().equals(block.getLocation())) {
                    XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                    if (island.getOwner().equals(player)) {
                        switch (island.toggleBuildMode()) {
                            case DEFAULT:
                                player.sendMessage(Messages.PREFIX.getMessage() + Messages.TOGGLE_BUILD_MODE_DEFAULT.getMessage());
                                break;
                            case COUNTDOWN_ONCE:
                                player.sendMessage(Messages.PREFIX.getMessage() + Messages.TOGGLE_BUILD_MODE_COUNTDOWN_ONCE.getMessage());
                                break;
                            case COUNTDOWN_CONTINUOUS:
                                player.sendMessage(Messages.PREFIX.getMessage() + Messages.TOGGLE_BUILD_MODE_COUNTDOWN_CONTINUOUS.getMessage());
                                break;
                        }
                    } else player.sendMessage(Messages.PREFIX.getMessage() + Messages.CANNOT_DO_THAT.getMessage());
                }
                if (island.getSigns().getPreset().equals(block.getLocation())) {
                    XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                    Bukkit.dispatchCommand(player, "sbp preset");
                }
                if (island.getSigns().getStart().equals(block.getLocation())) {
                    XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                    switch (island.getBuildMode()) {
                        case COUNTDOWN_ONCE:
                            island.activateCountdown();
                            break;
                        case COUNTDOWN_CONTINUOUS:
                            if (island.isStartCountdown()) {
                                island.setStartCountdown(false);
                                player.sendMessage(Messages.PREFIX.getMessage() + Messages.COUNTDOWN_CONTINUOUS_DISABLE.getMessage());
                            } else {
                                island.setStartCountdown(true);
                                player.sendMessage(Messages.PREFIX.getMessage() + Messages.COUNTDOWN_CONTINUOUS_ENABLE.getMessage());
                            }
                            island.activateCountdown();
                            break;
                        default:
                            player.sendMessage(Messages.PREFIX.getMessage() + Messages.CANNOT_DO_THAT.getMessage());
                            break;
                    }
                }
                if (island.getSigns().getPreview().equals(block.getLocation())) {
                    XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                    if (island.getOwner().equals(player)) {
                        island.viewBuilding();
                        player.sendMessage(Messages.PREFIX.getMessage() + Messages.VIEW_BUILDING.getMessage());
                    } else player.sendMessage(Messages.PREFIX.getMessage() + Messages.CANNOT_DO_THAT.getMessage());
                }
                island.refreshSigns();
            } else {
                if (event.getItem() != null) {
                    XMaterial material = XMaterial.matchXMaterial(event.getItem());
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (material == MainConfig.CLEAR_ITEM.getMaterial().orElse(null)) {
                            Bukkit.dispatchCommand(player, "sbp clear");
                        } else if (material == MainConfig.START_ITEM.getMaterial().orElse(null)) {
                            Bukkit.dispatchCommand(player, "sbp start");
                        }
                    }

                    for (Object item : MainConfig.BLOCKLIST_ITEMS.getList()) {
                        if (item instanceof String) {
                            if (material.name().contains((String) item)) event.setCancelled(true);
                        }
                    }
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        boolean isFull = true, isPerfect = true;
                        for (Vector vector : island.getBuildArea().getVectors()) {
                            BlockState state = Main.getNms().getBlockState(vector.toLocation(island.getBuildArea().getWorld()));
                            Optional<BlockState> optionalState = Optional.ofNullable(island.getRecordedBlocks().getOrDefault(state.getLocation(), null));
                            if (state.getType() != Material.AIR) isFull = false;
                            if (!optionalState.isPresent() || !Main.getNms().isSimilarBlockState(state, optionalState.get())) isPerfect = false;
                        }

                        if (!isFull) {
                            if (island.isStarted()) {
                                if (isPerfect) {
                                    PlayerPerfectRestoreEvent call = new PlayerPerfectRestoreEvent(island);
                                    Bukkit.getPluginManager().callEvent(call);
                                    if (call.isCancelled()) return;

                                    island.setCanStart(false);
                                    island.stopTimer();

                                    for (Player p : island.getSpawnPoint().getWorld().getPlayers()) {
                                        if (island.getArea().isInsideIgnoreYaxis(p.getLocation())) {
                                            XSound.ENTITY_PLAYER_LEVELUP.play(p);
                                            XSound.BLOCK_NOTE_BLOCK_PLING.play(p);
                                            Main.getNms().sendTitle(p, Messages.PERFECT_MATCH_TITLE.getMessage(), Messages.PERFECT_MATCH_SUBTITLE.getMessage().replace("%time%", island.getFormattedTime()), 5, 30, 5);
                                        }
                                    }
                                    data.getStats().setRestores(data.getStats().getRestores() + 1);

                                    if (island.getBuildMode() == BuildMode.COUNTDOWN_CONTINUOUS && island.isStartCountdown()) {
                                        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), island::activateCountdown, 20L);
                                    }
                                }
                            } else island.startTimer();
                        } else {
                            island.stopTimer();
                            island.setCanStart(true);
                        }
                    }
                }.runTaskLater(Main.getPlugin(), 3L);
            }

            player.updateInventory();
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (event.getTo().getY() < island.getArea().getYMin() - 10) {
                player.teleport(island.getSpawnPoint());
            }
            this.refreshVisibility(player);
        });
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (event.getTo().getY() < island.getArea().getYMin() - 10) {
                player.teleport(island.getSpawnPoint());
            }
            this.refreshVisibility(player);
        });
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        this.refreshVisibility(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (!event.isFlying()) return;
        Player player = event.getPlayer();
        Location location = player.getLocation();
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (island.getBuildArea().clone().outset(1).isInside(location)) {
                event.setCancelled(true);
                if (System.currentTimeMillis() - data.getHighjumpCooldown() >= 1250) {
                    player.setAllowFlight(false);
                    data.setHighjumpCooldown(System.currentTimeMillis());
                    XSound.ENTITY_GHAST_SHOOT.play(player);
                    player.setVelocity(new Vector(0D, 1.15D, 0D));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (player.isOnGround()) {
                                player.setAllowFlight(true);
                                this.cancel();
                            } else player.setAllowFlight(false);
                        }
                    }.runTaskTimer(Main.getPlugin(), 3L, 0L);
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        event.setCancelled(true);
    }

    private void refreshVisibility(Player player) {
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (island.getArea().isInsideIgnoreYaxis(player.getLocation())) {
                if (data.isHidden()) {
                    data.setHidden(false);
                    Main.getNms().showPlayer(player);
                }
            } else {
                if (!data.isHidden()) {
                    data.setHidden(true);
                    Main.getNms().hidePlayer(player);
                }
            }
        });
    }
}
