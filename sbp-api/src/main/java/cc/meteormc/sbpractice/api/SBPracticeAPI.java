package cc.meteormc.sbpractice.api;

import cc.meteormc.sbpractice.api.version.NMS;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public interface SBPracticeAPI {
    Supplier<SBPracticeAPI> INSTANCE = Suppliers.memoize(() -> {
        return Bukkit.getServicesManager().getRegistration(SBPracticeAPI.class).getProvider();
    });

    JavaPlugin getPlugin();

    NMS getNms();

    List<Zone> getZones();

    static SBPracticeAPI getInstance() {
        return INSTANCE.get();
    }
}
