package cc.meteormc.sbpractice;

import cc.carm.lib.mineconfiguration.bukkit.MineConfiguration;
import cc.meteormc.sbpractice.api.SBPracticeAPI;
import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.storage.Database;
import cc.meteormc.sbpractice.api.version.NMS;
import cc.meteormc.sbpractice.arena.DefaultArena;
import cc.meteormc.sbpractice.command.maincmds.MultiplayerCommand;
import cc.meteormc.sbpractice.command.maincmds.SBPracticeCommand;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.config.Message;
import cc.meteormc.sbpractice.config.adapter.XMaterialAdapter;
import cc.meteormc.sbpractice.database.MySQL;
import cc.meteormc.sbpractice.database.SQLite;
import cc.meteormc.sbpractice.hook.PlaceholderAPIHook;
import cc.meteormc.sbpractice.listener.*;
import fr.mrmicky.fastinv.FastInvManager;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.LoadOrder;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Plugin(name = "SBPractice", version = "5.8.5")
@Description("A Minecraft plugin for practicing SpeedBuilder")
@LoadOrder(PluginLoadOrder.POSTWORLD)
@Author("Meteor23333")
@Website("https://github.com/Meteor2333/SBPractice")
@SoftDependency("PlaceholderAPI")
public class Main extends JavaPlugin {
    @Getter
    private static Main plugin;
    @Getter
    private static Database remoteDatabase;
    @Getter
    private static NMS nms;

    private static final List<Arena> ARENAS = new ArrayList<>();

    @Override
    public void onLoad() {
        plugin = this;
        /* Load NMS */
        try {
            String version = Bukkit.getServer().getClass().getName().split("\\.")[3];
            Class<?> nmsClass = Class.forName(getClass().getPackage().getName() + ".version." + version);
            nms = (NMS) nmsClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException("Unsupported server version: " + this.getServer().getVersion());
        }
    }

    @Override
    public void onEnable() {
        /* Register API */
        Bukkit.getServicesManager().register(SBPracticeAPI.class, new API(), this, ServicePriority.Highest);

        /* Display info */
        this.getLogger().info("------------------------------------------------");
        this.getLogger().info("   _____ ____  ____                  __  _         ");
        this.getLogger().info("  / ___// __ )/ __ \\_________ ______/ /_(_)_______ ");
        this.getLogger().info("  \\__ \\/ __  / /_/ / ___/ __ `/ ___/ __/ / ___/ _ \\");
        this.getLogger().info(" ___/ / /_/ / ____/ /  / /_/ / /__/ /_/ / /__/  __/");
        this.getLogger().info("/____/_____/_/   /_/   \\__,_/\\___/\\__/_/\\___/\\___/ ");
        this.getLogger().info("");
        this.getLogger().info("Author: Meteor23333");
        this.getLogger().info("Version: " + getDescription().getVersion());
        this.getLogger().info("Running on: " + getServer().getVersion());
        this.getLogger().info("Java Version: " + System.getProperty("java.version"));
        this.getLogger().info("------------------------------------------------");

        /* Init config */
        MineConfiguration config = new MineConfiguration(this, MainConfig.class, Message.class);
        config.getConfig().adapters().register(new XMaterialAdapter());

        /* Init database */
        if (MainConfig.MYSQL.ENABLE.resolve()) remoteDatabase = new MySQL();
        else remoteDatabase = new SQLite();
        remoteDatabase.initialize();

        /* Init service */
        new Metrics(this, 24481);
        FastInvManager.register(this);

        /* Load arena */
        File[] arenaFiles = new File(this.getDataFolder() + "/Arenas").listFiles(File::isDirectory);
        for (File file : Optional.ofNullable(arenaFiles).orElse(new File[]{})) {
            try {
                String name = file.getName();
                ARENAS.add(new DefaultArena(name).load());
                this.getLogger().info("Loaded Arena " + name);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        this.getLogger().info("Loaded " + ARENAS.size() + " Arenas!");

        /* Register command */
        nms.registerCommand(new MultiplayerCommand());
        nms.registerCommand(new SBPracticeCommand());

        /* Register listener */
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new DataListener(), this);
        pm.registerEvents(new HighjumpListener(), this);
        pm.registerEvents(new PlayerListener(), this);
        pm.registerEvents(new SignListener(), this);
        pm.registerEvents(new WorldListener(), this);

        /* Register hook */
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIHook().register();
            this.getLogger().info("Hooked into PlaceholderAPI support!");
        }
    }

    @Override
    public void onDisable() {
        /* Unregister arena */
        ARENAS.forEach(Arena::unregister);
    }

    public static List<Arena> getArenas() {
        return ARENAS;
    }

    private static class API extends SBPracticeAPI {
        @Override
        public JavaPlugin getPlugin() {
            return Main.getPlugin();
        }

        @Override
        public Database getDatabase() {
            return Main.getRemoteDatabase();
        }

        @Override
        public NMS getNms() {
            return Main.getNms();
        }

        @Override
        public List<Arena> getArenas() {
            return Main.getArenas();
        }
    }
}
