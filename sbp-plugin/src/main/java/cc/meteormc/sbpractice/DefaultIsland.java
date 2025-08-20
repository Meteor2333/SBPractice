package cc.meteormc.sbpractice;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.arena.BuildMode;
import cc.meteormc.sbpractice.api.storage.player.PlayerData;
import cc.meteormc.sbpractice.api.storage.preset.PresetData;
import cc.meteormc.sbpractice.api.storage.sign.SignGroup;
import cc.meteormc.sbpractice.api.util.ItemBuilder;
import cc.meteormc.sbpractice.api.util.Region;
import cc.meteormc.sbpractice.api.util.Utils;
import cc.meteormc.sbpractice.arena.DefaultArena;
import cc.meteormc.sbpractice.arena.task.Timer;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.config.Messages;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class DefaultIsland extends Timer implements Island {
    @Setter
    private boolean startCountdown;
    @Setter
    private BuildMode mode = BuildMode.DEFAULT;
    private BukkitTask countdownTask;
    private final Player owner;
    private final DefaultArena arena;
    private final Region area, buildArea;
    private final Location spawnPoint;
    private final SignGroup signs;
    private final List<Player> guests = new ArrayList<>();
    private final Map<Location, BlockState> recordedBlocks = new HashMap<>();
    private final BukkitTask actionTask = new BukkitRunnable() {
        @Override
        public void run() {
            for (Player player : spawnPoint.getWorld().getPlayers()) {
                if (area.isInsideIgnoreYaxis(player.getLocation())) {
                    SBPractice.getNms().sendActionBar(
                            player,
                            Messages.CURRENT_TIME.getMessage().replace("%time%", getFormattedTime())
                    );
                }
            }
        }
    }.runTaskTimerAsynchronously(SBPractice.getPlugin(), 0L, 0L);


    @Override
    public void addGuest(Player player) {
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            if (island.getOwner().equals(player)) island.remove();
            else {
                island.removeGuest(player);
                island.getOwner().sendMessage(Messages.PREFIX.getMessage() + Messages.LEAVE_PASSIVE.getMessage().replace("%player%", player.getName()));
            }
            data.setIsland(DefaultIsland.this);
        });

        this.guests.add(player);
        Utils.resetPlayer(player);
        player.getInventory().setItem(7, new ItemBuilder(MainConfig.START_ITEM.getMaterial()
                .orElse(XMaterial.AIR))
                .setDisplayName(Messages.START_ITEM_NAME.getMessage())
                .build());
        player.getInventory().setItem(8, new ItemBuilder(MainConfig.CLEAR_ITEM.getMaterial()
                .orElse(XMaterial.AIR))
                .setDisplayName(Messages.CLEAR_ITEM_NAME.getMessage())
                .build());
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0, false, false));
        player.teleport(this.spawnPoint);
    }

    @Override
    public void removeGuest(Player player) {
        this.guests.remove(player);
    }

    @Override
    public void refreshSigns() {
        if (this.signs.getGround().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getGround().getBlock().getState();
            List<String> line = Messages.SIGN_GROUND.getMessageList();
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, Utils.colorize(Utils.colorize(line.get(i))));
            }
            sign.update();
        }
        if (this.signs.getRecord().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getRecord().getBlock().getState();
            List<String> line = Messages.SIGN_RECORD.getMessageList();
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, Utils.colorize(line.get(i)));
            }
            sign.update();
        }
        if (this.signs.getClear().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getClear().getBlock().getState();
            List<String> line = Messages.SIGN_CLEAR.getMessageList();
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, Utils.colorize(line.get(i)));
            }
            sign.update();
        }
        if (this.signs.getArena().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getArena().getBlock().getState();
            List<String> line = Messages.SIGN_SELECT_ARENA.getMessageList();
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, Utils.colorize(line.get(i)));
            }
            sign.update();
        }
        if (this.signs.getMode().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getMode().getBlock().getState();
            List<String> line = Messages.SIGN_MODE.getMessageList();
            String option;
            switch (this.mode) {
                case ONCE:
                    option = Messages.SIGN_OPTIONS_MODE_ONCE.getMessage();
                    break;
                case CONTINUOUS:
                    option = Messages.SIGN_OPTIONS_MODE_CONTINUOUS.getMessage();
                    break;
                default:
                    option = Messages.SIGN_OPTIONS_MODE_DEFAULT.getMessage();
                    break;
            }
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, Utils.colorize(line.get(i).replace("%build_mode%", option)));
            }
            sign.update();
        }
        if (this.signs.getPreset().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getPreset().getBlock().getState();
            List<String> line = Messages.SIGN_PRESET.getMessageList();
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, Utils.colorize(line.get(i)));
            }
            sign.update();
        }
        if (this.signs.getStart().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getStart().getBlock().getState();
            List<String> line = Messages.SIGN_START.getMessageList();
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, Utils.colorize(line.get(i)));
            }
            sign.update();
        }
        if (this.signs.getPreview().getBlock().getState() instanceof Sign) {
            Sign sign = (Sign) this.signs.getPreview().getBlock().getState();
            List<String> line = Messages.SIGN_PREVIEW.getMessageList();
            for (int i = 0; i <= 3; i++) {
                sign.setLine(i, Utils.colorize(line.get(i)));
            }
            sign.update();
        }
    }

    @Override
    public void ground() {
        for (int x = this.buildArea.getMinimumPos().getBlockX(); x <= this.buildArea.getMaximumPos().getBlockX(); x++) {
            for (int z = this.buildArea.getMinimumPos().getBlockZ(); z <= this.buildArea.getMaximumPos().getBlockZ(); z++) {
                Block block = this.arena.getWorld().getBlockAt(x, this.buildArea.getYMin(), z);
                XMaterial type = block.getType() != Material.AIR ? XMaterial.matchXMaterial(block.getType()) : MainConfig.DEFAULT_GROUND_BLOCK.getMaterial().orElse(XMaterial.GRASS_BLOCK);
                byte data = block.getType() != Material.AIR ? block.getData() : 0;
                SBPractice.getNms().setBlock(block.getRelative(BlockFace.DOWN), type, data);
            }
        }
    }

    @Override
    public void record() {
        super.stopTimer();
        super.setCanStart(false);
        this.recordedBlocks.clear();
        for (Vector vector : this.buildArea.getVectors()) {
            Location loc = vector.toLocation(this.arena.getWorld());
            this.recordedBlocks.put(loc, SBPractice.getNms().getBlockState(loc));
        }
    }

    @Override
    public void clear() {
        super.stopTimer();
        super.setCanStart(true);
        for (Entity entity : getSpawnPoint().getWorld().getEntities()) {
            if (!this.buildArea.clone().outset(1).isInside(entity.getLocation())) continue;
            if (entity.getType().equals(EntityType.PLAYER)) continue;
            entity.remove();
        }
        this.buildArea.fillBlock(this.arena.getWorld(), XMaterial.AIR);
    }

    @Override
    public void preview() {
        super.stopTimer();
        super.setCanStart(false);
        for (BlockState block : this.recordedBlocks.values()) {
            block.update(true, false);
        }
    }

    @Override
    public void applyPreset(PresetData preset) {
        super.stopTimer();
        Inventory inventory = this.owner.getInventory();
        for (int i = 0; i < 8; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) continue;

            Material type = item.getType();
            if (type == Material.AIR) continue;
            if (type != Material.SNOW && type != Material.EGG) {
                inventory.clear(i);
            }
        }

        int flag = 0;
        boolean isFull = false;
        this.recordedBlocks.clear();
        for (Vector vector : this.buildArea.getVectors()) {
            BlockState state = preset.getBlocks().get(flag++);
            Location loc = vector.toLocation(this.spawnPoint.getWorld());
            this.recordedBlocks.put(loc, SBPractice.getNms().setBlockStateLocation(state, loc));
            if (!isFull) {
                ItemStack item = SBPractice.getNms().getItemByBlock(state.getType(), state.getRawData());
                if (!inventory.addItem(item).isEmpty()) {
                    isFull = true;
                    this.owner.sendMessage(Messages.PREFIX.getMessage() + Messages.INVENTORY_FULL.getMessage());
                }
            }
        }

        this.preview();
        this.ground();
    }

    @Override
    public void start() {
        if (this.countdownTask != null) {
            if (!this.startCountdown && this.mode == BuildMode.CONTINUOUS) {
                this.countdownTask.cancel();
            }
            return;
        }

        this.preview();
        this.countdownTask = new BukkitRunnable() {
            private int times = 3;

            @Override
            public void run() {
                if (this.times-- <= 0) {
                    this.cancel();
                    for (Player player : spawnPoint.getWorld().getPlayers()) {
                        if (area.isInsideIgnoreYaxis(player.getLocation())) {
                            SBPractice.getNms().sendTitle(
                                    player,
                                    "",
                                    Messages.START_COUNTDOWN.getMessage(),
                                    0, 10, 10
                            );

                            new BukkitRunnable() {
                                int remaining = 3;
                                public void run() {
                                    XSound.BLOCK_NOTE_BLOCK_PLING.play(player);
                                    if (--this.remaining <= 0) {
                                        this.cancel();
                                        countdownTask = null;
                                    }
                                }
                            }.runTaskTimer(SBPractice.getPlugin(), 0L, 2L);
                        }
                    }
                    clear();
                    startTimer();
                } else {
                    for (Player player : spawnPoint.getWorld().getPlayers()) {
                        if (area.isInsideIgnoreYaxis(player.getLocation())) {
                            XSound.BLOCK_NOTE_BLOCK_HAT.play(player, 1F, 1.75F - (float) this.times / 8);
                            SBPractice.getNms().sendTitle(
                                    player,
                                    "",
                                    Messages.START_COUNTDOWN_NUMBER.getMessage().replace(
                                            "%number%",
                                            String.valueOf(this.times + 1)
                                    ),
                                    0, 20, 0
                            );
                        }
                    }
                }
            }
        }.runTaskTimer(SBPractice.getPlugin(), 0L, 15L);
    }

    @Override
    public void remove() {
        //if (this.owner.isOnline()) this.owner.kickPlayer("The current island is unloaded!");
        for (Player guest : this.guests) {
            this.arena.createIsland(guest);
        }
        this.actionTask.cancel();
        this.arena.removeIsland(this);
    }
}
