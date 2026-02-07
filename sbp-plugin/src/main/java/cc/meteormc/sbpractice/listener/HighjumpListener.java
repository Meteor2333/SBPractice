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
            int height = data.getHighjumpHeight();
            if (height <= 0) return;
            if (island == null) return;
            if (!island.getBuildArea().outset(1).isInside(location)) return;

            event.setCancelled(true);
            if (System.currentTimeMillis() - data.getHighjumpCooldown() >= 1000) {
                player.setAllowFlight(false);
                data.setHighjumpCooldown(System.currentTimeMillis());
                XSound.ENTITY_GHAST_SHOOT.play(player);
                player.setVelocity(new Vector(0D, (height + 1) * 0.15D, 0D));
                new AntiDoubleHighjumpTask(player).runTaskTimer(Main.get(), 3L, 0L);
            }
        });
    }

    private static class AntiDoubleHighjumpTask extends BukkitRunnable {
        private final Player player;
        private final long startTime;

        AntiDoubleHighjumpTask(Player player) {
            this.player = player;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            if (this.player.isOnGround() || System.currentTimeMillis() - this.startTime >= 1500) {
                this.player.setAllowFlight(true);
                this.cancel();
            } else {
                this.player.setAllowFlight(false);
            }
        }
    }
}
