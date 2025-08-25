package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import com.cryptomorin.xseries.XSound;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class HighjumpListener implements Listener {
    @EventHandler(priority = EventPriority.LOW)
    public void onUseHighjump(PlayerToggleFlightEvent event) {
        if (!event.isFlying()) return;
        Player player = event.getPlayer();
        Location location = player.getLocation();
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (!island.getBuildArea().clone().outset(1).isInside(location)) return;

            event.setCancelled(true);
            if (System.currentTimeMillis() - data.getHighjumpCooldown() >= 1250) {
                player.setAllowFlight(false);
                data.setHighjumpCooldown(System.currentTimeMillis());
                XSound.ENTITY_GHAST_SHOOT.play(player);
                player.setVelocity(new Vector(0D, 1.15D, 0D));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnGround()) {
                            player.setAllowFlight(true);
                            this.cancel();
                        } else player.setAllowFlight(false);
                    }
                }.runTaskTimer(Main.getPlugin(), 3L, 0L);
            }
        });
    }
}
