package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.SBPractice;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WorldListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Player) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Player) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Arrow) event.setCancelled(true);
        if (entity instanceof Snowball || entity instanceof Egg) {
            Bukkit.getScheduler().runTaskLater(SBPractice.getPlugin(), entity::remove, 3L);
        }
    }
}
