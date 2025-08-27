package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Zone;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.api.storage.data.PresetData;
import cc.meteormc.sbpractice.feature.session.MultiplayerSession;
import cc.meteormc.sbpractice.feature.session.SetupSession;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DataListener implements Listener {
    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        PlayerData data = new PlayerData(uuid, Main.get().getDb().getPlayerStats(uuid));
        data.register();
        for (Zone zone : Main.get().getZones()) {
            File presetsDir = new File(zone.getPresetFolder(), uuid.toString());
            if (!presetsDir.exists()) continue;
            if (!presetsDir.isDirectory()) continue;

            File[] presetFiles = presetsDir.listFiles((dir, name) -> name.endsWith(".preset"));
            if (presetFiles != null) {
                data.getPresets().put(
                        zone,
                        Arrays.stream(presetFiles)
                                .map(PresetData::load)
                                .collect(Collectors.toList())
                );
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();
        Optional<PlayerData> data = PlayerData.getData(player);
        if (!data.isPresent()) {
            player.kickPlayer("PlayerData failed to load, rejoin later!");
            return;
        }

        if (Main.get().getZones().isEmpty()) {
            data.get().unregister();
            player.sendMessage(ChatColor.RED + "The SBPractice plugin is not set");
            if (player.hasPermission("sbp.setup")) {
                player.setGameMode(GameMode.CREATIVE);
                player.setFlying(true);
                player.sendMessage(ChatColor.YELLOW + "Please enter '/sbp setup <zoneName>' to setup a zone");
            }
        } else {
            try {
                Main.get().getZones().get(0).createIsland(player);
            } catch (Throwable e) {
                player.kickPlayer(e.toString());
                Main.get().getLogger().log(Level.SEVERE, "Failed to create island for player " + player.getName() + ".", e);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        SetupSession.getSession(player).forEach(SetupSession::close);
        MultiplayerSession.getSession(player).ifPresent(MultiplayerSession::close);

        PlayerData.getData(player).ifPresent(data -> {
            data.unregister();
            data.getIsland().removeAny(player, false);
            Main.get().getDb().setPlayerStats(data.getStats());
        });
    }
}
