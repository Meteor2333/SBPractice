package com.meteor.SBPractice.Utils;

import com.meteor.SBPractice.Main;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("CallToPrintStackTrace")
@RequiredArgsConstructor
public enum VersionSupport {

    SOUND_ORB_PICKUP("ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP", "ENTITY_EXPERIENCE_ORB_PICKUP"),
    SOUND_NOTE_PLING("NOTE_PLING", "BLOCK_NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING"),
    SOUND_NOTE_STICKS("NOTE_STICKS", "BLOCK_NOTE_HAT", "BLOCK_NOTE_BLOCK_HAT"),
    SOUND_LEVEL_UP("LEVEL_UP", "ENTITY_PLAYER_LEVELUP", "ENTITY_PLAYER_LEVELUP"),
    SOUND_BLAZE_SHOOT("GHAST_FIREBALL", "ENTITY_BLAZE_SHOOT", "ENTITY_BLAZE_SHOOT");

    private final String v8, v12, v13;

    public String getForCurrentVersionSupport() {
        int version = Integer.parseInt(getServerVersion().split("_")[1]);
        if (version <= 8) return v8;
        else if (version <= 12) return v12;
        else return v13;
    }

    public static String getServerVersion() {
        return Bukkit.getServer().getClass().getName().split("\\.")[3];
    }

