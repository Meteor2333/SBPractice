package cc.meteormc.sbpractice.feature.session;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.value.ConfigValue;
import cc.carm.lib.configuration.value.standard.ConfiguredList;
import cc.carm.lib.configuration.value.standard.ConfiguredValue;
import cc.carm.lib.mineconfiguration.bukkit.MineConfiguration;
import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Zone;
import cc.meteormc.sbpractice.api.helper.Area;
import cc.meteormc.sbpractice.api.helper.ItemBuilder;
import cc.meteormc.sbpractice.api.storage.SchematicData;
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
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SetupSession extends BukkitRunnable {
    private final String name;
    private final Player player;
    private final ZoneConfig config;

    private static final Map<String, Entry<ZoneConfig, ConfigValue<?, ?>, ?>> ENTRIES;
    private static final Map<String, Entry<ZoneConfig.MAP, ConfiguredValue<?>, ?>> MAP_ENTRIES;
    private static final Map<String, Entry<ZoneConfig.SIGN, ConfiguredList<?>, ?>> SIGN_ENTRIES;

    static {
        ImmutableMap.Builder<String, Entry<ZoneConfig.MAP, ConfiguredValue<?>, ?>> map = ImmutableMap.builder();
        map.put("area", Entry.ofValue(
                ZoneConfig.MAP.class,
                "[Set Area]",
                "Set the main area of the zone.",
                config -> config.AREA,
                SetupSession::setupArea
        ));
        map.put("build-area", Entry.ofValue(
                ZoneConfig.MAP.class,
                "[Set Build Area]",
                "Set the build area of the zone.",
                config -> config.BUILD_AREA,
                SetupSession::setupArea
        ));
        map.put("spawn", Entry.ofValue(
                ZoneConfig.MAP.class,
                "[Set Spawn]",
                "Set the spawn at your current location of the zone.",
                config -> config.SPAWN,
                SetupSession::setupPoint
        ));
        MAP_ENTRIES = map.build();

        ImmutableMap.Builder<String, Entry<ZoneConfig.SIGN, ConfiguredList<?>, ?>> sign = ImmutableMap.builder();
        sign.put("clear-sign", Entry.ofList(
                ZoneConfig.SIGN.class,
                "[Add Clear Sign]",
                "Set the clear sign of the zone.",
                config -> config.CLEAR,
                SetupSession::setupSign
        ));
        sign.put("ground-sign", Entry.ofList(
                ZoneConfig.SIGN.class,
                "[Add Ground Sign]",
                "Set the ground sign of the zone.",
                config -> config.GROUND,
                SetupSession::setupSign
        ));
        sign.put("mode-sign", Entry.ofList(
                ZoneConfig.SIGN.class,
                "[Add Mode Sign]",
                "Set the mode sign of the zone.",
                config -> config.MODE,
                SetupSession::setupSign
        ));
        sign.put("preset-sign", Entry.ofList(
                ZoneConfig.SIGN.class,
                "[Add Preset Sign]",
                "Set the preset sign of the zone.",
                config -> config.PRESET,
                SetupSession::setupSign
        ));
        sign.put("preview-sign", Entry.ofList(
                ZoneConfig.SIGN.class,
                "[Add Preview Sign]",
                "Set the preview sign of the zone.",
                config -> config.PREVIEW,
                SetupSession::setupSign
        ));
        sign.put("record-sign", Entry.ofList(
                ZoneConfig.SIGN.class,
                "[Add Record Sign]",
                "Set the record sign of the zone.",
                config -> config.RECORD,
                SetupSession::setupSign
        ));
        sign.put("start-sign", Entry.ofList(
                ZoneConfig.SIGN.class,
                "[Add Start Sign]",
                "Set the start sign of the zone.",
                config -> config.START,
                SetupSession::setupSign
        ));
        sign.put("toggle-zone-sign", Entry.ofList(
                ZoneConfig.SIGN.class,
                "[Add Toggle Zone Sign]",
                "Set the toggle zone sign of the zone.",
                config -> config.TOGGLE_ZONE,
                SetupSession::setupSign
        ));
        SIGN_ENTRIES = sign.build();

        ImmutableMap.Builder<String, Entry<ZoneConfig, ConfigValue<?, ?>, ?>> all = ImmutableMap.builder();
        MAP_ENTRIES.forEach((name, entry) -> {
            all.put(name, copyAsParent(entry, config -> config.MAP));
        });
        SIGN_ENTRIES.forEach((name, entry) -> {
            all.put(name, copyAsParent(entry, config -> config.SIGN));
        });
        ENTRIES = all.build();
    }

    private static <C extends Configuration, T> Entry<ZoneConfig, ConfigValue<?, ?>, ?> copyAsParent(Entry<C, ? extends ConfigValue<?, ?>, ?> entry, Function<ZoneConfig, C> childConfig) {
        //noinspection unchecked,rawtypes
        return (Entry) new Entry<>(
                entry.getMessage(),
                entry.getDescription(),
                zc -> (ConfigValue<?, T>) entry.getValue().apply(childConfig.apply((ZoneConfig) zc)),
                (BiConsumer<SetupSession, ConfigValue<?, T>>) entry.getAction()
        );
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
        this.runTaskTimer(Main.get(), 0L, 5L);
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
        config.MAP.AREA.optional().ifPresent(area -> drawArea(area, Color.ORANGE));
        config.MAP.BUILD_AREA.optional().ifPresent(area -> drawArea(area, Color.BLUE));
        config.MAP.SPAWN.optional().ifPresent(point -> drawPoint(point.clone().add(0, 0.5, 0), Color.RED));
        config.SIGN.CLEAR.optional().ifPresent(sign -> drawSign(sign, "Clear Sign"));
        config.SIGN.GROUND.optional().ifPresent(sign -> drawSign(sign, "Ground Sign"));
        config.SIGN.MODE.optional().ifPresent(sign -> drawSign(sign, "Mode Sign"));
        config.SIGN.PRESET.optional().ifPresent(sign -> drawSign(sign, "Preset Sign"));
        config.SIGN.PREVIEW.optional().ifPresent(sign -> drawSign(sign, "Preview Sign"));
        config.SIGN.RECORD.optional().ifPresent(sign -> drawSign(sign, "Record Sign"));
        config.SIGN.START.optional().ifPresent(sign -> drawSign(sign, "Start Sign"));
        config.SIGN.TOGGLE_ZONE.optional().ifPresent(sign -> drawSign(sign, "Toggle Zone Sign"));
    }

    private void drawArea(Area area, Color color) {
        double xMin = area.getXMin();
        double xMax = area.getXMax() + 1.0;
        double yMin = area.getYMin();
        double yMax = area.getYMax() + 1.0;
        double zMin = area.getZMin();
        double zMax = area.getZMax() + 1.0;
        World world = player.getWorld();

        drawLine(new Location(world, xMin, yMin, zMin), new Location(world, xMax, yMin, zMin), color);
        drawLine(new Location(world, xMin, yMin, zMax), new Location(world, xMax, yMin, zMax), color);
        drawLine(new Location(world, xMin, yMax, zMin), new Location(world, xMax, yMax, zMin), color);
        drawLine(new Location(world, xMin, yMax, zMax), new Location(world, xMax, yMax, zMax), color);

        drawLine(new Location(world, xMin, yMin, zMin), new Location(world, xMin, yMax, zMin), color);
        drawLine(new Location(world, xMin, yMin, zMax), new Location(world, xMin, yMax, zMax), color);
        drawLine(new Location(world, xMax, yMin, zMin), new Location(world, xMax, yMax, zMin), color);
        drawLine(new Location(world, xMax, yMin, zMax), new Location(world, xMax, yMax, zMax), color);

        drawLine(new Location(world, xMin, yMin, zMin), new Location(world, xMin, yMin, zMax), color);
        drawLine(new Location(world, xMin, yMax, zMin), new Location(world, xMin, yMax, zMax), color);
        drawLine(new Location(world, xMax, yMin, zMin), new Location(world, xMax, yMin, zMax), color);
        drawLine(new Location(world, xMax, yMax, zMin), new Location(world, xMax, yMax, zMax), color);
    }

    private void drawLine(Location start, Location end, Color color) {
        Location delta = end.clone().subtract(start);
        double points = Math.max(1.0D, delta.length() / 0.5D);
        double xStep = delta.getX() / points;
        double yStep = delta.getY() / points;
        double zStep = delta.getZ() / points;

        Location mutable = start.clone();
        for (int i = 0; i <= points; i++) {
            mutable.setX(start.getX() + i * xStep);
            mutable.setY(start.getY() + i * yStep);
            mutable.setZ(start.getZ() + i * zStep);
            this.drawPoint(mutable, color);
        }
    }

    private void drawPoint(Location point, Color color) {
        ParticleType.of("REDSTONE").spawn(
                this.player,
                point.getX(),
                point.getY(),
                point.getZ(),
                0,
                color.getRed() / 255D,
                color.getGreen() / 255D,
                color.getBlue() / 255D,
                1, null, true
        );
    }

    private void drawSign(List<Vector> vectors, String message) {
        for (Vector vector : vectors) {
            BlockState state = vector.toLocation(player.getWorld()).getBlock().getState();
            if (state instanceof Sign) {
                Sign sign = (Sign) state;
                sign.setLine(1, message);
                sign.setLine(2, "Set");
                sign.update();
            }
        }
    }

    public void handleCommand(String args) {
        if ("save".equals(args)) {
            this.save();
            return;
        }

        String[] split = args.split(" ");
        Entry<ZoneConfig, ConfigValue<?, ?>, ?> entry = ENTRIES.get(split[0]);
        if (entry == null) {
            this.sendHelp();
            return;
        }

        XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
        entry.getAction().accept(this, entry.getValue().apply(this.config));
    }

    private void sendHelp() {
        player.sendMessage(ChatColor.GRAY + "---------------------------");
        player.sendMessage(ChatColor.AQUA + "Please click the entries listed below to setup!");
        for (Map.Entry<String, Entry<ZoneConfig, ConfigValue<?, ?>, ?>> e : ENTRIES.entrySet()) {
            Entry<ZoneConfig, ?, ?> entry = e.getValue();
            ConfigValue<?, ?> value = entry.getValue().apply(config);

            Collection<?> collection = null;
            boolean isPresent = value.optional().isPresent();
            if (value instanceof Collection) {
                collection = (Collection<?>) value.resolve();
                isPresent = !collection.isEmpty();
            }

            player.spigot().sendMessage(
                    new ComponentBuilder(" - ")
                            .color(ChatColor.DARK_GRAY.asBungee())
                            .append(entry.getMessage())
                            .color(isPresent ? ChatColor.GREEN.asBungee() : ChatColor.RED.asBungee())
                            .event(new ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/sbp setup " + name + " " + e.getKey()
                            ))
                            .event(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    TextComponent.fromLegacyText(ChatColor.GRAY + entry.getDescription())
                            ))
                            .append(collection != null ? " (" + collection.size() + " set)" : "")
                            .color(ChatColor.YELLOW.asBungee())
                            .create()
            );
        }
        player.sendMessage(ChatColor.GRAY + "---------------------------");
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
                0, 50, 10,
                "",
                ChatColor.GREEN + "Use the iron axe to select 2 points!"
        );
        Bukkit.getPluginManager().registerEvents(new Listener() {
            private Vector pos1, pos2;

            @EventHandler
            public void onInteract(PlayerInteractEvent event) {
                if (!event.getPlayer().equals(player)) return;
                ItemStack item = event.getItem();
                Block block = event.getClickedBlock();
                if (item == null) return;
                if (block == null) return;
                if (XMaterial.matchXMaterial(item) != XMaterial.IRON_AXE) return;

                event.setCancelled(true);
                Vector vector = block.getLocation().toVector();
                switch (event.getAction()) {
                    case LEFT_CLICK_BLOCK:
                        pos1 = vector;
                        player.sendMessage(ChatColor.GREEN + "Area position 1 set!");
                        break;
                    case RIGHT_CLICK_BLOCK:
                        pos2 = vector;
                        player.sendMessage(ChatColor.GREEN + "Area position 2 set!");
                        break;
                    default:
                        return;
                }

                XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
                if (pos1 != null && pos2 != null) {
                    item.setType(Material.AIR);
                    player.setItemInHand(null);
                    HandlerList.unregisterAll(this);
                    Area area = new Area(pos1, pos2);
                    value.set(area);
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

    private void setupSign(ConfiguredList<Vector> value) {
        PlayerInventory inventory = this.player.getInventory();
        inventory.setItem(
                inventory.getHeldItemSlot(),
                new ItemBuilder(XMaterial.OAK_SIGN)
                        .setDisplayName(ChatColor.AQUA + "Place me!")
                        .build()
        );
        Titles.sendTitle(
                this.player,
                0, 50, 10,
                "",
                ChatColor.GREEN + "Click a sign as the target location!"
        );
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onInteract(PlayerInteractEvent event) {
                Block block = event.getClickedBlock();
                if (block == null) return;
                if (!(block.getState() instanceof Sign)) return;
                if (!event.getPlayer().equals(player)) return;
                if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

                event.setCancelled(true);
                HandlerList.unregisterAll(this);
                Vector vector = block.getLocation().toVector();
                SIGN_ENTRIES.values().stream()
                        .map(Entry::getValue)
                        .map(v -> v.apply(config.SIGN))
                        .forEach(signs -> signs.removeIf(vec -> vec.equals(vector)));
                if (!value.optional().isPresent()) {
                    value.set(new ArrayList<>());
                }
                value.add(vector);
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
            new BukkitRunnable() {
                private final long startTime = System.currentTimeMillis();

                @Override
                public void run() {
                    if (System.currentTimeMillis() - startTime >= 5000L) return;
                    Firework firework = player.getWorld().spawn(player.getLocation().add(0, 3, 0), Firework.class);
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.addEffect(
                            FireworkEffect.builder()
                                    .with(FireworkEffect.Type.BALL_LARGE)
                                    .withFlicker()
                                    .withTrail()
                                    .withColor(randomColor())
                                    .withColor(randomColor())
                                    .withColor(randomColor())
                                    .withFade(randomColor())
                                    .withFade(randomColor())
                                    .withFade(randomColor())
                                    .build()
                    );
                    firework.setFireworkMeta(meta);
                }
            }.runTaskTimer(Main.get(), 0L, 20L);
        } else {
            player.sendMessage(ChatColor.RED + "Please complete the setup first!");
            XSound.ENTITY_VILLAGER_NO.play(player);
        }
    }

    private static Color randomColor() {
        Random random = new Random();
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        return Color.fromRGB(r, g, b);
    }

    public boolean isComplete() {
        boolean mapPresent = MAP_ENTRIES.values().stream()
                .map(Entry::getValue)
                .map(v -> v.apply(config.MAP))
                .map(ConfigValue::optional)
                .allMatch(Optional::isPresent);
        boolean signPresent = SIGN_ENTRIES.values().stream()
                .map(Entry::getValue)
                .map(v -> v.apply(config.SIGN))
                .noneMatch(ConfiguredList::isEmpty);
        return mapPresent && signPresent;
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
        } else {
            this.sendHelp();
        }
    }

    public void close() {
        SESSIONS.remove(this);
        if (!this.isComplete()) return;

        try {
            Area area = config.MAP.AREA.resolve();
            Area buildArea = config.MAP.BUILD_AREA.resolve();
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
            zc.SIGN.CLEAR.set(subtractAll(config.SIGN.CLEAR, reference));
            zc.SIGN.GROUND.set(subtractAll(config.SIGN.GROUND, reference));
            zc.SIGN.MODE.set(subtractAll(config.SIGN.MODE, reference));
            zc.SIGN.PRESET.set(subtractAll(config.SIGN.PRESET, reference));
            zc.SIGN.PREVIEW.set(subtractAll(config.SIGN.PREVIEW, reference));
            zc.SIGN.RECORD.set(subtractAll(config.SIGN.RECORD, reference));
            zc.SIGN.START.set(subtractAll(config.SIGN.START, reference));
            zc.SIGN.TOGGLE_ZONE.set(subtractAll(config.SIGN.TOGGLE_ZONE, reference));
            zc.getHolder().save();

            Main.get().getZones().add(zone.load());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            this.cancel();
        }
    }

    private static List<Vector> subtractAll(List<Vector> vectors, Vector subtract) {
        return vectors.stream().map(vec -> vec.subtract(subtract)).collect(Collectors.toList());
    }

    // This is a pile of shit code. Don't touch it!
    @Getter
    @RequiredArgsConstructor
    private static class Entry<C extends Configuration, V extends ConfigValue<?, T>, T> {
        private final String message;
        private final String description;
        private final Function<C, V> value;
        private final BiConsumer<SetupSession, V> action;

        public static <C extends Configuration, T> Entry<C, ConfiguredValue<?>, ?> ofValue(
                Class<C> configClass,
                String message,
                String description,
                Function<C, ConfiguredValue<T>> value,
                BiConsumer<SetupSession, ConfiguredValue<T>> action
        ) {
            //noinspection unchecked,rawtypes
            return (Entry) new Entry<>(message, description, value, action);
        }

        public static <C extends Configuration, T> Entry<C, ConfiguredList<?>, ?> ofList(
                Class<C> configClass,
                String message,
                String description,
                Function<C, ConfiguredList<T>> value,
                BiConsumer<SetupSession, ConfiguredList<T>> action
        ) {
            //noinspection unchecked,rawtypes
            return (Entry) new Entry<>(message, description, value, action);
        }
    }
}
