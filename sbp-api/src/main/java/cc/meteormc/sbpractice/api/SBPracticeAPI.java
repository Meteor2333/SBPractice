package cc.meteormc.sbpractice.api;

import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.storage.Database;
import cc.meteormc.sbpractice.api.version.NMS;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public abstract class SBPracticeAPI {
    public abstract JavaPlugin getPlugin();

    public abstract Database getDatabase();

    public abstract NMS getNms();

    public abstract List<Arena> getArenas();

    public static SBPracticeAPI getInstance() {
        return Bukkit.getServicesManager().getRegistration(SBPracticeAPI.class).getProvider();
    }
}
