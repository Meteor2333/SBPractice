package cc.meteormc.sbpractice;

import cc.carm.lib.mineconfiguration.bukkit.MineConfiguration;
import cc.meteormc.sbpractice.api.SBPracticeAPI;
import cc.meteormc.sbpractice.api.Zone;
import cc.meteormc.sbpractice.api.storage.Database;
import cc.meteormc.sbpractice.api.version.NMS;
import cc.meteormc.sbpractice.command.MainCommand;
import cc.meteormc.sbpractice.command.MultiplayerCommand;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.config.Message;
import cc.meteormc.sbpractice.config.adapter.XMaterialAdapter;
import cc.meteormc.sbpractice.database.MySQL;
import cc.meteormc.sbpractice.database.SQLite;
import cc.meteormc.sbpractice.feature.SimpleZone;
import cc.meteormc.sbpractice.hook.PlaceholderAPIHook;
import cc.meteormc.sbpractice.listener.*;
import com.cryptomorin.xseries.XSound;
import fr.mrmicky.fastinv.FastInvManager;
import lombok.Getter;
import me.despical.commandframework.CommandFramework;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.plugin.Description;
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
@Author("Meteor23333")
@Website("https://github.com/Meteor2333/SBPractice")
@SoftDependency("PlaceholderAPI")
public class Main extends JavaPlugin implements SBPracticeAPI {
    private final NMS nms;
    private final Database db;
    private final List<Zone> zones = new ArrayList<>();

    public static Main get() {
        return (Main) SBPracticeAPI.getInstance();
    }

    public Main() {
        try {
            String version = Bukkit.getServer().getClass().getName().split("\\.")[3];
            Class<?> nmsClass = Class.forName(getClass().getPackage().getName() + ".version." + version);
            this.nms = (NMS) nmsClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedOperationException("Unsupported server version: " + this.getServer().getVersion() + "!");
        }

        if (MainConfig.MYSQL.ENABLE.resolve()) this.db = new MySQL();
        else this.db = new SQLite();
    }

    @Override
    public void onLoad() {
        /* Register API */
        Bukkit.getServicesManager().register(SBPracticeAPI.class, this, this, ServicePriority.Highest);

        /* Init config */
        MineConfiguration config = new MineConfiguration(this, MainConfig.class, Message.class);
        config.getConfig().adapters().register(new XMaterialAdapter());

        /* Init database */
        this.db.initialize();
    }

    @Override
    public void onEnable() {
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

        /* Init service */
        new Metrics(this, 24481);
        FastInvManager.register(this);

        /* Load zone */
        File[] files = SimpleZone.ZONES_DIR.listFiles(File::isDirectory);
        for (File file : Optional.ofNullable(files).orElse(new File[]{})) {
            try {
                String name = file.getName();
                zones.add(new SimpleZone(name).load());
                this.getLogger().info("Loaded Zone " + name);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        this.getLogger().info("Loaded " + zones.size() + " Zones!");

        /* Register command */
        CommandFramework cf = new CommandFramework(this);
        cf.registerCommands(new MainCommand());
        cf.registerCommands(new MultiplayerCommand());
        me.despical.commandframework.Message.SHORT_ARG_SIZE.setMessage((command, arguments) -> {
            CommandSender sender = arguments.getSender();
            Message.COMMAND.USAGE.sendTo(sender, command.usage());
            if (sender instanceof Entity) {
                XSound.ENTITY_VILLAGER_NO.play((Entity) sender);
            }
            return true;
        });
        me.despical.commandframework.Message.LONG_ARG_SIZE.setMessage((command, arguments) -> {
            CommandSender sender = arguments.getSender();
            Message.COMMAND.USAGE.sendTo(sender, command.usage());
            if (sender instanceof Entity) {
                XSound.ENTITY_VILLAGER_NO.play((Entity) sender);
            }
            return true;
        });
        me.despical.commandframework.Message.NO_PERMISSION.setMessage((command, arguments) -> {
            Message.COMMAND.NO_PERMISSION.sendTo(arguments.getSender());
            return true;
        });


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
        /* Unregister zone */
        zones.forEach(Zone::unregister);
    }

    @Override
    public Main getPlugin() {
        return this;
    }
}
