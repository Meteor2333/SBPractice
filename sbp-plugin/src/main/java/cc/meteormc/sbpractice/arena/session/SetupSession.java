package cc.meteormc.sbpractice.arena.session;

import cc.meteormc.sbpractice.SBPractice;
import cc.meteormc.sbpractice.api.config.paths.ArenaConfigPath;
import cc.meteormc.sbpractice.api.storage.schematic.Schematic;
import cc.meteormc.sbpractice.api.util.ItemBuilder;
import cc.meteormc.sbpractice.api.util.Region;
import cc.meteormc.sbpractice.api.util.Utils;
import cc.meteormc.sbpractice.arena.DefaultArena;
import cc.meteormc.sbpractice.arena.setup.SetupType;
import cc.meteormc.sbpractice.config.Messages;
import cc.meteormc.sbpractice.gui.SetupArenaGui;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import fr.mrmicky.fastparticles.ParticleType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class SetupSession extends BukkitRunnable implements Listener {
    private final String name;
    private final Player player;
    private final SetupArenaGui gui;

    private Location mapAreaPos1 = null, mapAreaPos2 = null;
    private Location mapBuildAreaPos1 = null, mapBuildAreaPos2 = null;
    private Location mapSpawnPoint = null;
    private Location groundSign = null;
    private Location recordSign = null;
    private Location clearSign = null;
    private Location selectArenaSign = null;
    private Location modeSign = null;
    private Location presetSign = null;
    private Location startSign = null;
    private Location previewSign = null;

    private static final List<SetupSession> SESSIONS = new ArrayList<>();

    public SetupSession(Player player, String name) {
        this.name = name;
        this.player = player;
        this.gui = new SetupArenaGui(this);
        SESSIONS.add(this);
        Bukkit.getPluginManager().registerEvents(this, SBPractice.getPlugin());
    }

    @Override
    public void run() {
        if (mapAreaPos1 != null || mapAreaPos2 != null) {
            this.drawRegion(mapAreaPos1, mapAreaPos2, Color.GREEN);
        }

        if (mapBuildAreaPos1 != null || mapBuildAreaPos2 != null) {
            this.drawRegion(mapBuildAreaPos1, mapBuildAreaPos2, Color.RED);
        }

        if (mapSpawnPoint != null) {
            this.drawPoint(mapSpawnPoint, Color.ORANGE);
        }
    }

    private void drawRegion(Location p1, Location p2, Color color) {
        if (p1 == null && p2 == null) return;
        if (p1 == null ^ p2 == null) {
            Location base = p1 != null ? p1 : p2;
            Location other = base.clone().add(1, 1, 1);
            drawRegion(base, other, color);
            return;
        }

        ParticleType particle = ParticleType.of("REDSTONE");

        Region region = new Region(p1.toVector(), p2.toVector());
        Vector[] corners = {
                new Vector(region.getXMin(), region.getYMin(), region.getZMin()),
                new Vector(region.getXMin(), region.getYMin(), region.getZMax()),
                new Vector(region.getXMin(), region.getYMax(), region.getZMin()),
                new Vector(region.getXMin(), region.getYMax(), region.getZMax()),
                new Vector(region.getXMax(), region.getYMin(), region.getZMin()),
                new Vector(region.getXMax(), region.getYMin(), region.getZMax()),
                new Vector(region.getXMax(), region.getYMax(), region.getZMin()),
                new Vector(region.getXMax(), region.getYMax(), region.getZMax())
        };

        for (int i = 0; i < 8; i++) {
            for (int j = i + 1; j < 8; j++) {
                if (Integer.bitCount(i ^ j) != 1) continue;
                Vector diff = corners[j].subtract(corners[i]);
                int points = (int) (diff.length() * 4);
                Vector step = diff.multiply(1.0 / points);
                Vector current = corners[i].clone();
                for (int k = 0; k <= points; k++) {
                    particle.spawn(
                            this.player,
                            current.getX(),
                            current.getY(),
                            current.getZ(),
                            1,
                            color.getRed() / 255D,
                            color.getGreen() / 255D,
                            color.getBlue() / 255D
                    );
                    current.add(step);
                }
            }
        }
    }

    private void drawPoint(Location loc, Color color) {
        ParticleType.of("REDSTONE").spawn(this.player, loc, 1, color.getRed() / 255D, color.getGreen() / 255D, color.getBlue() / 255D);
    }

    public void openGui() {
        this.gui.open(this.player);
    }

    public void onLeftClickGui(SetupType type) {
        switch (type) {
            case MAP_AREA:
                this.mapAreaPos1 = this.player.getLocation().getBlock().getLocation();
                break;
            case MAP_BUILD_AREA:
                this.startSetMapBuildArea();
                break;
            case MAP_SPAWN:
                this.mapSpawnPoint = this.player.getLocation();
                break;
            case MAP_SIGN:
                this.startSetMapSigns();
                break;
            default: return;
        }
        XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(this.player);
    }

    public void onRightClickGui(SetupType type) {
        switch (type) {
            case MAP_AREA:
                this.mapAreaPos2 = this.player.getLocation().getBlock().getLocation();
                break;
            case MAP_BUILD_AREA:
                this.startSetMapBuildArea();
                break;
            case MAP_SPAWN:
                this.mapSpawnPoint = this.player.getLocation();
                break;
            case MAP_SIGN:
                this.startSetMapSigns();
                break;
            default: return;
        }
        XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(this.player);
    }

    public boolean isComplete() {
        return this.mapAreaPos1 != null
                && this.mapAreaPos2 != null
                && this.mapBuildAreaPos1 != null
                && this.mapBuildAreaPos2 != null
                && this.mapSpawnPoint != null
                && this.groundSign != null
                && this.recordSign != null
                && this.clearSign != null
                && this.selectArenaSign != null
                && this.modeSign != null
                && this.presetSign != null
                && this.startSign != null
                && this.previewSign != null;
    }

    public void close() {
        SESSIONS.remove(this);
        if (this.isComplete()) {
            try {
                DefaultArena arena = new DefaultArena(this.name);
                Region region = new Region(this.mapAreaPos1.toVector(), this.mapAreaPos2.toVector());
                Vector reference = region.getMinimumPos();
                arena.setObject(ArenaConfigPath.HEIGHT, this.mapAreaPos1.getBlockY());
                arena.setLocation(ArenaConfigPath.MAP_AREA_POS1, this.mapAreaPos1.subtract(reference));
                arena.setLocation(ArenaConfigPath.MAP_AREA_POS2, this.mapAreaPos2.subtract(reference));
                arena.setLocation(ArenaConfigPath.MAP_BUILD_AREA_POS1, this.mapBuildAreaPos1.subtract(reference));
                arena.setLocation(ArenaConfigPath.MAP_BUILD_AREA_POS2, this.mapBuildAreaPos2.subtract(reference));
                arena.setLocation(ArenaConfigPath.MAP_SPAWN, this.mapSpawnPoint.subtract(reference));
                arena.setLocation(ArenaConfigPath.GROUND_SIGN, this.groundSign.subtract(reference));
                arena.setLocation(ArenaConfigPath.RECORD_SIGN, this.recordSign.subtract(reference));
                arena.setLocation(ArenaConfigPath.CLEAR_SIGN, this.clearSign.subtract(reference));
                arena.setLocation(ArenaConfigPath.SELECT_ARENA_SIGN, this.selectArenaSign.subtract(reference));
                arena.setLocation(ArenaConfigPath.MODE_SIGN, this.modeSign.subtract(reference));
                arena.setLocation(ArenaConfigPath.PRESET_SIGN, this.presetSign.subtract(reference));
                arena.setLocation(ArenaConfigPath.START_SIGN, this.startSign.subtract(reference));
                arena.setLocation(ArenaConfigPath.PREVIEW_SIGN, this.previewSign.subtract(reference));
                arena.save();

                Schematic.save(
                        this.mapSpawnPoint.getWorld(),
                        region.getMinimumPos(),
                        region,
                        arena.getSchematicFile()
                );
                arena.load();

                SBPractice.getArenas().add(arena);
                this.player.sendMessage(ChatColor.GREEN + "Save was successful! " +
                        "If the settings are not applied, please try restarting the server.");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void startSetMapBuildArea() {
        this.player.closeInventory();
        this.mapBuildAreaPos1 = this.mapBuildAreaPos2 = null;
        SBPractice.getNms().sendTitle(
                this.player,
                "",
                ChatColor.GREEN + "Use the iron axe to select 2 points!",
                0, 20, 5
        );
        PlayerInventory inventory = this.player.getInventory();
        inventory.setItem(inventory.getHeldItemSlot(), SBPractice.getNms()
                .setItemTag(new ItemBuilder(XMaterial.IRON_AXE)
                .setDisplayName(SetupType.MAP_BUILD_AREA.getHint())
                .build(), "sbpractice", "setup-build-area"));
    }

    private void startSetMapSigns() {
        this.player.closeInventory();
        this.groundSign = null;
        this.recordSign = null;
        this.clearSign = null;
        this.selectArenaSign = null;
        this.modeSign = null;
        this.presetSign = null;
        this.startSign = null;
        this.previewSign = null;
        SBPractice.getNms().sendTitle(
                this.player,
                "",
                ChatColor.GREEN + "Place the oak sign to set sign!",
                0, 20, 5
        );
        final SetupType.SetupSignType sign = SetupType.SetupSignType.GROUND;
        PlayerInventory inventory = this.player.getInventory();
        inventory.setItem(inventory.getHeldItemSlot(), SBPractice.getNms()
                .setItemTag(new ItemBuilder(XMaterial.OAK_SIGN)
                .setDisplayName(sign.getHint())
                .build(), "sbpractice", "setup-signs_" + sign.name()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!event.hasItem()) return;
        Optional<SetupSession> optionalSession = getSession(player);
        if (optionalSession.isPresent()) {
            SetupSession session = optionalSession.get();
            if (SBPractice.getNms().hasItemTag(event.getItem(), "sbpractice")) {
                if (SBPractice.getNms().getItemTag(event.getItem(), "sbpractice").equals("setup-build-area")) {
                    event.setCancelled(true);
                    if (session.mapBuildAreaPos1 != null && session.mapBuildAreaPos2 != null) {
                        player.setItemInHand(XMaterial.AIR.parseItem());
                        return;
                    }

                    switch (event.getAction()) {
                        case LEFT_CLICK_BLOCK:
                            session.mapBuildAreaPos1 = event.getClickedBlock().getLocation();
                            player.sendMessage(Messages.PREFIX.getMessage() + ChatColor.GREEN + "Build area position 1 set!");
                            break;
                        case RIGHT_CLICK_BLOCK:
                            session.mapBuildAreaPos2 = event.getClickedBlock().getLocation();
                            player.sendMessage(Messages.PREFIX.getMessage() + ChatColor.GREEN + "Build area position 2 set!");
                            break;
                        default:
                            return;
                    }
                    XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);

                    if (session.mapBuildAreaPos1 != null && session.mapBuildAreaPos2 != null) {
                        player.getInventory().setItemInHand(null);
                        player.sendMessage(Messages.PREFIX.getMessage() + ChatColor.GREEN + "The setup was successful!");
                        session.openGui();
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.canBuild()) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        if (item == null) return;
        Optional<SetupSession> optionalSession = getSession(player);
        if (optionalSession.isPresent()) {
            SetupSession session = optionalSession.get();
            Block block = event.getBlockPlaced();
            String tag = SBPractice.getNms().hasItemTag(item, "sbpractice") ? SBPractice.getNms().getItemTag(item, "sbpractice") : "";
            if (tag.startsWith("setup-signs")) {
                //event.setCancelled(true);
                if (!(block.getState() instanceof Sign)) return;

                SetupType.SetupSignType sign;
                try {
                    sign = SetupType.SetupSignType.valueOf(tag.substring(tag.indexOf('_') + 1));
                    Field field = super.getClass().getDeclaredField(sign.getField());
                    field.setAccessible(true);
                    field.set(session, block.getLocation());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                    return;
                } catch (IllegalArgumentException e) {
                    return;
                }

                player.sendMessage(Messages.PREFIX.getMessage() + ChatColor.GREEN + ChatColor.stripColor(Utils.colorize(sign.getHint())) + " was successful!");
                XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(player);
                if (sign.ordinal() + 1 >= SetupType.SetupSignType.values().length) {
                    player.getInventory().setItemInHand(null);
                    session.openGui();
                } else {
                    sign = SetupType.SetupSignType.values()[sign.ordinal() + 1];
                    player.setItemInHand(SBPractice.getNms()
                            .setItemTag(new ItemBuilder(item)
                            .setDisplayName(sign.getHint())
                            .build(), "sbpractice", "setup-signs_" + sign.name()));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        getSession(player).ifPresent(SetupSession::close);
    }

    public static @NotNull Optional<SetupSession> getSession(Player player) {
        return SESSIONS.stream()
                .filter(session -> session.getPlayer().getName().equals(player.getName()))
                .findFirst();
    }
}
