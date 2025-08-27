package cc.meteormc.sbpractice.feature.session;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.Zone;
import cc.meteormc.sbpractice.api.storage.data.BlockData;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.api.storage.data.PresetData;
import cc.meteormc.sbpractice.config.Message;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import de.rapha149.signgui.SignGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class BuildPresetSession {
    private final Island island;
    private final Player player;
    private final boolean isGlobal;

    private String name;
    private XMaterial icon;

    public BuildPresetSession(Island island, boolean isGlobal) {
        island.stopTimer();
        this.island = island;
        this.player = island.getOwner();
        this.isGlobal = isGlobal;
    }

    public void start() {
        this.setupName();
    }

    private void setupName() {
        XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
        SignGUI.builder()
                .setLine(1, "^^^^^")
                .setLine(2, Message.GUI.PRESET.SAVE.SET_NAME.parseLine(player))
                .setHandler((p, result) -> {
                    this.name = result.getLine(0);
                    this.setupIcon();
                    return Collections.emptyList();
                })
                .build()
                .open(player);
    }

    private void setupIcon() {
        XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
        Message.GUI.PRESET.SAVE.SET_ICON.sendTo(player);
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onDrop(PlayerDropItemEvent event) {
                if (!event.getPlayer().equals(player)) return;
                event.setCancelled(true);
                HandlerList.unregisterAll(this);
                icon = XMaterial.matchXMaterial(event.getItemDrop().getItemStack());
                close();
            }

            @EventHandler
            public void onCancel(PlayerChangedWorldEvent event) {
                if (!event.getPlayer().equals(player)) return;
                HandlerList.unregisterAll(this);
            }

            @EventHandler
            public void onCancel(PlayerQuitEvent event) {
                if (!event.getPlayer().equals(player)) return;
                HandlerList.unregisterAll(this);
            }
        }, Main.get());
    }

    public void close() {
        if (name != null && icon != null) {
            XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
            Zone zone = island.getZone();
            List<BlockData> blocks = new ArrayList<>();
            for (Vector point : island.getBuildArea().getPoints()) {
                Location loc = point.toLocation(zone.getWorld());
                blocks.add(Main.get().getNms().getBlockDataAt(loc));
            }

            Bukkit.getScheduler().runTaskAsynchronously(Main.get(), () -> {
                String path;
                Consumer<PresetData> handler;
                if (isGlobal) {
                    path = zone.getPresetFolder().toString();
                    handler = preset -> zone.getPresets().add(preset);
                } else {
                    path = zone.getPresetFolder() + File.pathSeparator + player.getUniqueId();
                    handler = preset -> {
                        PlayerData.getData(player).ifPresent(data -> {
                            data.getPresets().computeIfAbsent(zone, a -> new ArrayList<>()).add(preset);
                        });
                    };
                }

                new File(path).mkdirs();
                File file = new File(path, name + ".preset");
                for (int i = 0; file.exists(); i++) {
                    file = new File(path, name + i + ".preset");
                }

                try {
                    PresetData preset = new PresetData(name, icon, blocks, file);
                    preset.save();
                    handler.accept(preset);
                    Message.GUI.PRESET.SAVE.SUCCESS.sendTo(player);
                } catch (Throwable e) {
                    Message.GUI.PRESET.SAVE.FAILED.sendTo(player);
                }
            });
        }
    }
}
