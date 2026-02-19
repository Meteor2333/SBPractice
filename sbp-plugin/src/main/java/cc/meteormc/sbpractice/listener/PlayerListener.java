package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.storage.PlayerData;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.feature.operation.ClearOperation;
import cc.meteormc.sbpractice.feature.operation.StartOperation;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        player.updateInventory();
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            Action action = event.getAction();
            if (item == null) return;
            if (island == null) return;
            if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

            XMaterial material = XMaterial.matchXMaterial(item);
            for (String blockedItem : MainConfig.MATERIAL.BLOCKED_ITEMS) {
                if (material.name().contains(blockedItem)) {
                    event.setCancelled(true);
                    break;
                }
            }

            Block block = event.getClickedBlock();
            if (block != null && block.getState() instanceof Sign) return;
            if (material == MainConfig.MATERIAL.START_ITEM.resolve()) {
                island.executeOperation(new StartOperation());
            } else if (material == MainConfig.MATERIAL.CLEAR_ITEM.resolve()) {
                island.executeOperation(new ClearOperation());
            }
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (island == null) return;
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
        if (!MainConfig.HIDE_PLAYER.resolve()) return;
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (island == null || island.getArea().isInsideIgnoreYaxis(player.getLocation())) {
                if (data.isHidden()) {
                    data.setHidden(false);
                    Main.get().getNms().showPlayer(player);
                }
            } else {
                if (!data.isHidden()) {
                    data.setHidden(true);
                    Main.get().getNms().hidePlayer(player);
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
