package cc.meteormc.sbpractice.config;

import cc.meteormc.sbpractice.SBPractice;
import cc.meteormc.sbpractice.api.config.ConfigManager;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum MainConfig {
    MYSQL_ENABLE("mysql.enable", false),
    MYSQL_HOST("mysql.host", "localhost"),
    MYSQL_PORT("mysql.port", 3306),
    MYSQL_DATABASE("mysql.database", "sbpractice"),
    MYSQL_USER("mysql.user", "root"),
    MYSQL_PASSWORD("mysql.password", "password"),
    MYSQL_SSL("mysql.ssl", false),
    BLOCKLIST_ITEMS("blacklist-items", Arrays.asList("ARMOR_STAND", "MINECART", "ITEM_FRAME", "BOAT", "DYE", "POTION", "MILK_BUCKET", "PAINTING", "MAP")),
    CLEAR_ITEM("item.clear", XMaterial.SNOWBALL.name()),
    START_ITEM("item.start", XMaterial.EGG.name()),
    DEFAULT_GROUND_BLOCK("default-ground-block", XMaterial.GRASS_BLOCK.name()),
    ISLAND_DISTANCE_INTERVAL("island-distance-interval", 100),
    MAX_PRESETS_LIMIT("max-presets-limit", 18);

    private final String path;
    private final Object defaultValue;
    private static final ConfigManager CONFIG;

    static {
        CONFIG = new ConfigManager(SBPractice.getPlugin(), SBPractice.getPlugin().getDataFolder().getPath(), "Config");

        for (MainConfig value : values()) {
            CONFIG.addDefault(value.getPath(), value.getDefaultValue());
        }

        CONFIG.copyDefaults();
        CONFIG.save();
    }

    public Optional<XMaterial> getMaterial() {
        return XMaterial.matchXMaterial(CONFIG.getString(this.path));
    }

    public String getString() {
        return CONFIG.getString(this.path);
    }

    public List<?> getList() {
        return CONFIG.getList(this.path);
    }

    public boolean getBoolean() {
        return CONFIG.getBoolean(this.path);
    }

    public int getInt() {
        return CONFIG.getInt(this.path);
    }
}
