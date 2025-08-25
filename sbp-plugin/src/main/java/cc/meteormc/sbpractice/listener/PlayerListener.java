package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.BuildMode;
import cc.meteormc.sbpractice.api.event.PlayerPerfectRestoreEvent;
import cc.meteormc.sbpractice.api.storage.data.BlockData;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.arena.operation.ClearOperation;
import cc.meteormc.sbpractice.arena.operation.StartOperation;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.config.Message;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class PlayerListener implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        player.updateInventory();
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (item != null) {
                XMaterial material = XMaterial.matchXMaterial(item);
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (material == MainConfig.MATERIAL.CLEAR.resolve()) {
                        island.executeOperation(new ClearOperation());
                    } else if (material == MainConfig.MATERIAL.START.resolve()) {
                        island.executeOperation(new StartOperation());
                    }
                }

                if (MainConfig.MATERIAL.BLOCKED_ITEMS.contains(material)) {
                    event.setCancelled(true);
                }
            }

            //fixme: 这里延时3tick的本意是为了正确地判断多格方块
            //       因为多格方块被放置时 会首先放置玩家点击的那个部分的方块 后续的tick才会放置其余部分
            //       但当玩家建造速度过快时 会导致正在开始判断的时候已经不是正确的状态了（简单来说就是要想其他办法判断多格方块）
            new BukkitRunnable() {
                @Override
                public void run() {
                    boolean isEmpty = true, isPerfect = true;
                    for (Vector vector : island.getBuildArea().getVectors()) {
                        Location location = vector.toLocation(island.getArena().getWorld());
                        BlockData currentBlock = Main.getNms().getBlockDataAt(location);

                        if (isEmpty) {
                            if (currentBlock.getType() != Material.AIR) {
                                isEmpty = false;
                            }
                        }

                        if (isPerfect) {
                            BlockData recordedBlock = island.getRecordedBlocks().get(location);
                            if (recordedBlock == null) {
                                isPerfect = false;
                                continue;
                            }

                            if (!Main.getNms().isSimilarBlock(currentBlock, recordedBlock)) {
                                isPerfect = false;
                            }
                        }
                    }

                    if (!isEmpty) {
                        if (island.isStarted()) {
                            if (isPerfect) {
                                PlayerPerfectRestoreEvent call = new PlayerPerfectRestoreEvent(island);
                                Bukkit.getPluginManager().callEvent(call);
                                if (call.isCancelled()) return;

                                island.setCanStart(false);
                                island.stopTimer();

                                data.getStats().setRestores(data.getStats().getRestores() + 1);
                                for (Player player : island.getNearbyPlayers()) {
                                    XSound.ENTITY_PLAYER_LEVELUP.play(player);
                                    XSound.BLOCK_NOTE_BLOCK_PLING.play(player);
                                    Message.TITLE.PERFECT_MATCH.send(player, island.getFormattedTime());
                                }

                                if (island.getMode() != BuildMode.CONTINUOUS || !island.isActive()) return;
                                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                                    StartOperation.refreshCountdown(island);
                                }, 20L);
                            }
                        } else if (island.getMode() == BuildMode.DEFAULT) {
                            island.startTimer();
                        }
                    } else {
                        island.stopTimer();
                        island.setCanStart(true);
                    }
                }
            }.runTaskLater(Main.getPlugin(), 3L);
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (event.getTo().getY() < island.getArea().getYMin() - 10) {
                player.teleport(island.getSpawnPoint());
            }
        });
        this.refreshVisibility(player);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTeleport(PlayerTeleportEvent event) {
        this.onMove(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRespawn(PlayerRespawnEvent event) {
        this.refreshVisibility(event.getPlayer());
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

    @EventHandler(ignoreCancelled = true)
    public void onBed(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEditBook(PlayerEditBookEvent event) {
        event.setCancelled(true);
    }
}
