package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.api.storage.player.PlayerData;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

public class BlockListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerData.getData(player).ifPresent(data -> {
            if (event instanceof BlockMultiPlaceEvent) {
                BlockMultiPlaceEvent bmpEvent = (BlockMultiPlaceEvent) event;
                for (BlockState state : bmpEvent.getReplacedBlockStates()) {
                    if (!data.getIsland().getBuildArea().isInside(state.getLocation())) {
                        event.setCancelled(true);
                        state.update(true, false);
                    }
                }
            } else {
                if (!data.getIsland().getBuildArea().isInside(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                    event.getBlock().getState().update(true, false);
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData.getData(player).ifPresent(data -> {
            if (!data.getIsland().getBuildArea().isInside(event.getBlock().getLocation())) {
                event.setCancelled(true);
                event.getBlock().getState().update(true, false);
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }
}
