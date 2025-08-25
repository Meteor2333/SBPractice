package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

public class BlockListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerData.getData(player).ifPresent(data -> {
            if (event instanceof BlockMultiPlaceEvent) {
                BlockMultiPlaceEvent omp = (BlockMultiPlaceEvent) event;
                for (BlockState state : omp.getReplacedBlockStates()) {
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

    @EventHandler(priority = EventPriority.LOW)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData.getData(player).ifPresent(data -> {
            if (!data.getIsland().getBuildArea().isInside(event.getBlock().getLocation())) {
                event.setCancelled(true);
                event.getBlock().getState().update(true, false);
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
