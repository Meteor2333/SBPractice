package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.api.storage.data.PresetData;
import cc.meteormc.sbpractice.arena.session.MultiplayerSession;
import cc.meteormc.sbpractice.arena.session.PresetBuildSession;
import cc.meteormc.sbpractice.arena.session.SetupSession;
import cc.meteormc.sbpractice.config.Message;
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
        PlayerData data = new PlayerData(uuid, Main.getRemoteDatabase().getPlayerStats(uuid));
        data.register();
        for (Arena arena : Main.getArenas()) {
            File presetsDir = new File(arena.getPresetsDir(), uuid.toString());
            if (!presetsDir.exists()) continue;
            if (!presetsDir.isDirectory()) continue;

            File[] presetFiles = presetsDir.listFiles((dir, name) -> name.endsWith(".preset"));
            if (presetFiles != null) {
                data.getPresets().put(
                        arena,
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

        if (Main.getArenas().isEmpty()) {
            data.get().unregister();
            player.sendMessage(ChatColor.RED + "The SBPractice plugin is not set");
            if (player.hasPermission("sbp.setup")) {
                player.setGameMode(GameMode.CREATIVE);
                player.setFlying(true);
                player.sendMessage(ChatColor.YELLOW + "Please enter '/sbp setup <arenaName>' to setup a arena");
            }
        } else {
            try {
                Main.getArenas().get(0).createIsland(player);
            } catch (Throwable e) {
                player.kickPlayer(e.toString());
                Main.getPlugin().getLogger().log(Level.SEVERE, "Failed to create island for player " + player.getName() + ".", e);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        MultiplayerSession.getSession(player).close();
        PresetBuildSession.getSession(player).ifPresent(PresetBuildSession::close);
        SetupSession.getSession(player).ifPresent(SetupSession::close);

        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (island != null) {
                if (island.getOwner().equals(player)) {
                    island.remove();
                } else {
                    island.removeGuest(player);
                    Message.MULTIPLAYER.LEAVE.PASSIVE.sendTo(island.getOwner(), player.getName());
                }
            }
            data.unregister();
            Main.getRemoteDatabase().setPlayerStats(data.getStats());
        });
    }
}
