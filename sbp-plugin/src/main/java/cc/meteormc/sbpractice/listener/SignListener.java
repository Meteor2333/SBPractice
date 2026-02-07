package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.Zone;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.api.storage.data.SignData;
import cc.meteormc.sbpractice.feature.operation.*;
import cc.meteormc.sbpractice.gui.PresetGui;
import com.cryptomorin.xseries.XSound;
import fr.mrmicky.fastparticles.ParticleType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class SignListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (island == null) return;
            if (data.isHidden()) {
                event.setCancelled(true);
                return;
            }

            Block block = event.getClickedBlock();
            if (block == null) return;
            if (!(block.getState() instanceof Sign)) return;

            SignData signs = island.getSigns();
            SignData.Type type = signs.matchType(block);
            if (type == null) return;
            switch (type) {
                case CLEAR:
                    island.executeOperation(new ClearOperation());
                    break;
                case GROUND:
                    island.executeOperation(new GroundOperation());
                    break;
                case MODE:
                    island.executeOperation(new ModeOperation());
                    break;
                case PRESET:
                    new PresetGui(player, island).open(player);
                    break;
                case PREVIEW:
                    island.executeOperation(new PreviewOperation());
                    break;
                case RECORD:
                    island.executeOperation(new RecordOperation());
                    break;
                case START:
                    island.executeOperation(new StartOperation());
                    break;
                case TOGGLE_ZONE:
                    List<Zone> zones = Main.get().getZones();
                    if (!zones.contains(island.getZone())) break;

                    int size = zones.size();
                    int index = zones.indexOf(island.getZone());
                    // index+1 → size-1 → 0 → index-1
                    for (int i = 1; i < size; i++) {
                        Zone zone = zones.get((index + i) % size);
                        if (!zone.isFull()) {
                            island.removeAny(player, false);
                            zone.createIsland(player);
                            break;
                        }
                    }

                    break;
                default:
                    return;
            }

            Location location = block.getLocation().add(0.5, 0.5, 0.5);
            ParticleType.of("CRIT").spawn(
                    block.getWorld(),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    1,
                    0, 0, 0,
                    0.1, null, true
            );
            XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
            island.refreshSigns();
        });
    }
}
