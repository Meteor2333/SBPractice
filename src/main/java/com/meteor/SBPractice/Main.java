package com.meteor.SBPractice;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.MultiplayerCommand;
import com.meteor.SBPractice.Commands.StartCommand;
import com.meteor.SBPractice.Database.Database;
import com.meteor.SBPractice.Database.MySQL;
import com.meteor.SBPractice.Database.SQLite;
import com.meteor.SBPractice.Hooks.PlaceholderAPIHook;
import com.meteor.SBPractice.Listeners.*;
import com.meteor.SBPractice.Utils.NMSSupport;
import com.meteor.SBPractice.Utils.Utils;
import org.bukkit.*;
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

import java.util.ArrayList;
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

        plugin = this;
        //noinspection InstantiationOfUtilityClass
        new Messages();

        getConfig().addDefault("plot-check-add-range", 8);
        getConfig().addDefault("default-platform-block", "GRASS");
        getConfig().addDefault("database.mysql", false);
        getConfig().addDefault("database.host", "localhost");
        getConfig().addDefault("database.port", 3306);
        getConfig().addDefault("database.database", "sbpractice");
        getConfig().addDefault("database.user", "root");
        getConfig().addDefault("database.pass", "password");
        getConfig().addDefault("database.ssl", false);
        getConfig().addDefault("proxy.enable", false);
        getConfig().options().copyDefaults(true);
        saveConfig();

        if (getConfig().getBoolean("database.mysql")) {
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

        if (getConfig().getBoolean("proxy.enable")) {
            if (remoteDatabase instanceof SQLite) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Please enable the \"MySQL\" database mode!");
                Bukkit.getPluginManager().disablePlugin(this);
            } return;
        }

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        ((CraftServer) getServer()).getCommandMap().register("sbp", new MainCommand("sbp"));
        ((CraftServer) getServer()).getCommandMap().register("mp", new MultiplayerCommand("mp"));
        ((CraftServer) getServer()).getCommandMap().register("start", new StartCommand("start"));

        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("SBPractice is reload."));

        //Load plots
        List<String> plots = Main.getPlugin().getConfig().getStringList("plots");
        if (plots == null) plots = new ArrayList<>();
        for (String plot : plots) {
            World world = Bukkit.getWorld(plot.split(":")[0]);
            world.setFullTime(0);
            world.setDifficulty(Difficulty.PEACEFUL);
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("doWeatherCycle", "false");
            world.setGameRuleValue("announceAdvancements", "false");
            world.setGameRuleValue("randomTickSpeed", "0");
            world.setAutoSave(true);

            new Plot(Utils.parseLocation(plot.split(";")[0]), Utils.parseLocation(plot.split(";")[1]), Utils.parseLocation(plot.split(";")[2]));
        }
    }

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

    public static Main getPlugin() {
        return plugin;
    }

    public static Database getRemoteDatabase() {
        return remoteDatabase;
    }

}
