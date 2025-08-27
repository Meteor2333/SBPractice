package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.Zone;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.gui.PresetGui;
import cc.meteormc.sbpractice.operation.*;
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
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            Block block = event.getClickedBlock();
            if (data.isHidden()) {
                event.setCancelled(true);
                return;
            }

            if (block == null) return;
            if (!(block.getState() instanceof Sign)) return;
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

            Location location = block.getLocation();
            if (island.getSigns().getGround().equals(location)) {
                island.executeOperation(new GroundOperation());
            } else if (island.getSigns().getRecord().equals(location)) {
                island.executeOperation(new RecordOperation());
            } else if (island.getSigns().getClear().equals(location)) {
                island.executeOperation(new ClearOperation());
            } else if (island.getSigns().getZone().equals(location)) {
                List<Zone> zones = Main.get().getZones();
                if (zones.contains(island.getZone())) {
                    int index = zones.indexOf(island.getZone()) + 1;
                    if (index >= zones.size()) index = 0;
                    island.removeAny(player, false);
                    zones.get(index).createIsland(player);
                }
            } else if (island.getSigns().getMode().equals(location)) {
                island.executeOperation(new ModeOperation());
            } else if (island.getSigns().getPreset().equals(location)) {
                new PresetGui(player, island).open(player);
            } else if (island.getSigns().getStart().equals(location)) {
                island.executeOperation(new StartOperation());
            } else if (island.getSigns().getPreview().equals(location)) {
                island.executeOperation(new PreviewOperation());
            } else {
                return;
            }

            XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
            ParticleType.of("CRIT").spawn(player, location.clone().add(0.5, 0.5, 0.5), 1, 0, 0, 0, 0.1);
            island.refreshSigns();
        });
    }
}