    public static Class<?> getOBCClass(String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + getServerVersion() + "." + className);
    }

    public static Class<?> getNMSClass(String className) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + getServerVersion() + "." + className);
    }

    public static void sendPacket(Player player, Object packet) {
        try {
            Class<?> craftPlayerClass = getOBCClass("entity.CraftPlayer");
            Object craftPlayerHandle = craftPlayerClass.getDeclaredMethod("getHandle").invoke(craftPlayerClass.cast(player));
            Object playerConnection = craftPlayerHandle.getClass().getDeclaredField("playerConnection").get(craftPlayerHandle);
            playerConnection.getClass().getDeclaredMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
        } catch (Exception ignored) { }
    }

    public static void registerCommand(String name, Command command) {
        try {
            Class<?> craftServerClass = getOBCClass("CraftServer");
            ((SimpleCommandMap) craftServerClass.getMethod("getCommandMap").invoke(craftServerClass.cast(Main.getPlugin().getServer()))).register(name, command);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static @NotNull ItemStack setTag(@NotNull ItemStack itemStack, String key, String value) {
        try {
            Class<?> itemStackClass = getNMSClass("ItemStack");
            Class<?> craftItemStackClass = getOBCClass("inventory.CraftItemStack");
            Class<?> nbtTagCompoundClass = getNMSClass("NBTTagCompound");
            Object nmsItemStack = craftItemStackClass.getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
            Object nbtTagCompound = itemStackClass.getDeclaredMethod("getTag").invoke(nmsItemStack);
            if (nbtTagCompound == null) {
                nbtTagCompound = nbtTagCompoundClass.getConstructor().newInstance();
                itemStackClass.getDeclaredMethod("setTag", nbtTagCompoundClass).invoke(nmsItemStack, nbtTagCompound);
            } nbtTagCompoundClass.getDeclaredMethod("setString", String.class, String.class).invoke(nbtTagCompound, key, value);
            return (ItemStack) craftItemStackClass.getDeclaredMethod("asBukkitCopy", itemStackClass).invoke(null, nmsItemStack);
        } catch (Exception e) {
            e.printStackTrace();
        } return itemStack;
    }

    public static @NotNull String getTag(@NotNull ItemStack itemStack, String key) {
        try {
            Class<?> nbtTagCompoundClass = getNMSClass("NBTTagCompound");
            Object nmsItemStack = getOBCClass("inventory.CraftItemStack").getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, itemStack);
            Object nbtTagCompound = getNMSClass("ItemStack").getDeclaredMethod("getTag").invoke(nmsItemStack);
            if (nbtTagCompound != null) {
                if ((boolean) nbtTagCompoundClass.getDeclaredMethod("hasKey", String.class).invoke(nbtTagCompound, key)) {
                    return (String) nbtTagCompoundClass.getDeclaredMethod("getString", String.class).invoke(nbtTagCompound, key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } return "";
    }

    public static void playAction(Player player, String message) {
        try {
            //noinspection JavaReflectionMemberAccess
            Player.Spigot.class.getDeclaredMethod("sendMessage", ChatMessageType.class, BaseComponent.class).invoke(player.spigot(), ChatMessageType.ACTION_BAR, new TextComponent(message));
        } catch (Exception e) {
            if (e instanceof NoSuchMethodException) {
                try {
                    Class<?> packetPlayOutChatClass = getNMSClass("PacketPlayOutChat");
                    Class<?> chatComponentTextClass = getNMSClass("ChatComponentText");
                    Class<?> iChatBaseComponentClass = getNMSClass("IChatBaseComponent");
                    try {
                        Class<?> chatMessageTypeClass = getNMSClass("ChatMessageType");
                        sendPacket(player, packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, chatMessageTypeClass}).newInstance(chatComponentTextClass.getConstructor(new Class<?>[]{String.class}).newInstance(message), Enum.valueOf(chatMessageTypeClass.asSubclass(Enum.class), "GAME_INFO")));
                    } catch (Exception ex) {
                        if (ex instanceof ClassNotFoundException) {
                            try {
                                sendPacket(player, packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(chatComponentTextClass.getConstructor(new Class<?>[]{String.class}).newInstance(message), (byte) 2));
                            } catch (Exception ignored) { }
                        } else ex.printStackTrace();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else e.printStackTrace();
        }
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        try {
            //noinspection JavaReflectionMemberAccess
            Player.class.getDeclaredMethod("sendTitle", String.class, String.class, int.class, int.class, int.class).invoke(player, title, subtitle, fadeIn, stay, fadeOut);
        } catch (Exception e) {
            if (e instanceof NoSuchMethodException) {
                try {
                    Class<?> packetPlayOutTitleClass = getNMSClass("PacketPlayOutTitle");
                    Class<?> chatComponentTextClass = getNMSClass("ChatComponentText");
                    Class<?> enumTitleActionClass = getNMSClass("PacketPlayOutTitle$EnumTitleAction");
                    Class<?> iChatBaseComponentClass = getNMSClass("IChatBaseComponent");
                    sendPacket(player, packetPlayOutTitleClass.getConstructor(new Class<?>[]{int.class, int.class, int.class}).newInstance(fadeIn, stay, fadeOut));
                    if (title != null) sendPacket(player, packetPlayOutTitleClass.getConstructor(new Class<?>[]{enumTitleActionClass, iChatBaseComponentClass}).newInstance(Enum.valueOf(enumTitleActionClass.asSubclass(Enum.class), "TITLE"), chatComponentTextClass.getConstructor(new Class<?>[]{String.class}).newInstance(title)));
                    if (subtitle != null) sendPacket(player, packetPlayOutTitleClass.getConstructor(new Class<?>[]{enumTitleActionClass, iChatBaseComponentClass}).newInstance(Enum.valueOf(enumTitleActionClass.asSubclass(Enum.class), "SUBTITLE"), chatComponentTextClass.getConstructor(new Class<?>[]{String.class}).newInstance(subtitle)));
                } catch (Exception ignored) { }
            } else e.printStackTrace();
        }
    }

    public static void hidePlayer(Player player) {
        try {
            player.spigot().setCollidesWithEntities(false);
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
            Class<?> craftPlayerClass = getOBCClass("entity.CraftPlayer");
            Class<?> packetPlayOutPlayerInfoClass = getNMSClass("PacketPlayOutPlayerInfo");
            Class<?> enumGamemodeClass = getNMSClass("WorldSettings$EnumGamemode");
            Class<?> enumPlayerInfoActionClass = getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
            Object craftPlayerHandle = craftPlayerClass.getDeclaredMethod("getHandle").invoke(craftPlayerClass.cast(player));
            Object playerInteractManager = craftPlayerHandle.getClass().getDeclaredField("playerInteractManager").get(craftPlayerHandle);
            Object entityPlayer = Array.newInstance(craftPlayerHandle.getClass(), 1);
            Field gamemode = playerInteractManager.getClass().getDeclaredField("gamemode");
            Array.set(entityPlayer, 0, craftPlayerHandle);
            gamemode.setAccessible(true);
            gamemode.set(playerInteractManager, Enum.valueOf(enumGamemodeClass.asSubclass(Enum.class), "SPECTATOR"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.hidePlayer(player);
                sendPacket(p, packetPlayOutPlayerInfoClass.getConstructor(new Class<?>[]{enumPlayerInfoActionClass, Array.newInstance(craftPlayerHandle.getClass(), 0).getClass()}).newInstance(Enum.valueOf(enumPlayerInfoActionClass.asSubclass(Enum.class), "ADD_PLAYER"), (Object[]) entityPlayer));
            } gamemode.set(playerInteractManager, Enum.valueOf(enumGamemodeClass.asSubclass(Enum.class), "CREATIVE"));
            sendPacket(player, packetPlayOutPlayerInfoClass.getConstructor(new Class<?>[]{enumPlayerInfoActionClass, Array.newInstance(craftPlayerHandle.getClass(), 0).getClass()}).newInstance(Enum.valueOf(enumPlayerInfoActionClass.asSubclass(Enum.class), "UPDATE_GAME_MODE"), (Object[]) entityPlayer));
        } catch (Exception ignored) { }
    }

    public static void showPlayer(Player player) {
        new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ((CraftPlayer) player).getHandle());
        try {
            player.spigot().setCollidesWithEntities(true);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            for (Player p : Bukkit.getOnlinePlayers()) p.showPlayer(player);
            Class<?> craftPlayerClass = getOBCClass("entity.CraftPlayer");
            Class<?> enumGamemodeClass = getNMSClass("WorldSettings$EnumGamemode");
            Object craftPlayerHandle = craftPlayerClass.getDeclaredMethod("getHandle").invoke(craftPlayerClass.cast(player));
            Object playerInteractManager = craftPlayerHandle.getClass().getDeclaredField("playerInteractManager").get(craftPlayerHandle);
            playerInteractManager.getClass().getDeclaredMethod("setGameMode", enumGamemodeClass).invoke(playerInteractManager, Enum.valueOf(enumGamemodeClass.asSubclass(Enum.class), "CREATIVE"));
        } catch (Exception ignored) { }
    }

    public static void togglePlayerTab(Player player, boolean toggle) {
        try {
            Class<?> craftPlayerClass = getOBCClass("entity.CraftPlayer");
            Class<?> packetPlayOutPlayerInfoClass = getNMSClass("PacketPlayOutPlayerInfo");
            Class<?> enumPlayerInfoActionClass = getNMSClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
            Object craftPlayerHandle = craftPlayerClass.getDeclaredMethod("getHandle").invoke(craftPlayerClass.cast(player));
            Object server = craftPlayerHandle.getClass().getDeclaredField("server").get(craftPlayerHandle);
            Object playerList = server.getClass().getDeclaredMethod("getPlayerList").invoke(server);
            playerList.getClass().getDeclaredMethod("sendAll", getNMSClass("Packet"), getNMSClass("EntityHuman")).invoke(playerList, packetPlayOutPlayerInfoClass.getConstructor(new Class<?>[]{enumPlayerInfoActionClass, getNMSClass("EntityPlayer")}).newInstance(Enum.valueOf(enumPlayerInfoActionClass.asSubclass(Enum.class), toggle ? "ADD_PLAYER" : "REMOVE_PLAYER"), craftPlayerHandle), craftPlayerHandle);
        } catch (Exception ignored) { }
    }
}
