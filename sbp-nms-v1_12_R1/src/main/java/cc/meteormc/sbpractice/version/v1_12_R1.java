package cc.meteormc.sbpractice.version;

import cc.meteormc.sbpractice.api.SBPracticeAPI;
import cc.meteormc.sbpractice.api.storage.data.BlockData;
import cc.meteormc.sbpractice.api.version.NMS;
import com.cryptomorin.xseries.XMaterial;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.*;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NBTSerializer;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Stairs;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

public class v1_12_R1 extends NMS {
    @Override
    public void registerCommand(@NotNull Command command) {
        ((CraftServer) Bukkit.getServer()).getCommandMap().register(command.getName(), command);
    }

    @Override
    public void setItemUnbreakable(@NotNull ItemMeta itemMeta) {
        itemMeta.setUnbreakable(true);
    }

    @Override
    public @NotNull ItemStack setItemTag(@NotNull ItemStack itemStack, @NotNull String key, @NotNull String value) {
        net.minecraft.server.v1_12_R1.ItemStack is = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = is.getTag();
        if (tag == null) tag = new NBTTagCompound();

        tag.setString(key, value);
        is.setTag(tag);
        return CraftItemStack.asCraftMirror(is);
    }

    @Override
    public @Nullable String getItemTag(@NotNull ItemStack itemStack, @NotNull String key) {
        net.minecraft.server.v1_12_R1.ItemStack i = CraftItemStack.asNMSCopy(itemStack);
        if (i == null) return null;

        NBTTagCompound tag = i.getTag();
        if (tag == null) return null;
        return tag.hasKey(key) ? tag.getString(key) : null;
    }

    @Override
    public boolean hasItemTag(@NotNull ItemStack itemStack, @NotNull String key) {
        net.minecraft.server.v1_12_R1.ItemStack i = CraftItemStack.asNMSCopy(itemStack);
        if (i == null) return false;

        NBTTagCompound tag = i.getTag();
        return tag != null && tag.hasKey(key);
    }

    @Override
    public @NotNull ItemStack removeItemTag(@NotNull ItemStack itemStack, @NotNull String key) {
        net.minecraft.server.v1_12_R1.ItemStack is = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = is.getTag();
        if (tag == null) tag = new NBTTagCompound();

        tag.remove(key);
        is.setTag(tag);
        return CraftItemStack.asCraftMirror(is);
    }

    @Override
    public @NotNull ItemStack getItemByBlock(Material material, byte data) {
        try {
            net.minecraft.server.v1_12_R1.Block block = CraftMagicNumbers.getBlock(material);
            Method method = net.minecraft.server.v1_12_R1.Block.class.getDeclaredMethod("u", IBlockData.class);
            method.setAccessible(true);
            return CraftItemStack.asCraftMirror((net.minecraft.server.v1_12_R1.ItemStack) method.invoke(block, block.fromLegacyData(data)));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get item by block " + material.name(), e);
        }
    }

