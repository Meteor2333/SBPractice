package com.meteor.SBPractice;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.MultiplayerCommand;
import com.meteor.SBPractice.Commands.StartCommand;
import com.meteor.SBPractice.Database.Database;
import com.meteor.SBPractice.Database.MySQL;
import com.meteor.SBPractice.Database.SQLite;
import com.meteor.SBPractice.Hooks.PlaceholderAPIHook;
import com.meteor.SBPractice.Listeners.*;
import com.meteor.SBPractice.Utils.VersionSupport;
import com.meteor.SBPractice.Utils.Utils;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class Main extends JavaPlugin {

    @Getter
    private static Main plugin;
    @Getter
    private static Database remoteDatabase;

    private static boolean serverSoftwareSupport = true;

    @Override
    public void onLoad() {
        int version = Integer.parseInt(VersionSupport.getServerVersion().split("_")[1]);
        if (version <= 8) {
            if (!VersionSupport.getServerVersion().equalsIgnoreCase("v1_8_R3")) serverSoftwareSupport = false;
        } else if (version > 16) serverSoftwareSupport = false;

        if (serverSoftwareSupport) {
            getLogger().info("------------------------------------------------");
            getLogger().info("§6   _____ ____  ____                  __  _         ");
            getLogger().info("§6  / ___// __ )/ __ \\_________ ______/ /_(_)_______ ");
            getLogger().info("§6  \\__ \\/ __  / /_/ / ___/ __ `/ ___/ __/ / ___/ _ \\");
            getLogger().info("§6 ___/ / /_/ / ____/ /  / /_/ / /__/ /_/ / /__/  __/");
            getLogger().info("§6/____/_____/_/   /_/   \\__,_/\\___/\\__/_/\\___/\\___/ ");
            getLogger().info("");
            getLogger().info("§eAuthor:" + getDescription().getAuthors().get(0));
            getLogger().info("§bVersion: " + getDescription().getVersion());
            getLogger().info("§aRunning on: " + getServer().getVersion());
            getLogger().info("§dJava Version: " + System.getProperty("java.version"));
            getLogger().info("------------------------------------------------");
        }
    }

    @Override
    public void onEnable() {
        if (!serverSoftwareSupport) {
            getLogger().severe("§cCould not load support for server version: " + VersionSupport.getServerVersion());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        plugin = this;

        getConfig().addDefault("enable-proxy", false);
        getConfig().addDefault("plot-check-add-range", 8);
        getConfig().addDefault("default-platform-block", "GRASS");
        getConfig().addDefault("item.clear", "SNOW_BALL");
        getConfig().addDefault("item.prestart", "EGG");

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
                getLogger().severe("§cCould not connect to database! Please verify your credentials and make sure that the server IP is whitelisted in MySQL.");
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            } if (System.currentTimeMillis() - time >= 5000) {
                getLogger().warning("§cIt took " + ((System.currentTimeMillis() - time) / 1000) + " ms to establish a database connection! Using this remote connection is not recommended!");
            }
        } else {
            remoteDatabase = new SQLite();
        } remoteDatabase.initialize();

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook().register();
        }

        if (getConfig().getBoolean("enable-proxy")) {
            if (remoteDatabase instanceof SQLite) {
                getLogger().warning("§cPlease enable the \"MySQL\" database mode!");
                Bukkit.getPluginManager().disablePlugin(this);
            } return;
        }

        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(), this);

        VersionSupport.registerCommand("sbp", new MainCommand("sbp"));
        VersionSupport.registerCommand("mp", new MultiplayerCommand("mp"));
        VersionSupport.registerCommand("start", new StartCommand("start"));

        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer("SBPractice is RELOAD."));

        //Load plots
        List<String> plots = getConfig().getStringList("plots");
        if (plots == null) plots = new ArrayList<>();
        for (String plot : plots) {
            World world = Bukkit.getWorld(plot.split(":")[0]);
            world.setFullTime(0);
            world.setDifficulty(Difficulty.PEACEFUL);
            world.setGameRuleValue("announceAdvancements", "false");
            world.setGameRuleValue("disableElytraMovementCheck", "true");
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setGameRuleValue("doFireTick", "false");
            world.setGameRuleValue("doTileDrops", "false");
            world.setGameRuleValue("doWeatherCycle", "false");
            world.setGameRuleValue("keepInventory", "true");
            world.setGameRuleValue("randomTickSpeed", "0");
            world.setGameRuleValue("spawnRadius", "0");
            world.setAutoSave(true);

            new Plot(Utils.parseLocation(plot.split(";")[0]), Utils.parseLocation(plot.split(";")[1]), Utils.parseLocation(plot.split(";")[2]));
        }
    }

    @Override
    public void onDisable() {
        if (!serverSoftwareSupport) return;
        new ArrayList<>(SBPPlayer.getPlayers()).forEach(SBPPlayer::removePlayer);
    }
}
