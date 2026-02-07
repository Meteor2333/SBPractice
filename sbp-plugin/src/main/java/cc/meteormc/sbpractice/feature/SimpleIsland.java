package cc.meteormc.sbpractice.feature;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.Zone;
import cc.meteormc.sbpractice.api.helper.Area;
import cc.meteormc.sbpractice.api.helper.ItemBuilder;
import cc.meteormc.sbpractice.api.helper.Operation;
import cc.meteormc.sbpractice.api.storage.data.BlockData;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.api.storage.data.PresetData;
import cc.meteormc.sbpractice.api.storage.data.SignData;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.config.Message;
import cc.meteormc.sbpractice.feature.operation.GroundOperation;
import cc.meteormc.sbpractice.feature.operation.RecordOperation;
import cc.meteormc.sbpractice.feature.task.Checker;
import cc.meteormc.sbpractice.feature.task.Timer;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class SimpleIsland extends Timer implements Island {
    @Setter
    private boolean isActive;
    @Setter
    private BuildMode mode = BuildMode.DEFAULT;
    @Setter
    private BukkitTask modeTask;
    private final Player owner;
    private final Zone zone;
    private final Area area, buildArea;
    private final Location spawnPoint;
    private final SignData signs;
    private final Checker checker = new Checker(this);
    private final List<Player> guests = new ArrayList<>();
    private final Map<Location, BlockData> recordedBlocks = new ConcurrentHashMap<>();
    private final BukkitTask actionbarTask = new BukkitRunnable() {
        @Override
        public void run() {
            for (Player player : getNearbyPlayers()) {
                Message.BASIC.TIME_FORMAT.sendActionBar(player, getFormattedTime());
            }
        }
    }.runTaskTimerAsynchronously(Main.get(), 0L, 0L);

    @Override
    public List<Player> getAllPlayers() {
        List<Player> player = new ArrayList<>();
        player.add(owner);
        player.addAll(guests);
        return player;
    }

    @Override
    public List<Player> getNearbyPlayers() {
        return zone.getWorld()
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
                new ItemBuilder(MainConfig.MATERIAL.START_ITEM.resolve())
                        .setDisplayName(Message.ITEM.START.parseLine(player))
                        .build()
        );
        player.getInventory().setItem(
                8,
                new ItemBuilder(MainConfig.MATERIAL.CLEAR_ITEM.resolve())
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
            if (island != null) island.removeAny(player, false);
            data.setIsland(SimpleIsland.this);
        });
        this.guests.add(player);
        this.resetPlayer(player);
    }

    @Override
    public void removeGuest(Player player) {
        this.guests.remove(player);
    }

    @Override
    public void removeAny(Player player, boolean createNew) {
        if (this.owner.equals(player)) {
            this.remove();
        } else {
            this.removeGuest(player);
            Message.MULTIPLAYER.LEAVE.PASSIVE.sendTo(this.owner, player.getName());
        }

        if (createNew) {
            this.zone.createIsland(player);
        }
    }

    @Override
    public void refreshSigns() {
        for (Map.Entry<SignData.Type, List<Location>> entry : this.signs.getSigns().entrySet()) {
            List<String> lines;
            switch (entry.getKey()) {
                case CLEAR:
                    lines = Message.SIGN.CLEAR.parse(owner);
                    break;
                case GROUND:
                    lines = Message.SIGN.GROUND.parse(owner);
                    break;
                case MODE:
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
                    lines = Message.SIGN.MODE.parse(owner, option);
                    break;
                case PRESET:
                    lines = Message.SIGN.PRESET.parse(owner);
                    break;
                case PREVIEW:
                    lines = Message.SIGN.PREVIEW.parse(owner);
                    break;
                case RECORD:
                    lines = Message.SIGN.RECORD.parse(owner);
                    break;
                case START:
                    lines = Message.SIGN.START.parse(owner);
                    break;
                case TOGGLE_ZONE:
                    lines = Message.SIGN.TOGGLE_ZONE.parse(owner);
                    break;
                default:
                    continue;
            }

            for (Location location : entry.getValue()) {
                BlockState state = location.getBlock().getState();
                if (!(state instanceof Sign)) return;
                Sign sign = (Sign) state;
                System.arraycopy(lines.toArray(new String[0]), 0, sign.getLines(), 0, sign.getLines().length);
                sign.update();
            }
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
            if (type != MainConfig.MATERIAL.START_ITEM.resolve() && type != MainConfig.MATERIAL.CLEAR_ITEM.resolve()) {
                inventory.clear(i);
            }
        }

        boolean isFull = false;
        this.recordedBlocks.clear();
        Iterator<BlockData> iterator = preset.getBlocks().iterator();
        for (Vector point : this.buildArea.getPoints()) {
            BlockData block = iterator.next();
            Location loc = point.toLocation(this.spawnPoint.getWorld());
            Main.get().getNms().setBlock(loc, block);
            if (!isFull) {
                ItemStack item = Main.get().getNms().getItemByBlock(
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
        Iterator<Player> iterator = this.guests.iterator();
        while (iterator.hasNext()) {
            Player guest = iterator.next();
            iterator.remove();
            this.removeAny(guest, false);
        }

        this.checker.shutdown();
        this.actionbarTask.cancel();
        this.zone.removeIsland(this);
    }
}
