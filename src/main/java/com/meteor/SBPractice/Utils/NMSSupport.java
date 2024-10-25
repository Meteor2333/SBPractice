package com.meteor.SBPractice.Utils;

import com.meteor.SBPractice.Main;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

// 懒 没做跨版本
public class NMSSupport {
    public static String getServerVersion() {
        String sv = Bukkit.getServer().getClass().getPackage().getName();
        return sv.substring(sv.lastIndexOf(".") + 1);
    }

    public static void registerCommand(String name, Command command) {
        try {
            Class<?> clazz = Class.forName("org.bukkit.craftbukkit." + getServerVersion() + ".CraftServer");
            ((SimpleCommandMap) clazz.getMethod("getCommandMap").invoke(clazz.cast(Main.getPlugin().getServer()))).register(name, command);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public static ItemStack setTag(@NotNull ItemStack itemStack, String key, String value) {
        net.minecraft.server.v1_8_R3.ItemStack is = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = is.getTag();
        if (tag == null) {
            tag = new NBTTagCompound();
            is.setTag(tag);
        }

        tag.setString(key, value);
        return CraftItemStack.asBukkitCopy(is);
    }

    public static String getTag(@NotNull ItemStack itemStack, String key) {
        net.minecraft.server.v1_8_R3.ItemStack i = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = i.getTag();
        return tag == null ? "" : (tag.hasKey(key) ? tag.getString(key) : "");
    }

    public static void playAction(Player p, String text) {
        CraftPlayer cPlayer = (CraftPlayer) p;
        IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + text + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
        cPlayer.getHandle().playerConnection.sendPacket(ppoc);
    }

    public static void sendTitle(Player p, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (title != null) {
            if (!title.isEmpty()) {
                IChatBaseComponent bc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + title + "\"}");
                PacketPlayOutTitle tit = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, bc);
                PacketPlayOutTitle length = new PacketPlayOutTitle(fadeIn, stay, fadeOut);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(tit);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(length);
            }
        } if (subtitle != null) {
            IChatBaseComponent bc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitle + "\"}");
            PacketPlayOutTitle tit = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, bc);
            PacketPlayOutTitle length = new PacketPlayOutTitle(fadeIn, stay, fadeOut);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(tit);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(length);
        }
    }

    public static void hidePlayer(Player player, boolean antispam) {
        if (player.hasMetadata("hidden") && antispam) return;
        player.setMetadata("hidden", new FixedMetadataValue(Main.getPlugin(), true));
        player.spigot().setCollidesWithEntities(false);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));

        try {
            Field PIManager = Class.forName("net.minecraft.server." + getServerVersion() + ".PlayerInteractManager").getDeclaredField("gamemode");
            PIManager.setAccessible(true);

            PIManager.set(((CraftPlayer) player).getHandle().playerInteractManager, WorldSettings.EnumGamemode.SPECTATOR);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hidePlayer(player);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) player).getHandle()));
            }

            PIManager.set(((CraftPlayer) player).getHandle().playerInteractManager, WorldSettings.EnumGamemode.CREATIVE);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE, ((CraftPlayer) player).getHandle()));
        } catch (Exception ignored) { }
    }

    public static void showPlayer(Player player, boolean antispam) {
        if (!player.hasMetadata("hidden") && antispam) return;
        player.removeMetadata("hidden", Main.getPlugin());
        player.spigot().setCollidesWithEntities(true);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(player);
        } player.setGameMode(GameMode.CREATIVE);
        ((CraftPlayer) player).getHandle().playerInteractManager.setGameMode(WorldSettings.EnumGamemode.CREATIVE);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutGameStateChange(3, 1F));
        ((CraftPlayer) player).getHandle().server.getPlayerList().sendAll(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE, ((CraftPlayer) player).getHandle()), ((CraftPlayer) player).getHandle());
    }
}
