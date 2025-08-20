package cc.meteormc.sbpractice;

import cc.meteormc.sbpractice.api.SBPracticeAPI;
import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.arena.exception.ArenaCreationException;
import cc.meteormc.sbpractice.api.storage.Database;
import cc.meteormc.sbpractice.api.storage.preset.PresetData;
import cc.meteormc.sbpractice.api.storage.schematic.Schematic;
import cc.meteormc.sbpractice.api.util.ItemBuilder;
import cc.meteormc.sbpractice.api.version.NMS;
import cc.meteormc.sbpractice.arena.DefaultArena;
import cc.meteormc.sbpractice.command.maincmds.MultiplayerCommand;
import cc.meteormc.sbpractice.command.maincmds.SBPracticeCommand;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.database.MySQL;
import cc.meteormc.sbpractice.database.SQLite;
import cc.meteormc.sbpractice.hook.PlaceholderAPIHook;
import cc.meteormc.sbpractice.listener.BlockListener;
import cc.meteormc.sbpractice.listener.PlayerListener;
import cc.meteormc.sbpractice.listener.WorldListener;
import fr.mrmicky.fastinv.FastInvManager;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.LoadOrder;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@org.bukkit.plugin.java.annotation.plugin.Plugin(name = "SBPractice", version = "2.1.6")
@Description("A Minecraft plugin for practicing SpeedBuilder")
@LoadOrder(PluginLoadOrder.POSTWORLD)
@Author("Meteor23333")
@Website("https://github.com/Meteor2333/SBPractice")
@SoftDependency("PlaceholderAPI")
public class SBPractice extends JavaPlugin {
    @Getter
    private static SBPractice plugin;
    @Getter
    private static Database remoteDatabase;
    @Getter
    private static NMS nms;

    private static final List<Arena> ARENAS = new ArrayList<>();

    @Override
    public void onLoad() {
        plugin = this;
        /* Load version support */
        try {
            Class.forName("org.spigotmc.SpigotConfig");
            String serverVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
            Class<?> nmsClass = Class.forName("cc.meteormc.sbpractice.version." + serverVersion);
            nms = (NMS) nmsClass.getConstructor(Plugin.class).newInstance(this);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new UnsupportedOperationException("Unsupported server version: " + super.getServer().getVersion());
        }
    }

    @Override
    public void onEnable() {
        /* Load api manager */
        Bukkit.getServicesManager().register(SBPracticeAPI.class, new API(), this, ServicePriority.Highest);

        /* Display screen */
        super.getLogger().info("------------------------------------------------");
        super.getLogger().info("   _____ ____  ____                  __  _         ");
        super.getLogger().info("  / ___// __ )/ __ \\_________ ______/ /_(_)_______ ");
        super.getLogger().info("  \\__ \\/ __  / /_/ / ___/ __ `/ ___/ __/ / ___/ _ \\");
        super.getLogger().info(" ___/ / /_/ / ____/ /  / /_/ / /__/ /_/ / /__/  __/");
        super.getLogger().info("/____/_____/_/   /_/   \\__,_/\\___/\\__/_/\\___/\\___/ ");
        super.getLogger().info("");
        super.getLogger().info("Author: Meteor23333");
        super.getLogger().info("Version: " + getDescription().getVersion());
        super.getLogger().info("Running on: " + getServer().getVersion());
        super.getLogger().info("Java Version: " + System.getProperty("java.version"));
        super.getLogger().info("------------------------------------------------");

        /* Load database */
        if (MainConfig.MYSQL_ENABLE.getBoolean()) remoteDatabase = new MySQL();
        else remoteDatabase = new SQLite();
        remoteDatabase.initialize();

        /* Register bukkit service */
        nms.registerCommand(new MultiplayerCommand());
        nms.registerCommand(new SBPracticeCommand());
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(), this);

        /* Init classes */
        FastInvManager.register(this);
        new Metrics(this, 24481);
        ItemBuilder.init(nms);
        PresetData.init(nms);
        Schematic.init(nms);

        /* Load arenas */
        File[] arenaFiles = new File(super.getDataFolder() + "/Arenas").listFiles(File::isDirectory);
        for (File file : Optional.ofNullable(arenaFiles).orElse(new File[]{})) {
            try {
                String name = file.getName();
                ARENAS.add(new DefaultArena(name).load());
                super.getLogger().info("Loaded Arena " + name);
            } catch (ArenaCreationException e) {
                e.printStackTrace();
            }
        }
        super.getLogger().info("Loaded " + ARENAS.size() + " Arenas!");

        /* Load hooks */
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIHook().register();
            this.getLogger().info("Hooked into PlaceholderAPI support!");
        }
    }

    @Override
    public void onDisable() {
        /* Unregister arenas */
        for (Arena arena : ARENAS) {
            arena.unregister();
        }
    }

    public static List<Arena> getArenas() {
        return ARENAS;
    }

    private static class API extends SBPracticeAPI {
        @Override
        public JavaPlugin getPlugin() {
            return SBPractice.getPlugin();
        }

        @Override
        public Database getDatabase() {
            return SBPractice.getRemoteDatabase();
        }

        @Override
        public NMS getNms() {
            return SBPractice.getNms();
        }

        @Override
        public List<Arena> getArenas() {
            return SBPractice.getArenas();
        }
    }
}
