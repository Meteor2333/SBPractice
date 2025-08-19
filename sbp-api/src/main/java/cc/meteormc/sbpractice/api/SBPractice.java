package cc.meteormc.sbpractice.api;

import cc.meteormc.sbpractice.api.arena.Arena;
import cc.meteormc.sbpractice.api.storage.Database;
import cc.meteormc.sbpractice.api.version.NMS;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public abstract class SBPractice {
    public abstract JavaPlugin getPlugin();

    public abstract Database getDatabase();

    public abstract NMS getNms();

    public abstract List<Arena> getArenas();

    public static SBPractice getInstance() {
        return Bukkit.getServicesManager().getRegistration(SBPractice.class).getProvider();
    }
}
