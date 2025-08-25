package cc.meteormc.sbpractice;

import cc.carm.lib.easyplugin.utils.ColorParser;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.BuildMode;
import cc.meteormc.sbpractice.api.arena.operation.Operation;
import cc.meteormc.sbpractice.api.storage.data.BlockData;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.api.storage.data.PresetData;
import cc.meteormc.sbpractice.api.storage.data.SignData;
import cc.meteormc.sbpractice.api.util.ItemBuilder;
import cc.meteormc.sbpractice.api.util.Region;
import cc.meteormc.sbpractice.arena.DefaultArena;
import cc.meteormc.sbpractice.arena.operation.ClearOperation;
import cc.meteormc.sbpractice.arena.operation.GroundOperation;
import cc.meteormc.sbpractice.arena.operation.RecordOperation;
import cc.meteormc.sbpractice.arena.task.Timer;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.config.Message;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class DefaultIsland extends Timer implements Island {
    @Setter
    private boolean isActive;
    @Setter
    private BuildMode mode = BuildMode.DEFAULT;
    @Setter
    private BukkitTask modeTask;
    private final Player owner;
    private final DefaultArena arena;
    private final Region area, buildArea;
    private final Location spawnPoint;
    private final SignData signs;
    private final List<Player> guests = new ArrayList<>();
    private final Map<Location, BlockData> recordedBlocks = new HashMap<>();
    private final BukkitTask actionTask = new BukkitRunnable() {
        @Override
        public void run() {
            for (Player player : getNearbyPlayers()) {
                Message.ACTIONBAR.TIME.sendActionBar(player, getFormattedTime());
            }
        }
    }.runTaskTimerAsynchronously(Main.getPlugin(), 0L, 0L);

    @Override
    public List<Player> getNearbyPlayers() {
        return arena.getWorld()
                .getPlayers()
                .stream()
                .filter(player -> area.isInsideIgnoreYaxis(player.getLocation()))
                .collect(Collectors.toList());
    }

    @Override
    public void resetPlayer(Player player) {
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setExp(0F);
        player.setFireTicks(0);
        player.setFlying(false);
        player.setFoodLevel(20);
        player.setHealth(20D);
        player.setLevel(0);
        player.getInventory().clear();
        player.getInventory().setItem(
                7,
                new ItemBuilder(MainConfig.MATERIAL.START.resolve())
                        .setDisplayName(Message.ITEM.START.parseLine(player))
                        .build()
        );
        player.getInventory().setItem(
                8,
                new ItemBuilder(MainConfig.MATERIAL.CLEAR.resolve())
                        .setDisplayName(Message.ITEM.CLEAR.parseLine(player))
                        .build()
        );
        player.updateInventory();
        player.getActivePotionEffects().stream()
                .map(PotionEffect::getType)
                .forEach(player::removePotionEffect);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0, false, false));
        player.teleport(this.spawnPoint);
    }

    @Override
    public void addGuest(Player player) {
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (island.getOwner().equals(player)) island.remove();
            else {
                island.removeGuest(player);
                Message.MULTIPLAYER.LEAVE.PASSIVE.sendTo(island.getOwner(), player.getName());
            }
            data.setIsland(DefaultIsland.this);
        });

        this.guests.add(player);
        this.resetPlayer(player);
    }

    @Override
    public void removeGuest(Player player) {
        this.guests.remove(player);
    }

    @Override
    public void refreshSigns() {
        if (this.signs.getGround().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getGround().getBlock().getState();
            List<String> line = Message.SIGN.GROUND.parse(owner);
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, ColorParser.parse(line.get(i)));
            }
            sign.update();
        }
        if (this.signs.getRecord().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getRecord().getBlock().getState();
            List<String> line = Message.SIGN.RECORD.parse(owner);
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, ColorParser.parse(line.get(i)));
            }
            sign.update();
        }
        if (this.signs.getClear().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getClear().getBlock().getState();
            List<String> line = Message.SIGN.CLEAR.parse(owner);
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, ColorParser.parse(line.get(i)));
            }
            sign.update();
        }
        if (this.signs.getSelectArena().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getSelectArena().getBlock().getState();
            List<String> line = Message.SIGN.SELECT_ARENA.parse(owner);
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, ColorParser.parse(line.get(i)));
            }
            sign.update();
        }
        if (this.signs.getMode().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getMode().getBlock().getState();
            String option;
            switch (this.mode) {
                case ONCE:
                    option = Message.SIGN.OPTIONS.MODE.ONCE.parseLine(owner);
                    break;
                case CONTINUOUS:
                    option = Message.SIGN.OPTIONS.MODE.CONTINUOUS.parseLine(owner);
                    break;
                default:
                    option = Message.SIGN.OPTIONS.MODE.DEFAULT.parseLine(owner);
                    break;
            }
            List<String> line = Message.SIGN.MODE.parse(owner, option);
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, ColorParser.parse(line.get(i)));
            }
            sign.update();
        }
        if (this.signs.getPreset().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getPreset().getBlock().getState();
            List<String> line = Message.SIGN.PRESET.parse(owner);
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, ColorParser.parse(line.get(i)));
            }
            sign.update();
        }
        if (this.signs.getStart().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getStart().getBlock().getState();
            List<String> line = Message.SIGN.START.parse(owner);
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, ColorParser.parse(line.get(i)));
            }
            sign.update();
        }
        if (this.signs.getPreview().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getPreview().getBlock().getState();
            List<String> line = Message.SIGN.PREVIEW.parse(owner);
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, ColorParser.parse(line.get(i)));
            }
            sign.update();
        }
    }

    @Override
    public boolean executeOperation(Operation... operations) {
        boolean result = true;
        for (Operation operation : operations) {
            try {
                result &= operation.execute(this);
            } catch (Throwable e) {
                e.printStackTrace();
                result = false;
            }
        }
        return result;
    }

    @Override
    public void applyPreset(PresetData preset) {
        this.stopTimer();
        Inventory inventory = this.owner.getInventory();
        for (int i = 0; i < 8; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) continue;

            XMaterial type = XMaterial.matchXMaterial(item.getType());
            if (type == XMaterial.AIR) continue;
            if (type != MainConfig.MATERIAL.START.resolve() && type != MainConfig.MATERIAL.CLEAR.resolve()) {
                inventory.clear(i);
            }
        }

        boolean isFull = false;
        this.recordedBlocks.clear();
        Iterator<BlockData> iterator = preset.getBlocks().iterator();
        for (Vector vector : this.buildArea.getVectors()) {
            BlockData block = iterator.next();
            Location loc = vector.toLocation(this.spawnPoint.getWorld());
            Main.getNms().setBlock(loc, block);
            if (!isFull) {
                ItemStack item = Main.getNms().getItemByBlock(
                        block.getType(),
                        block.getData().getData()
                );
                if (!inventory.addItem(item).isEmpty()) {
                    isFull = true;
                    Message.BASIC.INVENTORY_FULL.sendTo(owner);
                }
            }
        }

        this.executeOperation(new RecordOperation(), new GroundOperation());
    }

    @Override
    public void remove() {
        for (Player guest : this.guests) {
            this.arena.createIsland(guest);
        }

        this.executeOperation(new ClearOperation());
        this.actionTask.cancel();
        this.arena.removeIsland(this);
    }
}
