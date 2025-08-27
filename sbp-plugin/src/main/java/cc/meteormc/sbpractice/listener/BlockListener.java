package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class BlockListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlace(BlockPlaceEvent event) {
        if (event instanceof BlockMultiPlaceEvent) {
            BlockMultiPlaceEvent omp = (BlockMultiPlaceEvent) event;
            for (BlockState state : omp.getReplacedBlockStates()) {
                this.handleBlock(
                        event.getPlayer(),
                        state,
                        event
                );
            }
        } else {
            this.handleBlock(
                    event.getPlayer(),
                    event.getBlock().getState(),
                    event
            );
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPour(PlayerBucketEmptyEvent event) {
        this.handleBlock(
                event.getPlayer(),
                event.getBlockClicked().getRelative(event.getBlockFace()).getState(),
                event
        );
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBreak(BlockBreakEvent event) {
        this.handleBlock(
                event.getPlayer(),
                event.getBlock().getState(),
                event
        );
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onFill(PlayerBucketFillEvent event) {
        this.handleBlock(
                event.getPlayer(),
                event.getBlockClicked().getRelative(event.getBlockFace()).getState(),
                event
        );
    }

    private void handleBlock(Player player, BlockState block, Cancellable event) {
        PlayerData.getData(player).ifPresent(data -> {
            if (!data.getIsland().getBuildArea().isInside(block.getLocation())) {
                event.setCancelled(true);
                block.update(true, false);
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPiston(BlockPistonExtendEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPiston(BlockPistonRetractEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFluid(BlockFromToEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }
}
