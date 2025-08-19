package cc.meteormc.sbpractice.version;

import cc.meteormc.sbpractice.api.bukkitfix.blockstate.Stairs;
import cc.meteormc.sbpractice.api.version.NMS;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_12_R1.*;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBanner;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBed;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftSkull;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class v1_12_R1 extends NMS {
    public v1_12_R1(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void registerCommand(Command command) {
        ((CraftServer) Bukkit.getServer()).getCommandMap().register(command.getName(), command);
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        if (title != null) {
            IChatBaseComponent bc = CraftChatMessage.fromString(title)[0];
            PacketPlayOutTitle tit = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, bc);
            PacketPlayOutTitle length = new PacketPlayOutTitle(fadeIn, stay, fadeOut);
            connection.sendPacket(tit);
            connection.sendPacket(length);
        }

        if (subTitle != null) {
            IChatBaseComponent bc = CraftChatMessage.fromString(subTitle)[0];
            PacketPlayOutTitle tit = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, bc);
            PacketPlayOutTitle length = new PacketPlayOutTitle(fadeIn, stay, fadeOut);
            connection.sendPacket(tit);
            connection.sendPacket(length);
        }
    }

    @Override
    public void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    @Override
    public void setUnbreakable(ItemMeta itemMeta) {
        itemMeta.setUnbreakable(true);
    }

    @Override
    public ItemStack setItemTag(ItemStack itemStack, String key, String value) {
        net.minecraft.server.v1_12_R1.ItemStack is = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = is.getTag();
        if (tag == null) tag = new NBTTagCompound();

        tag.setString(key, value);
        is.setTag(tag);
        return CraftItemStack.asCraftMirror(is);
    }

    @Override
    public String getItemTag(ItemStack itemStack, String key) {
        net.minecraft.server.v1_12_R1.ItemStack i = CraftItemStack.asNMSCopy(itemStack);
        if (i == null) return null;

        NBTTagCompound tag = i.getTag();
        return tag == null ? null : tag.hasKey(key) ? tag.getString(key) : null;
    }

    @Override
    public boolean hasItemTag(ItemStack itemStack, String key) {
        net.minecraft.server.v1_12_R1.ItemStack i = CraftItemStack.asNMSCopy(itemStack);
        if (i == null) return false;

        NBTTagCompound tag = i.getTag();
        return tag != null && tag.hasKey(key);
    }

    @Override
    public ItemStack removeItemTag(ItemStack itemStack, String key) {
        net.minecraft.server.v1_12_R1.ItemStack is = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = is.getTag();
        if (tag == null) tag = new NBTTagCompound();

        tag.remove(key);
        is.setTag(tag);
        return CraftItemStack.asCraftMirror(is);
    }

    @Override
    public void hidePlayer(Player player) {
        CraftPlayer cPlayer = (CraftPlayer) player;
        cPlayer.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
        try {
            Field gm = PlayerInteractManager.class.getDeclaredField("gamemode");
            gm.setAccessible(true);
            PlayerInteractManager pim = cPlayer.getHandle().playerInteractManager;
            gm.set(pim, EnumGamemode.SPECTATOR);
            PacketPlayOutPlayerInfo info = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, cPlayer.getHandle());
            player.getWorld().getPlayers().forEach(p -> {
                p.hidePlayer(super.plugin, player);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(info);
            });
            gm.set(pim, EnumGamemode.CREATIVE);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        cPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE, cPlayer.getHandle()));
    }

    @Override
    public void showPlayer(Player player) {
        CraftPlayer cPlayer = (CraftPlayer) player;
        cPlayer.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.getWorld().getPlayers().forEach(p -> p.showPlayer(super.plugin, player));
        cPlayer.getHandle().playerInteractManager.setGameMode(EnumGamemode.CREATIVE);
    }

    @Override
    public void fixOtherPlayerTab(Player player) {
        EntityPlayer ePlayer = ((CraftPlayer) player).getHandle();
        ePlayer.server.getPlayerList()
                .sendAll(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo
                .EnumPlayerInfoAction.REMOVE_PLAYER, ePlayer));
    }

    @Override
    public BlockState createBlockState(Material material, String data) {
        CraftBlockState result;
        String[] split = data.split(":");
        switch (material) {
            case BED_BLOCK:
                CraftBed bed = new CraftBed(material, new TileEntityBed());
                if (split.length > 1) {
                    bed.setColor(DyeColor.valueOf(split[1]));
                }
                result = bed;
                break;
            case SKULL:
                CraftSkull skull = new CraftSkull(material, new TileEntitySkull());
                if (split.length > 2) {
                    skull.setRotation(BlockFace.valueOf(split[1]));
                    skull.setSkullType(SkullType.valueOf(split[2]));
                }
                result = skull;
                break;
            case STANDING_BANNER:
            case WALL_BANNER:
            case BANNER:
                CraftBanner banner = new CraftBanner(material, new TileEntityBanner());
                if (split.length > 1) {
                    banner.setBaseColor(DyeColor.valueOf(split[1]));
                }
                result = banner;
                break;
            default: result = new CraftBlockState(material);
        }

        MaterialData mData = material.getNewData(Byte.parseByte(split[0]));
        if (mData instanceof org.bukkit.material.Stairs) {
            Stairs stairs = new Stairs(mData);
            if (split.length > 1) {
                stairs.setShape(Stairs.StairShape.valueOf(split[1]));
            }
            mData = stairs;
        }
        return this.setBlockStateData(result, mData);
    }

    @Override
    public BlockState setBlockStateLocation(BlockState state, Location location) {
        BlockState result = new CraftBlockState(location.getBlock());
        if (state instanceof CraftBanner) {
            CraftBanner banner = new CraftBanner(location.getBlock());
            banner.setBaseColor(((CraftBanner) state).getBaseColor());
            result = banner;
        } else if (state instanceof CraftBed) {
            CraftBed bed = new CraftBed(location.getBlock());
            bed.setColor(((CraftBed) state).getColor());
            result = bed;
        } else if (state instanceof CraftSkull) {
            CraftSkull skull = new CraftSkull(location.getBlock());
            skull.setRotation(((CraftSkull) state).getRotation());
            skull.setSkullType(((CraftSkull) state).getSkullType());
            result = skull;
        }
        result.setType(state.getType());
        return this.setBlockStateData(result, state.getData());
    }

    @Override
    public BlockState setBlockStateData(BlockState state, MaterialData data) {
        try {
            final Field field = CraftBlockState.class.getDeclaredField("data");
            field.setAccessible(true);
            field.set(state, data);
        } catch (IllegalAccessException | NoSuchFieldException ignored) { }
        return state;
    }

    @Override
    public String getDataByBlockState(BlockState state) {
        StringBuilder result = new StringBuilder().append(state.getData().getData());
        switch (state.getType()) {
            case STANDING_BANNER:
            case WALL_BANNER:
            case BANNER:
                if (state instanceof CraftBanner) {
                    CraftBanner banner = (CraftBanner) state;
                    result.append(":").append(banner.getBaseColor().name());
                }
                break;
            case BED_BLOCK:
                if (state instanceof CraftBed) {
                    CraftBed bed = (CraftBed) state;
                    result.append(":").append(bed.getColor().name());
                }
                break;
            case SKULL:
                if (state instanceof CraftSkull) {
                    CraftSkull skull = (CraftSkull) state;
                    result.append(":").append(skull.getRotation().name()).append(":").append(skull.getSkullType().name());
                }
                break;
            case WOOD_STAIRS:
            case COBBLESTONE_STAIRS:
            case BRICK_STAIRS:
            case SMOOTH_STAIRS:
            case NETHER_BRICK_STAIRS:
            case SANDSTONE_STAIRS:
            case SPRUCE_WOOD_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case QUARTZ_STAIRS:
            case ACACIA_STAIRS:
            case DARK_OAK_STAIRS:
            case RED_SANDSTONE_STAIRS:
            case PURPUR_STAIRS:
                if (state.getData() instanceof Stairs) {
                    Stairs stairs = (Stairs) state.getData();
                    result.append(":").append(stairs.getShape().name());
                }
                break;
        }
        return result.toString();
    }

    @Override
    public BlockState getBlockState(Location location) {
        BlockState state = location.getBlock().getState();
        if (state.getData() instanceof org.bukkit.material.Stairs) {
            Stairs stairs = new Stairs(state.getData());
            BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            IBlockData data = ((CraftChunk) location.getChunk()).getHandle().getBlockData(position);
            stairs.setShape(Stairs.StairShape.values()[data.getBlock().updateState(data, ((CraftWorld) location.getWorld()).getHandle(), position).get(BlockStairs.SHAPE).ordinal()]);
            this.setBlockStateData(state, stairs);
        }
        return state;
    }

    @Override
    public boolean isSimilarBlockState(BlockState state1, BlockState state2) {
        if (!state1.getData().equals(state2.getData())) {
            if (state1.getData() instanceof Stairs && state2.getData() instanceof Stairs) {
                Stairs stairs1 = (Stairs) state1.getData();
                Stairs stairs2 = (Stairs) state2.getData();
                if (Math.abs(stairs1.getFacing().ordinal() - stairs2.getFacing().ordinal()) == 1
                        || stairs1.getFacing() == BlockFace.NORTH && stairs2.getFacing() == BlockFace.WEST
                        || stairs1.getFacing() == BlockFace.WEST && stairs2.getFacing() == BlockFace.NORTH
                        && stairs1.isInverted() == stairs2.isInverted()) {
                    return (stairs1.getShape().name().contains("LEFT") && stairs2.getShape().name().contains("RIGHT")) || (stairs1.getShape().name().contains("RIGHT") && stairs2.getShape().name().contains("LEFT"));
                }
            }
        } else {
            if (state1 instanceof CraftBanner || state2 instanceof CraftBanner) {
                if (state1 instanceof CraftBanner && state2 instanceof CraftBanner) {
                    return ((CraftBanner) state1).getBaseColor() == ((CraftBanner) state2).getBaseColor();
                }
            } else if (state1 instanceof CraftBed || state2 instanceof CraftBed) {
                if (state1 instanceof CraftBed && state2 instanceof CraftBed) {
                    return ((CraftBed) state1).getColor() == ((CraftBed) state2).getColor();
                }
            } else if (state1 instanceof CraftSkull || state2 instanceof CraftSkull) {
                if (state1 instanceof CraftSkull && state2 instanceof CraftSkull) {
                    return ((CraftSkull) state1).getSkullType() == ((CraftSkull) state2).getSkullType();
                }
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public CompoundTag getTileEntityData(Block block) {
        CompoundTag tag = new CompoundTag();
        TileEntity entity = ((CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
        if (entity == null) return tag;
        if (entity instanceof TileEntityBanner) {
            tag.putString("Id", "Banner");
            TileEntityBanner banner = (TileEntityBanner) entity;
            tag.putInt("Color", banner.color.getInvColorIndex());

            ListTag<CompoundTag> patterns = new ListTag<>(CompoundTag.class);
            for (int i = 0; i < banner.patterns.size(); i++) {
                NBTTagCompound pattern = banner.patterns.get(i);
                CompoundTag patternTag = new CompoundTag();
                if (pattern.hasKey("Color")) patternTag.putInt("Color", pattern.getInt("Color"));
                if (pattern.hasKey("Pattern")) patternTag.putString("Pattern", pattern.getString("Pattern"));
                patterns.add(patternTag);
            }
            tag.put("Patterns", patterns);
        } else if (entity instanceof TileEntityBed) {
            tag.putString("Id", "Bed");
            TileEntityBed bed = (TileEntityBed) entity;
            tag.putInt("Color", bed.a().getColorIndex());
        } else if (entity instanceof TileEntitySign) {
            tag.putString("Id", "Sign");
            TileEntitySign sign = (TileEntitySign) entity;
            for (int i = 0; i < 4; i++) {
                tag.putString("Text" + i, CraftChatMessage.fromComponent(sign.lines[i]));
            }
        } else if (entity instanceof TileEntitySkull) {
            tag.putString("Id", "Skull");
            TileEntitySkull skull = (TileEntitySkull) entity;
            tag.putInt("SkullType", skull.getSkullType());
            tag.putInt("Rotation", skull.rotation);
        }
        return tag;
    }

    @Override
    public void setTileEntityData(Block block, CompoundTag tag) {
        TileEntity entity = ((CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
        if (entity == null) return;
        switch (tag.getString("Id")) {
            case "Banner":
                if (entity instanceof TileEntityBanner) {
                    TileEntityBanner banner = ((TileEntityBanner) entity);
                    banner.color = EnumColor.fromInvColorIndex(tag.getInt("Color"));

                    NBTTagList patterns = new NBTTagList();
                    for (CompoundTag pattern : tag.getListTag("Patterns").asCompoundTagList()) {
                        NBTTagCompound patternTag = new NBTTagCompound();
                        patternTag.setInt("Color", pattern.getInt("Color"));
                        patternTag.setString("Pattern", pattern.getString("Pattern"));
                        patterns.add(patternTag);
                    }
                    banner.patterns = patterns;
                }
                break;
            case "Bed":
                if (entity instanceof TileEntityBed) {
                    TileEntityBed bed = ((TileEntityBed) entity);
                    bed.a(EnumColor.fromColorIndex(tag.getInt("Color")));
                }
                break;
            case "Sign":
                if (entity instanceof TileEntitySign) {
                    TileEntitySign sign = ((TileEntitySign) entity);
                    for (int i = 0; i < 4; i++) {
                        sign.lines[i] = CraftChatMessage.fromString(tag.getString("Text" + i))[0];
                    }
                }
                break;
            case "Skull":
                if (entity instanceof TileEntitySkull) {
                    TileEntitySkull skull = ((TileEntitySkull) entity);
                    skull.setSkullType(tag.getInt("SkullType"));
                    skull.setRotation(tag.getInt("Rotation"));
                }
                break;
            default: return;
        }
        entity.update();
    }

    @Override
    public ItemStack getItemByBlock(Material material, byte data) {
        try {
            net.minecraft.server.v1_12_R1.Block block = CraftMagicNumbers.getBlock(material);
            Method method = net.minecraft.server.v1_12_R1.Block.class.getDeclaredMethod("u", IBlockData.class);
            method.setAccessible(true);
            return CraftItemStack.asCraftMirror((net.minecraft.server.v1_12_R1.ItemStack) method.invoke(block, block.fromLegacyData(data)));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to get item by block " + material.name(), e);
        }
    }
}
