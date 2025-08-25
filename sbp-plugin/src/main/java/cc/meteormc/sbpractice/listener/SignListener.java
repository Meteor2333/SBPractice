package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.arena.operation.*;
import cc.meteormc.sbpractice.config.Message;
import cc.meteormc.sbpractice.gui.PresetGui;
import com.cryptomorin.xseries.XSound;
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

            if (island.getSigns().getGround().equals(block.getLocation())) {
                XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                island.executeOperation(new GroundOperation());
            }
            if (island.getSigns().getRecord().equals(block.getLocation())) {
                XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                if (island.getOwner().equals(player)) {
                    data.getIsland().executeOperation(new RecordOperation());
                } else {
                    Message.BASIC.CANNOT_DO_THAT.sendTo(player);
                }
            }
            if (island.getSigns().getClear().equals(block.getLocation())) {
                XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                island.executeOperation(new ClearOperation());
            }
            if (island.getSigns().getSelectArena().equals(block.getLocation())) {
                XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                List<Arena> arenas = Main.getArenas();
                if (arenas.contains(island.getArena())) {
                    int index = arenas.indexOf(island.getArena()) + 1;
                    if (index >= arenas.size()) index = 0;

                    if (island.getOwner().equals(player)) island.remove();
                    else {
                        island.removeGuest(player);
                        Message.MULTIPLAYER.LEAVE.PASSIVE.sendTo(island.getOwner(), player.getName());
                    }
                    arenas.get(index).createIsland(player);
                }
            }
            if (island.getSigns().getMode().equals(block.getLocation())) {
                XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                if (island.getOwner().equals(player)) {
                    island.executeOperation(new ModeOperation());
                } else {
                    Message.BASIC.CANNOT_DO_THAT.sendTo(player);
                }
            }
            if (island.getSigns().getPreset().equals(block.getLocation())) {
                XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                new PresetGui(player, island).open(player);
            }
            if (island.getSigns().getStart().equals(block.getLocation())) {
                XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                island.executeOperation(new StartOperation());
            }
            if (island.getSigns().getPreview().equals(block.getLocation())) {
                XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
                if (island.getOwner().equals(player)) {
                    island.executeOperation(new PreviewOperation());
                } else {
                    Message.BASIC.CANNOT_DO_THAT.sendTo(player);
                }
            }

            island.refreshSigns();
        });
    }
}
