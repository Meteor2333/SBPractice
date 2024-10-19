package com.meteor.SBPractice;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.MultiplayerCommand;
import com.meteor.SBPractice.Database.Database;
import com.meteor.SBPractice.Database.MySQL;
import com.meteor.SBPractice.Database.SQLite;
import com.meteor.SBPractice.Hooks.PlaceholderAPIHook;
import com.meteor.SBPractice.Listener.*;
import com.meteor.SBPractice.Utils.Message;
import com.meteor.SBPractice.Utils.NMSSupport;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Main extends JavaPlugin implements Listener {
    private static Main plugin;
    private static Database remoteDatabase;

    @Override
    public void onEnable() {
        if (!NMSSupport.getServerVersion().startsWith("v1_8_R3")) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "SBPractice only supports the v1.8.R3 server version");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("SBPractice is reload."));

        plugin = this;
        //noinspection InstantiationOfUtilityClass
        new Message();
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new BlockPlaceBreakListener(), this);
        Bukkit.getPluginManager().registerEvents(new HighJumpListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);

        //Add #6
        ((CraftServer) getServer()).getCommandMap().register("mp", new MultiplayerCommand("mp"));
        ((CraftServer) getServer()).getCommandMap().register("sbp", new MainCommand("sbp"));

        getConfig().addDefault("default-platform-block", "GRASS");
        getConfig().addDefault("database.enable", false);
        getConfig().addDefault("database.host", "localhost");
        getConfig().addDefault("database.port", 3306);
        getConfig().addDefault("database.database", "sbpractice");
        getConfig().addDefault("database.user", "root");
        getConfig().addDefault("database.pass", "password");
        getConfig().addDefault("database.ssl", false);
        getConfig().options().copyDefaults(true);
        saveConfig();

        if (getConfig().getBoolean("database.enable")) {
            MySQL mySQL = new MySQL();
            long time = System.currentTimeMillis();
            if (mySQL.connect()) remoteDatabase = mySQL;
            else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[SBPractice] Could not connect to database! Please verify your credentials and make sure that the server IP is whitelisted in MySQL.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            } if (System.currentTimeMillis() - time >= 5000) {
                Bukkit.getConsoleSender().sendMessage(
                        ChatColor.RED + "[SBPractice] It took " + ((System.currentTimeMillis() - time) / 1000) +
                        " ms to establish a database connection! Using this remote connection is not recommended!"
                );
            }
        } else {
            remoteDatabase = new SQLite();
        } remoteDatabase.initialize();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook().register();
        }

        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (getConfig().get("Plot." + i) == null) break;
            addPlot(i);
        }
    }

    //Bugs #1 <anti ojang>//////////////////////////////////////

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        e.getBlock().setType(Material.AIR);
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        e.getEntity().remove();
        e.setCancelled(true);
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent e) {
        e.getEntity().remove();
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        e.setFoodLevel(20);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof Player || e.getEntity() instanceof ArmorStand) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity().isOp()) return;
        if (e.getDamager() instanceof Player) e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPiston(BlockPistonExtendEvent e) {
        if (e.getBlock().getWorld().getPlayers() != null) e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPiston(BlockPistonRetractEvent e) {
        if (e.getBlock().getWorld().getPlayers() != null) e.setCancelled(true);
    }

    @EventHandler
    public void onLiquidFlow(BlockFromToEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof Snowball || e.getEntity() instanceof Egg) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> e.getEntity().remove(), 5L);
        } else e.setCancelled(true);
    }

    ////////////////////////////////////////////////////////////

    public static Main getPlugin() {
        return plugin;
    }

    public static Database getRemoteDatabase() {
        return remoteDatabase;
    }

    public static void addPlot(int id) {
        String sp = plugin.getConfig().getString("Plot." + id + ".SpawnPoint");
        String ba = plugin.getConfig().getString("Plot." + id + ".BuildArea");
        if (ba == null || sp == null) return;
        List<String> spData = List.of(sp.split(","));
        List<String> baData = List.of(ba.split(","));
        World world = Bukkit.getWorld(spData.get(0));
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setGameRuleValue("announceAdvancements", "false");
        world.setGameRuleValue("randomTickSpeed", "0");
        world.setAutoSave(true);
        new Plot(
                new Location(
                        world, Double.parseDouble(spData.get(1)),
                        Double.parseDouble(spData.get(2)), Double.parseDouble(spData.get(3)),
                        Float.parseFloat(spData.get(4)), Float.parseFloat(spData.get(5))
                ),

                new Location(
                        world, Double.parseDouble(baData.get(0)), Double.parseDouble(baData.get(1)),
                        Double.parseDouble(baData.get(2))
                ),

                new Location(
                        world, Double.parseDouble(baData.get(3)), Double.parseDouble(baData.get(4)),
                        Double.parseDouble(baData.get(5))
                )
        );
    }
}
