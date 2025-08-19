package cc.meteormc.sbpractice.api.config;

import cc.meteormc.sbpractice.api.util.ItemBuilder;
import cc.meteormc.sbpractice.api.util.Utils;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@SuppressWarnings("unused")
public class ConfigManager {
    private final File file;
    private final String name;
    private final Plugin plugin;
    private YamlConfiguration config;

    public ConfigManager(Plugin plugin, String path, String name) {
        this.name = name;
        this.plugin = plugin;

        new File(path).mkdirs();
        this.file = new File(path, name + ".yml");
        if (!this.file.exists()) {
            try {
                if (!this.file.createNewFile()) {
                    plugin.getLogger().warning("Failed to create new file: " + this.file.getName());
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                plugin.getLogger().warning("Failed to create new file: " + this.file.getName());
            }
        }
        reload();
    }

    public void addDefault(String path, Object value) {
        this.config.addDefault(path, value);
    }

    public void setObject(String path, Object value) {
        this.config.set(path, value);
    }

    public void setLocation(String path, Location loc) {
        this.config.set(path + ".World", loc.getWorld().getName());
        this.config.set(path + ".X", Double.valueOf(String.format("%.1f", loc.getX())));
        this.config.set(path + ".Y", Double.valueOf(String.format("%.1f", loc.getY())));
        this.config.set(path + ".Z", Double.valueOf(String.format("%.1f", loc.getZ())));
        this.config.set(path + ".Yaw", Double.valueOf(String.format("%.0f", loc.getYaw())));
        this.config.set(path + ".Pitch", Double.valueOf(String.format("%.0f", loc.getPitch())));
    }

    public void setItemStack(String path, ItemStack item) {
        ItemBuilder builder = new ItemBuilder(item);
        this.config.set(path + ".Material", builder.getType().toString());
        if (builder.getAmount() > 1) this.config.set(path + ".Amount", builder.getAmount());
        if (builder.hasDisplayName()) this.config.set(path + ".DisplayName", Utils.decolorize(builder.getDisplayName()));
        if (builder.hasLore()) this.config.set(path + ".Lore", Utils.decolorize(builder.getLore()));
        if (builder.hasEnchantments()) {
            for (Map.Entry<Enchantment, Integer> enchantment : builder.getEnchantments().entrySet()) {
                this.config.set(path + ".Enchantments." + enchantment.getKey().getName(), enchantment.getValue());
            }
        }

        List<String> itemFlags = new ArrayList<>();
        for (ItemFlag flag : ItemFlag.values()) {
            if (builder.hasFlag(flag)) itemFlags.add(flag.name());
        }
        if (!itemFlags.isEmpty()) this.config.set(path + ".ItemFlags", itemFlags);
    }

    public Object getObject(String path) {
        return this.config.get(path);
    }

    public String getString(String path) {
        return Utils.colorize(Optional.ofNullable(this.config.getString(path)).orElse("%" + path + "%"));
    }

    public List<?> getList(String path) {
        return Optional.ofNullable(this.config.getList(path)).orElse(new ArrayList<>())
                .stream()
                .map(text -> {
                    if (text instanceof String) return Utils.colorize((String) text);
                    return text;
                })
                .collect(Collectors.toList());
    }

    public boolean getBoolean(String path) {
        return this.config.getBoolean(path);
    }

    public short getShort(String path) {
        return (short) getInt(path);
    }

    public int getInt(String path) {
        return this.config.getInt(path);
    }

    public long getLong(String path) {
        return this.config.getLong(path);
    }

    public float getFloat(String path) {
        return (float) getDouble(path);
    }

    public double getDouble(String path) {
        return this.config.getDouble(path);
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return this.config.getConfigurationSection(path);
    }

    public Location getLocation(String path) {
        return new Location(Bukkit.getWorld(getString(path + ".World")), getDouble(path + ".X"), getDouble(path + ".Y"), getDouble(path + ".Z"), getFloat(path + ".Yaw"), getFloat(path + ".Pitch"));
    }

    public Location getLocation(String path, World world) {
        return new Location(world, getDouble(path + ".X"), getDouble(path + ".Y"), getDouble(path + ".Z"), getFloat(path + ".Yaw"), getFloat(path + ".Pitch"));
    }

    public ItemStack getItemStack(String path) {
        if (contains(path)) {
            ItemBuilder item = new ItemBuilder(XMaterial.matchXMaterial(getString(path + ".Material")).orElse(XMaterial.BEDROCK));
            if (contains(path + ".Amount")) item.setAmount(getInt(path + ".Amount"));
            if (contains(path + ".DisplayName")) item.setDisplayName(Utils.colorize(getString(path + ".DisplayName")));
            if (contains(path + ".Lore")) item.setLore(Utils.colorize(getList(path + ".Lore").stream().map(text -> (String) text).collect(Collectors.toList())));
            if (contains(path + ".Enchantments")) {
                for (String enchantment : getConfigurationSection(path + ".Enchantments").getKeys(false)) {
                    item.addEnchantment(Enchantment.getByName(enchantment), getInt(path + ".Enchantments." + enchantment));
                }
            }
            if (contains(path + ".ItemFlags")) {
                for (Object flag : getList(path + ".ItemFlags")) item.addFlag(ItemFlag.valueOf((String) flag));
            }
            return item.build();
        } else return null;
    }

    public boolean contains(String path) {
        return this.config.contains(path);
    }

    public void copyDefaults() {
        this.config.options().copyDefaults(true);
    }

    public void save() {
        try {
            this.config.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
            this.plugin.getLogger().warning("Failed to save file: " + this.file.getName());
        }
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }
}