    @Override
    public void hidePlayer(@NotNull Player player) {
        CraftPlayer cPlayer = (CraftPlayer) player;
        EntityPlayer handle = cPlayer.getHandle();
        cPlayer.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                Integer.MAX_VALUE,
                1,
                false,
                false
        ));

        try {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE
            );
            Field field = PacketPlayOutPlayerInfo.class.getDeclaredField("b");
            field.setAccessible(true);

            // Use reflection because @javax.annotation.Nullable causes compile-time annotation errors
            Class<?> clazz = Class.forName("net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo$PlayerInfoData");
            Constructor<?> ctor = clazz.getDeclaredConstructor(GameProfile.class, int.class, EnumGamemode.class, IChatBaseComponent.class);
            //noinspection ALL
            ((List) field.get(packet)).add(ctor.newInstance(
                    handle.getProfile(),
                    handle.ping,
                    EnumGamemode.SPECTATOR,
                    handle.getPlayerListName()
            ));

            player.getWorld().getPlayers().forEach(p -> {
                if (player.equals(p)) return;
                p.hidePlayer(SBPracticeAPI.getInstance().getPlugin(), player);
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
            });
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showPlayer(@NotNull Player player) {
        CraftPlayer cPlayer = (CraftPlayer) player;
        cPlayer.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.getWorld().getPlayers().forEach(p -> {
            p.showPlayer(SBPracticeAPI.getInstance().getPlugin(), player);
        });
        cPlayer.getHandle().playerInteractManager.setGameMode(EnumGamemode.CREATIVE);
    }

    @Override
    public @NotNull BlockData getBlockDataAt(@NotNull Location location) {
        Block block = location.getBlock();
        Material type = block.getType();
        MaterialData data = block.getState().getData();
        CompoundTag entity = this.getBlockEntityNBT(block);
        if (data instanceof Stairs) {
            FixedStairs stairs = new FixedStairs(data);
            BlockPosition position = toNMSPosition(location);
            World world = ((CraftWorld) location.getWorld()).getHandle();
            IBlockData blockData = getBlock(world, position);
            BlockStairs.EnumStairShape shape = blockData.getBlock().updateState(
                    blockData,
                    world,
                    position
            ).get(BlockStairs.SHAPE);
            stairs.setShape(FixedStairs.StairShape.values()[shape.ordinal()]);
            data = stairs;
        }
        return new BlockData(type, data, entity);
    }

    @Override
    public boolean isSimilarBlock(@NotNull BlockData b1, @NotNull BlockData b2) {
        MaterialData data1 = b1.getData();
        MaterialData data2 = b2.getData();
        if (!data1.equals(data2)) {
            if (data1 instanceof FixedStairs && data2 instanceof FixedStairs) {
                FixedStairs stairs1 = (FixedStairs) data1;
                FixedStairs stairs2 = (FixedStairs) data2;
                if (Math.abs(stairs1.getFacing().ordinal() - stairs2.getFacing().ordinal()) == 1
                        || stairs1.getFacing() == BlockFace.NORTH && stairs2.getFacing() == BlockFace.WEST
                        || stairs1.getFacing() == BlockFace.WEST && stairs2.getFacing() == BlockFace.NORTH
                        && stairs1.isInverted() == stairs2.isInverted()) {
                    return (stairs1.getShape().name().contains("LEFT") && stairs2.getShape().name().contains("RIGHT"))
                            || (stairs1.getShape().name().contains("RIGHT") && stairs2.getShape().name().contains("LEFT"));
                }
            }
            return false;
        } else {
            CompoundTag entity1 = b1.getBlockEntity();
            CompoundTag entity2 = b2.getBlockEntity();
            return Objects.equals(filterBlockEntityNBT(entity1), filterBlockEntityNBT(entity2));
        }
    }

    @Override
    public boolean toFullBlock(@NotNull Block block) {
        if (block.getType() == null) return false;
        BlockData newBlock;
        Location location = block.getLocation();

        BlockPosition position = toNMSPosition(location);
        World world = ((CraftWorld) block.getWorld()).getHandle();
        IBlockData blockData = getBlock(world, position);
        net.minecraft.server.v1_12_R1.Block nmsBlock = blockData.getBlock();
        if (nmsBlock instanceof BlockAnvil) {
            newBlock = BlockData.of(XMaterial.IRON_BLOCK);
        } else if (nmsBlock instanceof BlockBanner) {
            newBlock = BlockData.of(Material.WOOL, block.getData());
        } else if (nmsBlock instanceof BlockBed) {
            newBlock = BlockData.of(Material.WOOL, block.getData());
        } else if (nmsBlock instanceof BlockBrewingStand) {
            newBlock = BlockData.of(XMaterial.GOLD_BLOCK);
        } else if (nmsBlock instanceof BlockButtonAbstract) {
            EnumDirection facing = blockData.get(BlockDirectional.FACING);
            if (facing == EnumDirection.UP || facing == EnumDirection.DOWN) {
                if (nmsBlock instanceof BlockStoneButton) {
                    newBlock = BlockData.of(XMaterial.STONE);
                } else if (nmsBlock instanceof BlockWoodButton) {
                    newBlock = BlockData.of(XMaterial.OAK_PLANKS);
                } else {
                    newBlock = null;
                }
            } else {
                newBlock = null;
            }
        } else if (nmsBlock instanceof BlockCactus) {
            newBlock = BlockData.of(XMaterial.SAND);
        } else if (nmsBlock instanceof BlockCake) {
            newBlock = BlockData.of(XMaterial.WHITE_WOOL);
        } else if (nmsBlock instanceof BlockCarpet) {
            newBlock = BlockData.of(Material.WOOL, block.getData());
        } else if (nmsBlock instanceof BlockChest) {
            newBlock = BlockData.of(XMaterial.OAK_PLANKS);
        } else if (nmsBlock instanceof BlockWeb) {
            newBlock = BlockData.of(XMaterial.WHITE_WOOL);
        } else if (nmsBlock instanceof BlockCrops) {
            newBlock = BlockData.of(XMaterial.FARMLAND);
        } else if (nmsBlock instanceof BlockDaylightDetector) {
            newBlock = BlockData.of(XMaterial.DARK_OAK_PLANKS);
        } else if (nmsBlock instanceof BlockDeadBush) {
            newBlock = BlockData.of(XMaterial.RED_SAND);
        } else if (nmsBlock instanceof BlockDoor) {
            if (nmsBlock == Blocks.IRON_DOOR) {
                newBlock = BlockData.of(XMaterial.IRON_DOOR);
            } else if (nmsBlock == Blocks.SPRUCE_DOOR) {
                newBlock = BlockData.of(XMaterial.SPRUCE_DOOR);
            } else if (nmsBlock == Blocks.BIRCH_DOOR) {
                newBlock = BlockData.of(XMaterial.BIRCH_DOOR);
            } else if (nmsBlock == Blocks.JUNGLE_DOOR) {
                newBlock = BlockData.of(XMaterial.JUNGLE_DOOR);
            } else if (nmsBlock == Blocks.ACACIA_DOOR) {
                newBlock = BlockData.of(XMaterial.ACACIA_DOOR);
            } else if (nmsBlock == Blocks.DARK_OAK_DOOR) {
                newBlock = BlockData.of(XMaterial.DARK_OAK_DOOR);
            } else if (nmsBlock == Blocks.WOODEN_DOOR) {
                newBlock = BlockData.of(XMaterial.OAK_DOOR);
            } else {
                newBlock = null;
            }
        } else if (nmsBlock instanceof BlockTallPlant) {
            newBlock = BlockData.of(XMaterial.GRASS_BLOCK);
        } else if (nmsBlock instanceof BlockEnchantmentTable) {
            newBlock = BlockData.of(XMaterial.DIAMOND_BLOCK);
        } else if (nmsBlock instanceof BlockEndRod) {
            EnumDirection facing = blockData.get(BlockDirectional.FACING);
            if (facing == EnumDirection.UP || facing == EnumDirection.DOWN) {
                newBlock = BlockData.of(XMaterial.QUARTZ_BLOCK);
            } else {
                newBlock = null;
            }
        } else if (nmsBlock instanceof BlockEnderChest) {
            newBlock = BlockData.of(XMaterial.OBSIDIAN);
        } else if (nmsBlock instanceof BlockEnderPortalFrame) {
            newBlock = BlockData.of(XMaterial.END_STONE);
        } else if (nmsBlock instanceof BlockFence) {
            if (nmsBlock == Blocks.SPRUCE_FENCE) {
                newBlock = BlockData.of(XMaterial.SPRUCE_PLANKS);
            } else if (nmsBlock == Blocks.BIRCH_FENCE) {
                newBlock = BlockData.of(XMaterial.BIRCH_PLANKS);
            } else if (nmsBlock == Blocks.JUNGLE_FENCE) {
                newBlock = BlockData.of(XMaterial.JUNGLE_PLANKS);
            } else if (nmsBlock == Blocks.ACACIA_FENCE) {
                newBlock = BlockData.of(XMaterial.ACACIA_PLANKS);
            } else if (nmsBlock == Blocks.DARK_OAK_FENCE) {
                newBlock = BlockData.of(XMaterial.DARK_OAK_PLANKS);
            } else if (nmsBlock == Blocks.FENCE) {
                newBlock = BlockData.of(XMaterial.OAK_PLANKS);
            } else {
                newBlock = null;
            }
        } else if (nmsBlock instanceof BlockFenceGate) {
            if (nmsBlock == Blocks.SPRUCE_FENCE_GATE) {
                newBlock = BlockData.of(XMaterial.SPRUCE_PLANKS);
            } else if (nmsBlock == Blocks.BIRCH_FENCE_GATE) {
                newBlock = BlockData.of(XMaterial.BIRCH_PLANKS);
            } else if (nmsBlock == Blocks.JUNGLE_FENCE_GATE) {
                newBlock = BlockData.of(XMaterial.JUNGLE_PLANKS);
            } else if (nmsBlock == Blocks.ACACIA_FENCE_GATE) {
                newBlock = BlockData.of(XMaterial.ACACIA_PLANKS);
            } else if (nmsBlock == Blocks.DARK_OAK_FENCE_GATE) {
                newBlock = BlockData.of(XMaterial.DARK_OAK_PLANKS);
            } else if (nmsBlock == Blocks.FENCE_GATE) {
                newBlock = BlockData.of(XMaterial.OAK_PLANKS);
            } else {
                newBlock = null;
            }
        } else if (nmsBlock instanceof BlockFire) {
            newBlock = BlockData.of(XMaterial.NETHERRACK);
        } else if (nmsBlock instanceof BlockFlowerPot) {
            newBlock = null;
        } else if (nmsBlock instanceof BlockFlowers) {
            newBlock = BlockData.of(XMaterial.GRASS_BLOCK);
        } else if (nmsBlock instanceof BlockThin) {
            if (nmsBlock == Blocks.GLASS_PANE) {
                newBlock = BlockData.of(XMaterial.GLASS);
            } else if (nmsBlock == Blocks.STAINED_GLASS_PANE) {
                newBlock = BlockData.of(Material.STAINED_GLASS, block.getData());
            } else if (nmsBlock == Blocks.IRON_BARS) {
                newBlock = BlockData.of(XMaterial.IRON_BLOCK);
            } else {
                newBlock = null;
            }
        } else if (nmsBlock instanceof BlockSkull) {
            newBlock = null;
        } else if (nmsBlock instanceof BlockHopper) {
            newBlock = BlockData.of(XMaterial.IRON_BLOCK);
        } else if (nmsBlock instanceof BlockLadder) {
            newBlock = null;
        } else if (nmsBlock instanceof BlockLever) {
            BlockLever.EnumLeverPosition facing = blockData.get(BlockLever.FACING);
            if (facing.c() == EnumDirection.UP || facing.c() == EnumDirection.DOWN) {
                newBlock = BlockData.of(XMaterial.COBBLESTONE);
            } else {
                newBlock = null;
            }
        } else if (nmsBlock instanceof BlockWaterLily) {
            newBlock = null;
        } else if (nmsBlock instanceof BlockMushroom) {
            newBlock = BlockData.of(XMaterial.MYCELIUM);
        } else if (nmsBlock instanceof BlockNetherWart) {
            newBlock = BlockData.of(XMaterial.SOUL_SAND);
        } else if (nmsBlock instanceof BlockPressurePlateAbstract) {
            if (nmsBlock == Blocks.STONE_PRESSURE_PLATE) {
                newBlock = BlockData.of(XMaterial.STONE);
            } else if (nmsBlock == Blocks.WOODEN_PRESSURE_PLATE) {
                newBlock = BlockData.of(XMaterial.OAK_PLANKS);
            } else if (nmsBlock == Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                newBlock = BlockData.of(XMaterial.IRON_BLOCK);
            } else if (nmsBlock == Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE) {
                newBlock = BlockData.of(XMaterial.GOLD_BLOCK);
            } else {
                newBlock = null;
            }
        } else if (nmsBlock instanceof BlockMinecartTrackAbstract) {
            newBlock = BlockData.of(XMaterial.IRON_BLOCK);
        } else if (nmsBlock instanceof BlockRedstoneWire) {
            newBlock = BlockData.of(XMaterial.RED_WOOL);
        } else if (nmsBlock instanceof BlockRedstoneComparator) {
            newBlock = BlockData.of(XMaterial.RED_WOOL);
        } else if (nmsBlock instanceof BlockRepeater) {
            newBlock = BlockData.of(XMaterial.RED_WOOL);
        } else if (nmsBlock instanceof BlockSapling) {
            newBlock = null;
        } else if (nmsBlock instanceof BlockSign) {
            if (nmsBlock == Blocks.STANDING_SIGN) {
                newBlock = BlockData.of(XMaterial.OAK_PLANKS);
            } else {
                newBlock = null;
            }
        } else if (nmsBlock instanceof BlockStepAbstract) {
            if (nmsBlock instanceof BlockDoubleStepAbstract) { // Stone Slab
                BlockDoubleStepAbstract.EnumStoneSlabVariant variant = blockData.get(BlockDoubleStepAbstract.VARIANT);
                switch (variant) {
                    case STONE:
                        newBlock = BlockData.of(Material.DOUBLE_STEP, (byte) 8);
                        break;
                    case SAND:
                        newBlock = BlockData.of(XMaterial.SANDSTONE);
                        break;
                    case COBBLESTONE:
                        newBlock = BlockData.of(XMaterial.COBBLESTONE);
                        break;
                    case BRICK:
                        newBlock = BlockData.of(XMaterial.BRICK);
                        break;
                    case SMOOTHBRICK:
                        newBlock = BlockData.of(XMaterial.STONE_BRICKS);
                        break;
                    case NETHERBRICK:
                        newBlock = BlockData.of(XMaterial.NETHER_BRICK);
                        break;
                    case QUARTZ:
                        newBlock = BlockData.of(XMaterial.QUARTZ_BLOCK);
                        break;
                    default:
                        newBlock = null;
                        break;
                }
            } else if (nmsBlock instanceof BlockWoodenStep) { // Wood Slab
                BlockWood.EnumLogVariant variant = blockData.get(BlockWoodenStep.VARIANT);
                newBlock = BlockData.of(Material.WOOD, (byte) variant.a());
            } else if (nmsBlock instanceof BlockPurpurSlab) { // Purpur Slab
                newBlock = BlockData.of(XMaterial.PURPUR_BLOCK);
            } else if (nmsBlock instanceof BlockDoubleStoneStepAbstract) { // Red Sandstone Slab
                newBlock = BlockData.of(XMaterial.RED_SANDSTONE);
            } else {
                newBlock = null;
            }
        } else if (nmsBlock instanceof BlockSnow) {
            newBlock = BlockData.of(XMaterial.SNOW_BLOCK);
        } else if (nmsBlock instanceof BlockStairs) {
            if (nmsBlock == Blocks.OAK_STAIRS) {
                newBlock = BlockData.of(XMaterial.OAK_PLANKS);
            } else if (nmsBlock == Blocks.STONE_STAIRS) {
                newBlock = BlockData.of(XMaterial.COBBLESTONE);
            } else if (nmsBlock == Blocks.BRICK_STAIRS) {
                newBlock = BlockData.of(XMaterial.BRICK);
            } else if (nmsBlock == Blocks.STONE_BRICK_STAIRS) {
                newBlock = BlockData.of(XMaterial.STONE_BRICKS);
            } else if (nmsBlock == Blocks.NETHER_BRICK_STAIRS) {
                newBlock = BlockData.of(XMaterial.NETHER_BRICK);
            } else if (nmsBlock == Blocks.SANDSTONE_STAIRS) {
                newBlock = BlockData.of(XMaterial.SANDSTONE);
            } else if (nmsBlock == Blocks.SPRUCE_STAIRS) {
                newBlock = BlockData.of(XMaterial.SPRUCE_PLANKS);
            } else if (nmsBlock == Blocks.BIRCH_STAIRS) {
                newBlock = BlockData.of(XMaterial.BIRCH_PLANKS);
            } else if (nmsBlock == Blocks.JUNGLE_STAIRS) {
                newBlock = BlockData.of(XMaterial.JUNGLE_PLANKS);
            } else if (nmsBlock == Blocks.QUARTZ_STAIRS) {
                newBlock = BlockData.of(XMaterial.QUARTZ_BLOCK);
            } else if (nmsBlock == Blocks.ACACIA_STAIRS) {
                newBlock = BlockData.of(XMaterial.ACACIA_PLANKS);
            } else if (nmsBlock == Blocks.DARK_OAK_STAIRS) {
                newBlock = BlockData.of(XMaterial.DARK_OAK_PLANKS);
            } else if (nmsBlock == Blocks.RED_SANDSTONE_STAIRS) {
                newBlock = BlockData.of(XMaterial.RED_SANDSTONE);
            } else if (nmsBlock == Blocks.PURPUR_STAIRS) {
                newBlock = BlockData.of(XMaterial.PURPUR_BLOCK);
            } else {
                newBlock = null;
            }
        } else if (nmsBlock instanceof BlockStem) {
            newBlock = BlockData.of(XMaterial.FARMLAND);
        } else if (nmsBlock instanceof BlockReed) {
            newBlock = null;
        } else if (nmsBlock instanceof BlockLongGrass) {
            newBlock = BlockData.of(XMaterial.GRASS_BLOCK);
        } else if (nmsBlock instanceof BlockTorch) {
            newBlock = BlockData.of(XMaterial.OAK_PLANKS);
        } else if (nmsBlock instanceof BlockTrapdoor) {
            boolean open = blockData.get(BlockTrapdoor.OPEN);
            BlockTrapdoor.EnumTrapdoorHalf half = blockData.get(BlockTrapdoor.HALF);
            if (!open && half == BlockTrapdoor.EnumTrapdoorHalf.BOTTOM) {
                if (nmsBlock == Blocks.TRAPDOOR) {
                    newBlock = BlockData.of(XMaterial.OAK_PLANKS);
                } else if (nmsBlock == Blocks.IRON_TRAPDOOR) {
                    newBlock = BlockData.of(XMaterial.IRON_BLOCK);
                } else {
                    newBlock = null;
                }
            } else {
                newBlock = null;
            }
        } else if (nmsBlock instanceof BlockTripwireHook) {
            newBlock = null;
        } else if (nmsBlock instanceof BlockVine) {
            newBlock = null;
        } else if (nmsBlock instanceof BlockCobbleWall) {
            BlockCobbleWall.EnumCobbleVariant variant = blockData.get(BlockCobbleWall.VARIANT);
            switch (variant) {
                case NORMAL:
                    newBlock = BlockData.of(XMaterial.COBBLESTONE);
                    break;
                case MOSSY:
                    newBlock = BlockData.of(XMaterial.MOSSY_COBBLESTONE);
                    break;
                default:
                    newBlock = null;
                    break;
            }
        } else if (nmsBlock instanceof BlockFluids) {
            if (nmsBlock == Blocks.WATER || nmsBlock == Blocks.FLOWING_WATER) {
                newBlock = BlockData.of(XMaterial.LIGHT_BLUE_WOOL);
            } else if (nmsBlock == Blocks.LAVA || nmsBlock == Blocks.FLOWING_LAVA) {
                newBlock = BlockData.of(XMaterial.ORANGE_WOOL);
            } else {
                newBlock = null;
            }
        } else {
            return true;
        }

        if (newBlock != null) {
            this.setBlock(location, newBlock);
            return true;
        } else {
            return false;
        }
    }

    private IBlockData getBlock(World world, BlockPosition position) {
        return world.getType(position);
    }

    private BlockPosition toNMSPosition(Location location) {
        return new BlockPosition(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );
    }

    @Override
    public @Nullable CompoundTag getBlockEntityNBT(@NotNull Block block) {
        TileEntity entity = ((CraftWorld) block.getWorld()).getTileEntityAt(
                block.getX(),
                block.getY(),
                block.getZ()
        );
        if (entity == null) return null;

        try {
            NBTTagCompound tag = new NBTTagCompound();
            entity.save(tag);
            ByteArrayDataOutput output = ByteStreams.newDataOutput();
            NBTCompressedStreamTools.a(tag, output);
            ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
            return (CompoundTag) new NBTDeserializer(false).fromStream(input).getTag();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void updateBlockEntityNBT(@NotNull Block block, @NotNull CompoundTag tag) {
        TileEntity entity = ((CraftWorld) block.getWorld()).getTileEntityAt(
                block.getX(),
                block.getY(),
                block.getZ()
        );
        if (entity == null) return;

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            new NBTSerializer(false).toStream(new NamedTag(null, tag), output);
            ByteArrayDataInput input = ByteStreams.newDataInput(output.toByteArray());
            entity.load(NBTCompressedStreamTools.a(input, NBTReadLimiter.a));
            entity.update();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
