package com.meteor.SBPractice.Api;

import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Utils.NMSSupport;
import com.meteor.SBPractice.Utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SBPPlayer {
    private final Player player;
    private PlayerStats stats;

    private boolean enableHighjump = true;
    private long highjumpCooldown = 0L;
    private double highjumpIntensity = 1.20D;

    @Getter
    private static List<SBPPlayer> players = new ArrayList<>();

    public SBPPlayer(Player player) {
        this.player = player;
        this.stats = Main.getRemoteDatabase().getPlayerStats(player.getUniqueId());
        players.add(this);
    }

    public Location getLocation() {
        return player.getLocation();
    }

    public void teleport(Location location) {
        player.teleport(location);
    }

    public String getName() {
        return player.getName();
    }

    public void resetPlayer() {
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setExp(0.0F);
        player.setFireTicks(0);
        player.setFlying(false);
        player.setFoodLevel(20);
        player.setHealth(20.0D);
        player.setLevel(0);
        player.getInventory().setArmorContents(null);
        player.getInventory().clear();
        player.updateInventory();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        NMSSupport.showPlayer(player, true);
    }

    public void sendPlotItem() {
        ItemStack is = new ItemStack(Material.valueOf(Main.getPlugin().getConfig().getString("item.clear")));
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(Messages.ITEM_CLEAR.getMessage());
        is.setItemMeta(im);
        player.getInventory().setItem(8, is);
        is = new ItemStack(Material.valueOf(Main.getPlugin().getConfig().getString("item.prestart")));
        im = is.getItemMeta();
        im.setDisplayName(Messages.ITEM_PRESTART.getMessage());
        is.setItemMeta(im);
        player.getInventory().setItem(7, is);
        player.updateInventory();
    }

    public void sendMessage(String message) {
        player.sendMessage(message.replaceAll("&", "§").replace("§§", "&"));
    }

    public void playSound(Utils.Sounds sound) {
        Utils.playSound(player, sound, 1.0F, 1.0F);
    }

    public static @Nullable SBPPlayer getPlayer(Player player) {
        for (SBPPlayer p : players) {
            if (p.getPlayer().equals(player)) {
                return p;
            }
        } return null;
    }

    public static void removePlayer(SBPPlayer player) {
        Main.getRemoteDatabase().setPlayerStats(player.getStats());
        players.remove(player);
    }

    @Getter
    @Setter
    public static class PlayerStats {
        private UUID uuid;
        private int breakBlocks;
        private int placeBlocks;
        private int jumps;
        private int restores;
        private int onlineTimes;

        public PlayerStats(UUID uuid, int breakBlocks, int placeBlocks, int jumps, int restores, int onlineTimes) {
            this.uuid = uuid;
            this.breakBlocks = breakBlocks;
            this.placeBlocks = placeBlocks;
            this.jumps = jumps;
            this.restores = restores;
            this.onlineTimes = onlineTimes;
        }
    }
}
