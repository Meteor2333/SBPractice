package cc.meteormc.sbpractice.arena.session;

import cc.meteormc.sbpractice.SBPractice;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.storage.player.PlayerData;
import cc.meteormc.sbpractice.api.storage.preset.PresetData;
import cc.meteormc.sbpractice.config.Messages;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import de.rapha149.signgui.SignGUI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PresetBuildSession {
    private final Island island;
    private final Player player;
    private final boolean isGlobal;

    private String name;
    private XMaterial icon;

    private static final List<PresetBuildSession> SESSIONS = new ArrayList<>();

    public PresetBuildSession(Island island, boolean isGlobal) {
        island.stopTimer();
        this.island = island;
        this.player = island.getOwner();
        this.isGlobal = isGlobal;
        SESSIONS.add(this);
    }

    public void start() {
        this.setupName();
    }

    private void setupName() {
        XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
        SignGUI.builder()
                .setLine(1, "^^^^^")
                .setLine(2, Messages.GUI_PRESET_SAVE_CUSTOMNAME.getMessage())
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
        player.sendMessage(Messages.PREFIX.getMessage() + Messages.GUI_PRESET_SAVE_CUSTOMICON.getMessage());
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onDrop(PlayerDropItemEvent event) {
                if (!event.getPlayer().equals(player)) return;
                event.setCancelled(true);
                icon = XMaterial.matchXMaterial(event.getItemDrop().getItemStack());
                HandlerList.unregisterAll(this);
                close();
            }

            @EventHandler
            public void onCancel(PlayerChangedWorldEvent event) {
                HandlerList.unregisterAll(this);
                close();
            }

            @EventHandler
            public void onCancel(PlayerQuitEvent event) {
                HandlerList.unregisterAll(this);
                close();
            }
        }, SBPractice.getPlugin());
    }

    public void close() {
        SESSIONS.remove(this);
        if (name != null && icon != null) {
            XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
            Arena arena = island.getArena();
            List<BlockState> blocks = new ArrayList<>();
            for (Vector vector : island.getBuildArea().getVectors()) {
                Location loc = vector.toLocation(arena.getWorld());
                blocks.add(SBPractice.getNms().getBlockState(loc));
            }

            String parentPath;
            PresetData preset = new PresetData(name, icon, blocks);
            if (isGlobal) {
                arena.getPresets().add(preset);
                parentPath = arena.getPresetsDir().toString();
            } else {
                PlayerData.getData(player).ifPresent(data -> {
                    data.getPresets().computeIfAbsent(arena, a -> new ArrayList<>()).add(preset);
                });
                parentPath = arena.getPresetsDir() + "/" + player.getUniqueId();
            }

            File parent = new File(parentPath);
            Bukkit.getScheduler().runTaskAsynchronously(SBPractice.getPlugin(), () -> {
                parent.mkdirs();
                File file = new File(parentPath, name + ".preset");
                for (int i = 0; file.exists(); i++) {
                    file = new File(parentPath, name + i + ".preset");
                }
                preset.save(file);
                player.sendMessage(Messages.PREFIX.getMessage() + Messages.GUI_PRESET_SAVE_SUCCESS.getMessage());
            });
        }
    }

    public static @NotNull Optional<PresetBuildSession> getSession(Player player) {
        return SESSIONS.stream()
                .filter(session -> session.player.equals(player))
                .findFirst();
    }
}
