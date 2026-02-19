package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.Zone;
import cc.meteormc.sbpractice.api.storage.PlayerData;
import cc.meteormc.sbpractice.api.storage.PresetData;
import cc.meteormc.sbpractice.feature.session.MultiplayerSession;
import cc.meteormc.sbpractice.feature.session.SetupSession;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
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
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DataListener implements Listener {
    private final Dao<PlayerData.PlayerSettings, UUID> settingsDao;
    private final Dao<PlayerData.PlayerStats, UUID> statsDao;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public DataListener() {
        ConnectionSource source = Main.get().getDbSource();
        try {
            Method method = TableUtils.class.getDeclaredMethod("doCreateTable", Dao.class, boolean.class);
            method.setAccessible(true);

            this.settingsDao = DaoManager.createDao(source, PlayerData.PlayerSettings.class);
            method.invoke(null, this.settingsDao, true);
            this.statsDao = DaoManager.createDao(source, PlayerData.PlayerStats.class);
            method.invoke(null, this.statsDao, true);
        } catch (ReflectiveOperationException | SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        ArrayList<Zone> zones = new ArrayList<>(Main.get().getZones());
        if (zones.isEmpty()) return;

        PlayerData.PlayerSettings settings;
        PlayerData.PlayerStats stats;
        try {
            settings = Optional.ofNullable(settingsDao.queryForId(uuid)).orElse(new PlayerData.PlayerSettings());
            settings.setUuid(uuid);
            stats = Optional.ofNullable(statsDao.queryForId(uuid)).orElse(new PlayerData.PlayerStats());
            stats.setUuid(uuid);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        PlayerData data = new PlayerData(uuid, settings, stats);
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
                throw new IllegalStateException("Failed to create island for player " + player.getName() + "!", e);
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

            CompletableFuture.runAsync(() -> {
                try {
                    settingsDao.createOrUpdate(data.getSettings());
                    statsDao.createOrUpdate(data.getStats());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, this.executor).exceptionally(e -> {
                e.printStackTrace();
                return null;
            });
            data.unregister();
        });
    }
}
