package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
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
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DataListener implements Listener {
    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        ArrayList<Zone> zones = new ArrayList<>(Main.get().getZones());
        if (zones.isEmpty()) return;

        PlayerData data = new PlayerData(uuid, Main.get().getDb().getPlayerStats(uuid));
        data.register();
        for (Zone zone : zones) {
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
        Player player = event.getPlayer();
        player.getInventory().clear();
        player.updateInventory();
        player.getActivePotionEffects().stream()
                .map(PotionEffect::getType)
                .forEach(player::removePotionEffect);

        if (Main.get().getZones().isEmpty()) {
            player.sendMessage(ChatColor.RED + "The SBPractice plugin is not set");
            if (player.hasPermission("sbp.setup")) {
                player.setGameMode(GameMode.CREATIVE);
                player.setFlying(true);
                player.sendMessage(ChatColor.YELLOW + "Please enter '/sbp setup <zoneName>' to setup a zone");
            }
            return;
        }

        Optional<PlayerData> data = PlayerData.getData(player);
        if (!data.isPresent()) {
            player.kickPlayer("PlayerData failed to load, rejoin later!");
            return;
        }

        for (Zone zone : Main.get().getZones()) {
            if (zone.isFull()) continue;
            try {
                zone.createIsland(player);
            } catch (Throwable e) {
                player.kickPlayer(e.toString());
                Main.get().getLogger().log(Level.SEVERE, "Failed to create island for player " + player.getName() + ".", e);
            }
            return;
        }

        player.kickPlayer("All zones are currently full, please try again later!");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        SetupSession.getSession(player).forEach(SetupSession::close);
        MultiplayerSession.getSession(player).ifPresent(MultiplayerSession::close);

        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (island != null) {
                island.removeAny(player, false);
            }

            Main.get().getDb().setPlayerStats(data.getStats());
            data.unregister();
        });
    }
}
