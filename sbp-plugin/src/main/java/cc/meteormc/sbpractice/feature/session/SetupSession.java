package cc.meteormc.sbpractice.feature.session;

import cc.carm.lib.configuration.value.standard.ConfiguredValue;
import cc.carm.lib.mineconfiguration.bukkit.MineConfiguration;
import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Zone;
import cc.meteormc.sbpractice.api.helper.Area;
import cc.meteormc.sbpractice.api.helper.ItemBuilder;
import cc.meteormc.sbpractice.api.storage.data.SchematicData;
import cc.meteormc.sbpractice.config.ZoneConfig;
import cc.meteormc.sbpractice.feature.SimpleZone;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.Titles;
import com.google.common.collect.ImmutableMap;
import fr.mrmicky.fastparticles.ParticleType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.material.Sign;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SetupSession extends BukkitRunnable {
    private final String name;
    private final Player player;
    private final ZoneConfig config;

    private static final Map<String, Entry<?>> ENTRIES;

    static {
        ImmutableMap.Builder<String, Entry<?>> builder = ImmutableMap.builder();
        builder.put("area", new Entry<>(
                "[Setup Area]",
                "Set the main area of the zone.",
                zc -> zc.MAP.AREA,
                SetupSession::setupArea
        ));
        builder.put("build-area", new Entry<>(
                "[Setup Build Area]",
                "Set the build area of the zone.",
                zc -> zc.MAP.BUILD_AREA,
                SetupSession::setupArea
        ));
        builder.put("spawn", new Entry<>(
                "[Setup Spawn]",
                "Set the spawn of the zone.",
                zc -> zc.MAP.SPAWN,
                SetupSession::setupPoint
        ));
        builder.put("ground-sign", new Entry<>(
                "[Setup Ground Sign]",
                "Set the ground sign of the zone.",
                zc -> zc.SIGN.GROUND,
                SetupSession::setupSign
        ));
        builder.put("record-sign", new Entry<>(
                "[Setup Record Sign]",
                "Set the record sign of the zone.",
                zc -> zc.SIGN.RECORD,
                SetupSession::setupSign
        ));
        builder.put("clear-sign", new Entry<>(
                "[Setup Clear Sign]",
                "Set the clear sign of the zone.",
                zc -> zc.SIGN.CLEAR,
                SetupSession::setupSign
        ));
        builder.put("togglezone-sign", new Entry<>(
                "[Setup Toggle Zone Sign]",
                "Set the toggle zone sign of the zone.",
                zc -> zc.SIGN.TOGGLE_ZONE,
                SetupSession::setupSign
        ));
        builder.put("mode-sign", new Entry<>(
                "[Setup Mode Sign]",
                "Set the mode sign of the zone.",
                zc -> zc.SIGN.MODE,
                SetupSession::setupSign
        ));
        builder.put("preset-sign", new Entry<>(
                "[Setup Preset Sign]",
                "Set the preset sign of the zone.",
                zc -> zc.SIGN.PRESET,
                SetupSession::setupSign
        ));
        builder.put("start-sign", new Entry<>(
                "[Setup Start Sign]",
                "Set the start sign of the zone.",
                zc -> zc.SIGN.START,
                SetupSession::setupSign
        ));
        builder.put("preview-sign", new Entry<>(
                "[Setup Preview Sign]",
                "Set the preview sign of the zone.",
                zc -> zc.SIGN.PREVIEW,
                SetupSession::setupSign
        ));
        ENTRIES = builder.build();
    }

    private static final List<SetupSession> SESSIONS = new CopyOnWriteArrayList<>();

    public SetupSession(Player player, String name) throws IllegalArgumentException {
        if (Main.get().getZones().stream().map(Zone::getName).anyMatch(zn -> zn.equals(name))) {
            throw new IllegalArgumentException(ChatColor.RED + "Zone '" + name + "' already exists!");
        }

        this.name = name;
        this.player = player;
        try {
            File tempFile = File.createTempFile("sbp-setup", ".yml");
            tempFile.deleteOnExit();
            this.config = new ZoneConfig(MineConfiguration.from(tempFile, null));
        } catch (IOException e) {
            throw new IllegalArgumentException(ChatColor.RED + "Failed to create temporary config file!");
        }
        SESSIONS.add(this);
    }

    public static Optional<SetupSession> getSession(String name) {
        return SESSIONS.stream()
                .filter(session -> session.name.equals(name))
                .findFirst();
    }

    public static List<SetupSession> getSession(Player player) {
        return SESSIONS.stream()
                .filter(session -> session.player.equals(player))
                .collect(Collectors.toList());
    }

    @Override
    public void run() {
        config.MAP.AREA.optional().ifPresent(area -> drawArea(area, Color.GREEN));
        config.MAP.BUILD_AREA.optional().ifPresent(area -> drawArea(area, Color.RED));
        config.MAP.SPAWN.optional().ifPresent(point -> drawPoint(point, Color.RED));
    }

    private void drawArea(Area area, Color color) {
        Vector[] corners = {
                new Vector(area.getXMin(), area.getYMin(), area.getZMin()),
                new Vector(area.getXMin(), area.getYMin(), area.getZMax()),
                new Vector(area.getXMin(), area.getYMax(), area.getZMin()),
                new Vector(area.getXMin(), area.getYMax(), area.getZMax()),
                new Vector(area.getXMax(), area.getYMin(), area.getZMin()),
                new Vector(area.getXMax(), area.getYMin(), area.getZMax()),
                new Vector(area.getXMax(), area.getYMax(), area.getZMin()),
                new Vector(area.getXMax(), area.getYMax(), area.getZMax())
        };
        for (int i = 0; i < 8; i++) {
            for (int j = i + 1; j < 8; j++) {
                if (Integer.bitCount(i ^ j) != 1) continue;
                Vector diff = corners[j].subtract(corners[i]);
                int points = (int) (diff.length() * 4);
                Vector step = diff.multiply(1.0 / points);
                Vector current = corners[i].clone();
                for (int k = 0; k <= points; k++) {
                    this.drawPoint(current.toLocation(null), color);
                    current.add(step);
                }
            }
        }
    }

    private void drawPoint(Location point, Color color) {
        ParticleType.of("REDSTONE").spawn(
                this.player, point, 1,
                color.getRed() / 255D,
                color.getGreen() / 255D,
                color.getBlue() / 255D
        );
    }

    public void handleCommand(String args) {
        if (args.startsWith("save")) {
            this.save();
            return;
        }

        String[] split = args.split(" ");
        //noinspection unchecked
        Entry<Object> entry = (Entry<Object>) ENTRIES.get(split[0]);
        if (entry == null) {
            player.sendMessage(ChatColor.GRAY + "----------------------------------");
            player.sendMessage(ChatColor.AQUA + "Please click the entries listed below to setup!");
            for (Map.Entry<String, Entry<?>> e : ENTRIES.entrySet()) {
                String key = e.getKey();
                Entry<?> value = e.getValue();
                boolean present = value.getValue().apply(config).optional().isPresent();
                player.spigot().sendMessage(
                        new ComponentBuilder(" - ")
                                .color(ChatColor.DARK_GRAY.asBungee())
                                .append(value.getMessage())
                                .color(present ? ChatColor.GREEN.asBungee() : ChatColor.RED.asBungee())
                                .event(new ClickEvent(
                                        ClickEvent.Action.SUGGEST_COMMAND,
                                        "/sbp setup " + name + " " + key
                                ))
                                .event(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        TextComponent.fromLegacyText(ChatColor.GRAY + value.getDescription())
                                ))
                                .create()
                );
            }
            player.sendMessage(ChatColor.GRAY + "----------------------------------");
            return;
        }

        XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
        entry.getAction().accept(this, entry.getValue().apply(this.config));
        this.checkComplete();
    }

    private void setupArea(ConfiguredValue<Area> value) {
        value.set(null);
        PlayerInventory inventory = this.player.getInventory();
        inventory.setItem(
                inventory.getHeldItemSlot(),
                new ItemBuilder(XMaterial.IRON_AXE)
                        .setDisplayName(ChatColor.AQUA + "Select 2 points!")
                        .build()
        );
        Titles.sendTitle(
                this.player,
                0, 20, 5,
                "",
                ChatColor.GREEN + "Use the iron axe to select 2 points!"
        );
        Bukkit.getPluginManager().registerEvents(new Listener() {
            private Vector pos1, pos2;

            @EventHandler
            public void onInteract(PlayerInteractEvent event) {
                Block block = event.getClickedBlock();
                if (block == null) return;
                if (!event.getPlayer().equals(player)) return;
                if (XMaterial.matchXMaterial(event.getItem()) != XMaterial.IRON_AXE) return;

                event.setCancelled(true);
                Vector vector = block.getLocation().toVector();
                switch (event.getAction()) {
                    case LEFT_CLICK_BLOCK:
                        pos1 = vector;
                        break;
                    case RIGHT_CLICK_BLOCK:
                        pos2 = vector;
                        break;
                    default:
                        return;
                }

                if (pos1 != null && pos2 != null) {
                    HandlerList.unregisterAll(this);
                    Area area = new Area(pos1, pos2);
                    value.set(area);
                    XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
                    player.sendMessage(ChatColor.GREEN + "Area successfully updated to " + area + ".");
                    checkComplete();
                }
            }

            @EventHandler
            public void onCancel(PlayerChangedWorldEvent event) {
                if (!event.getPlayer().equals(player)) return;
                HandlerList.unregisterAll(this);
            }

            @EventHandler
            public void onCancel(PlayerQuitEvent event) {
                if (!event.getPlayer().equals(player)) return;
                HandlerList.unregisterAll(this);
            }
        }, Main.get());
    }

    private void setupPoint(ConfiguredValue<Location> value) {
        Location point = player.getLocation();
        point.setX(BigDecimal.valueOf(point.getX()).setScale(1, RoundingMode.HALF_UP).doubleValue());
        point.setY(BigDecimal.valueOf(point.getY()).setScale(1, RoundingMode.HALF_UP).doubleValue());
        point.setZ(BigDecimal.valueOf(point.getZ()).setScale(1, RoundingMode.HALF_UP).doubleValue());
        point.setPitch(BigDecimal.valueOf(point.getPitch()).setScale(1, RoundingMode.HALF_UP).floatValue());
        point.setYaw(BigDecimal.valueOf(point.getYaw()).setScale(1, RoundingMode.HALF_UP).floatValue());
        value.set(point);
        XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
        player.sendMessage(ChatColor.GREEN + "Point successfully updated to " + point.toVector() + ".");
        checkComplete();
    }

    private void setupSign(ConfiguredValue<Vector> value) {
        value.set(null);
        Titles.sendTitle(
                this.player,
                0, 20, 5,
                "",
                ChatColor.GREEN + "Click a sign!"
        );
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInteract(PlayerInteractEvent event) {
                Block block = event.getClickedBlock();
                if (block == null) return;
                if (!(block.getState() instanceof Sign)) return;
                if (!event.getPlayer().equals(player)) return;
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) return;

                event.setCancelled(true);
                HandlerList.unregisterAll(this);
                Vector vector = block.getLocation().toVector();
                value.set(vector);
                XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
                player.sendMessage(ChatColor.GREEN + "Sign successfully updated to " + vector + ".");
                checkComplete();
            }

            @EventHandler
            public void onCancel(PlayerChangedWorldEvent event) {
                if (!event.getPlayer().equals(player)) return;
                HandlerList.unregisterAll(this);
            }

            @EventHandler
            public void onCancel(PlayerQuitEvent event) {
                if (!event.getPlayer().equals(player)) return;
                HandlerList.unregisterAll(this);
            }
        }, Main.get());
    }

    private void save() {
        if (this.isComplete()) {
            this.close();
            player.sendMessage(ChatColor.GREEN + "Save was successful! "
                    + "If the settings are not applied, please try restarting the server.");
            Firework firework = player.getWorld().spawn(player.getLocation().add(0, 3, 0), Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(
                    FireworkEffect.builder()
                            .with(FireworkEffect.Type.BALL_LARGE)
                            .withFlicker()
                            .withColor(Color.RED, Color.ORANGE)
                            .withFade(Color.GREEN, Color.AQUA)
                            .build()
            );
            meta.setPower(3);
            firework.setFireworkMeta(meta);
        } else {
            player.sendMessage(ChatColor.RED + "Please complete the setup first!");
            XSound.ENTITY_VILLAGER_NO.play(player);
        }
    }

    public boolean isComplete() {
        return config.MAP.AREA.optional().isPresent()
                && config.MAP.BUILD_AREA.optional().isPresent()
                && config.MAP.SPAWN.optional().isPresent()
                && config.SIGN.GROUND.optional().isPresent()
                && config.SIGN.RECORD.optional().isPresent()
                && config.SIGN.CLEAR.optional().isPresent()
                && config.SIGN.TOGGLE_ZONE.optional().isPresent()
                && config.SIGN.MODE.optional().isPresent()
                && config.SIGN.PRESET.optional().isPresent()
                && config.SIGN.START.optional().isPresent()
                && config.SIGN.PREVIEW.optional().isPresent();
    }

    private void checkComplete() {
        if (this.isComplete()) {
            player.spigot().sendMessage(
                    new ComponentBuilder("Well done, setup complete! ")
                            .color(ChatColor.GREEN.asBungee())
                            .append("[Click here]")
                            .color(ChatColor.YELLOW.asBungee())
                            .bold(true)
                            .event(new ClickEvent(
                                    ClickEvent.Action.SUGGEST_COMMAND,
                                    "/sbp setup " + name + " save"
                            ))
                            .event(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    TextComponent.fromLegacyText(ChatColor.GRAY + "Click to save!")
                            ))
                            .append(" to save!")
                            .color(ChatColor.GREEN.asBungee())
                            .create()
            );
        }
    }

    public void close() {
        SESSIONS.remove(this);
        if (!this.isComplete()) return;

        try {
            Area area = config.MAP.AREA.resolve().clone();
            Area buildArea = config.MAP.BUILD_AREA.resolve().clone();
            Location spawn = config.MAP.SPAWN.resolve().clone();
            SimpleZone zone = new SimpleZone(this.name);

            // Save schematic
            SchematicData schematic = SchematicData.copy(player.getWorld(), area);
            schematic.save(zone.getSchematicFile());

            // Copy config
            ZoneConfig zc = zone.getConfig();
            Vector reference = area.getMinimumPos();
            zc.MAP.HEIGHT.set(area.getYMin());
            zc.MAP.AREA.set(area.subtract(reference));
            zc.MAP.BUILD_AREA.set(buildArea.subtract(reference));
            zc.MAP.SPAWN.set(spawn.subtract(reference));
            zc.SIGN.GROUND.set(config.SIGN.GROUND.resolve().subtract(reference));
            zc.SIGN.RECORD.set(config.SIGN.RECORD.resolve().subtract(reference));
            zc.SIGN.CLEAR.set(config.SIGN.CLEAR.resolve().subtract(reference));
            zc.SIGN.TOGGLE_ZONE.set(config.SIGN.TOGGLE_ZONE.resolve().subtract(reference));
            zc.SIGN.MODE.set(config.SIGN.MODE.resolve().subtract(reference));
            zc.SIGN.PRESET.set(config.SIGN.PRESET.resolve().subtract(reference));
            zc.SIGN.START.set(config.SIGN.START.resolve().subtract(reference));
            zc.SIGN.PREVIEW.set(config.SIGN.PREVIEW.resolve().subtract(reference));
            zc.getHolder().save();

            Main.get().getZones().add(zone.load());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Getter
    @RequiredArgsConstructor
    private static class Entry<T> {
        private final String message;
        private final String description;
        private final Function<ZoneConfig, ConfiguredValue<T>> value;
        private final BiConsumer<SetupSession, ConfiguredValue<T>> action;
    }
}
