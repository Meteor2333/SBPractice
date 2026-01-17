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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
    public void setItemUnbreakable(@NotNull ItemMeta itemMeta) {
        itemMeta.setUnbreakable(true);
    }

    @Override
    public @NotNull ItemStack getItemByBlock(Material material, byte data) {
        try {
            net.minecraft.server.v1_12_R1.Block block = CraftMagicNumbers.getBlock(material);
            Method method = net.minecraft.server.v1_12_R1.Block.class.getDeclaredMethod("u", IBlockData.class);
            method.setAccessible(true);
            return CraftItemStack.asCraftMirror((net.minecraft.server.v1_12_R1.ItemStack) method.invoke(block, block.fromLegacyData(data)));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get item by block " + material.name() + "!", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void hidePlayer(@NotNull Player player) {
        CraftPlayer cPlayer = (CraftPlayer) player;
        EntityPlayer handle = cPlayer.getHandle();
        handle.collides = false;
        cPlayer.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                Integer.MAX_VALUE,
                1,
                false,
                false
        ));

        try {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(
                    PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER
            );
            Field field = PacketPlayOutPlayerInfo.class.getDeclaredField("b");
            field.setAccessible(true);

            List<PacketPlayOutPlayerInfo.PlayerInfoData> entries = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) field.get(packet);
            // Use reflection because @javax.annotation.Nullable causes compile-time annotation errors
            String className = "net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo$PlayerInfoData";
            Class<PacketPlayOutPlayerInfo.PlayerInfoData> clazz = (Class<PacketPlayOutPlayerInfo.PlayerInfoData>) Class.forName(className);
            Constructor<PacketPlayOutPlayerInfo.PlayerInfoData> ctor = clazz.getDeclaredConstructor(
                    PacketPlayOutPlayerInfo.class,
                    GameProfile.class,
                    Integer.TYPE,
                    EnumGamemode.class,
                    IChatBaseComponent.class
            );
            entries.add(ctor.newInstance(
                    packet,
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
        EntityPlayer handle = cPlayer.getHandle();
        handle.collides = true;
        cPlayer.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.getWorld().getPlayers().forEach(p -> {
            p.showPlayer(SBPracticeAPI.getInstance().getPlugin(), player);
        });
        handle.playerInteractManager.setGameMode(EnumGamemode.CREATIVE);
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
    public @NotNull BlockData toFullBlock(@NotNull Block block) {
        BlockData fullBlock;
        Location location = block.getLocation();
        BlockPosition position = toNMSPosition(location);
        World world = ((CraftWorld) block.getWorld()).getHandle();
        IBlockData blockData = getBlock(world, position);
        net.minecraft.server.v1_12_R1.Block nmsBlock = blockData.getBlock();
        if (nmsBlock instanceof BlockAir) {
            fullBlock = null;
        } else if (nmsBlock instanceof BlockAnvil) {
            fullBlock = BlockData.of(XMaterial.IRON_BLOCK);
        } else if (nmsBlock instanceof BlockBanner) {
            fullBlock = BlockData.of(Material.WOOL, block.getData());
        } else if (nmsBlock instanceof BlockBed) {
            fullBlock = BlockData.of(Material.WOOL, block.getData());
        } else if (nmsBlock instanceof BlockBrewingStand) {
            fullBlock = BlockData.of(XMaterial.GOLD_BLOCK);
        } else if (nmsBlock instanceof BlockButtonAbstract) {
            EnumDirection facing = blockData.get(BlockDirectional.FACING);
            if (facing == EnumDirection.UP || facing == EnumDirection.DOWN) {
                if (nmsBlock instanceof BlockStoneButton) {
                    fullBlock = BlockData.of(XMaterial.STONE);
                } else if (nmsBlock instanceof BlockWoodButton) {
                    fullBlock = BlockData.of(XMaterial.OAK_PLANKS);
                } else {
                    fullBlock = null;
                }
            } else {
                fullBlock = null;
            }
        } else if (nmsBlock instanceof BlockCactus) {
            fullBlock = BlockData.of(XMaterial.SAND);
        } else if (nmsBlock instanceof BlockCake) {
            fullBlock = BlockData.of(XMaterial.WHITE_WOOL);
        } else if (nmsBlock instanceof BlockCarpet) {
            fullBlock = BlockData.of(Material.WOOL, block.getData());
        } else if (nmsBlock instanceof BlockChest) {
            fullBlock = BlockData.of(XMaterial.OAK_PLANKS);
        } else if (nmsBlock instanceof BlockWeb) {
            fullBlock = BlockData.of(XMaterial.WHITE_WOOL);
        } else if (nmsBlock instanceof BlockCrops) {
            fullBlock = BlockData.of(XMaterial.FARMLAND);
        } else if (nmsBlock instanceof BlockDaylightDetector) {
            fullBlock = BlockData.of(XMaterial.DARK_OAK_PLANKS);
        } else if (nmsBlock instanceof BlockDeadBush) {
            fullBlock = BlockData.of(XMaterial.RED_SAND);
        } else if (nmsBlock instanceof BlockDoor) {
            if (nmsBlock == Blocks.IRON_DOOR) {
                fullBlock = BlockData.of(XMaterial.IRON_BLOCK);
            } else if (nmsBlock == Blocks.SPRUCE_DOOR) {
                fullBlock = BlockData.of(XMaterial.SPRUCE_PLANKS);
            } else if (nmsBlock == Blocks.BIRCH_DOOR) {
                fullBlock = BlockData.of(XMaterial.BIRCH_PLANKS);
            } else if (nmsBlock == Blocks.JUNGLE_DOOR) {
                fullBlock = BlockData.of(XMaterial.JUNGLE_PLANKS);
            } else if (nmsBlock == Blocks.ACACIA_DOOR) {
                fullBlock = BlockData.of(XMaterial.ACACIA_PLANKS);
            } else if (nmsBlock == Blocks.DARK_OAK_DOOR) {
                fullBlock = BlockData.of(XMaterial.DARK_OAK_PLANKS);
            } else if (nmsBlock == Blocks.WOODEN_DOOR) {
                fullBlock = BlockData.of(XMaterial.OAK_PLANKS);
            } else {
                fullBlock = null;
            }
        } else if (nmsBlock instanceof BlockTallPlant) {
            fullBlock = BlockData.of(XMaterial.GRASS_BLOCK);
        } else if (nmsBlock instanceof BlockEnchantmentTable) {
            fullBlock = BlockData.of(XMaterial.DIAMOND_BLOCK);
        } else if (nmsBlock instanceof BlockEndRod) {
            EnumDirection facing = blockData.get(BlockDirectional.FACING);
            if (facing == EnumDirection.UP || facing == EnumDirection.DOWN) {
                fullBlock = BlockData.of(XMaterial.QUARTZ_BLOCK);
            } else {
                fullBlock = null;
            }
        } else if (nmsBlock instanceof BlockEnderChest) {
            fullBlock = BlockData.of(XMaterial.OBSIDIAN);
        } else if (nmsBlock instanceof BlockEnderPortalFrame) {
            fullBlock = BlockData.of(XMaterial.END_STONE);
        } else if (nmsBlock instanceof BlockFence) {
            if (nmsBlock == Blocks.SPRUCE_FENCE) {
                fullBlock = BlockData.of(XMaterial.SPRUCE_PLANKS);
            } else if (nmsBlock == Blocks.BIRCH_FENCE) {
                fullBlock = BlockData.of(XMaterial.BIRCH_PLANKS);
            } else if (nmsBlock == Blocks.JUNGLE_FENCE) {
                fullBlock = BlockData.of(XMaterial.JUNGLE_PLANKS);
            } else if (nmsBlock == Blocks.ACACIA_FENCE) {
                fullBlock = BlockData.of(XMaterial.ACACIA_PLANKS);
            } else if (nmsBlock == Blocks.DARK_OAK_FENCE) {
                fullBlock = BlockData.of(XMaterial.DARK_OAK_PLANKS);
            } else if (nmsBlock == Blocks.FENCE) {
                fullBlock = BlockData.of(XMaterial.OAK_PLANKS);
            } else {
                fullBlock = null;
            }
        } else if (nmsBlock instanceof BlockFenceGate) {
            if (nmsBlock == Blocks.SPRUCE_FENCE_GATE) {
                fullBlock = BlockData.of(XMaterial.SPRUCE_PLANKS);
            } else if (nmsBlock == Blocks.BIRCH_FENCE_GATE) {
                fullBlock = BlockData.of(XMaterial.BIRCH_PLANKS);
            } else if (nmsBlock == Blocks.JUNGLE_FENCE_GATE) {
                fullBlock = BlockData.of(XMaterial.JUNGLE_PLANKS);
            } else if (nmsBlock == Blocks.ACACIA_FENCE_GATE) {
                fullBlock = BlockData.of(XMaterial.ACACIA_PLANKS);
            } else if (nmsBlock == Blocks.DARK_OAK_FENCE_GATE) {
                fullBlock = BlockData.of(XMaterial.DARK_OAK_PLANKS);
            } else if (nmsBlock == Blocks.FENCE_GATE) {
                fullBlock = BlockData.of(XMaterial.OAK_PLANKS);
            } else {
                fullBlock = null;
            }
        } else if (nmsBlock instanceof BlockFire) {
            fullBlock = BlockData.of(XMaterial.NETHERRACK);
        } else if (nmsBlock instanceof BlockFlowerPot) {
            fullBlock = null;
        } else if (nmsBlock instanceof BlockFlowers) {
            fullBlock = BlockData.of(XMaterial.GRASS_BLOCK);
        } else if (nmsBlock instanceof BlockLongGrass) {
            fullBlock = BlockData.of(XMaterial.GRASS_BLOCK);
        } else if (nmsBlock instanceof BlockSkull) {
            fullBlock = null;
        } else if (nmsBlock instanceof BlockHopper) {
            fullBlock = BlockData.of(XMaterial.IRON_BLOCK);
        } else if (nmsBlock instanceof BlockLadder) {
            fullBlock = null;
        } else if (nmsBlock instanceof BlockLever) {
            BlockLever.EnumLeverPosition facing = blockData.get(BlockLever.FACING);
            if (facing.c() == EnumDirection.UP || facing.c() == EnumDirection.DOWN) {
                fullBlock = BlockData.of(XMaterial.COBBLESTONE);
            } else {
                fullBlock = null;
            }
        } else if (nmsBlock instanceof BlockWaterLily) {
            fullBlock = null;
        } else if (nmsBlock instanceof BlockMushroom) {
            fullBlock = BlockData.of(XMaterial.MYCELIUM);
        } else if (nmsBlock instanceof BlockNetherWart) {
            fullBlock = BlockData.of(XMaterial.SOUL_SAND);
        } else if (nmsBlock instanceof BlockThin) {
            if (nmsBlock == Blocks.GLASS_PANE) {
                fullBlock = BlockData.of(XMaterial.GLASS);
            } else if (nmsBlock == Blocks.STAINED_GLASS_PANE) {
                fullBlock = BlockData.of(Material.STAINED_GLASS, block.getData());
            } else if (nmsBlock == Blocks.IRON_BARS) {
                fullBlock = BlockData.of(XMaterial.IRON_BLOCK);
            } else {
                fullBlock = null;
            }
        } else if (nmsBlock instanceof BlockPressurePlateAbstract) {
            if (nmsBlock == Blocks.STONE_PRESSURE_PLATE) {
                fullBlock = BlockData.of(XMaterial.STONE);
            } else if (nmsBlock == Blocks.WOODEN_PRESSURE_PLATE) {
                fullBlock = BlockData.of(XMaterial.OAK_PLANKS);
            } else if (nmsBlock == Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                fullBlock = BlockData.of(XMaterial.IRON_BLOCK);
            } else if (nmsBlock == Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE) {
                fullBlock = BlockData.of(XMaterial.GOLD_BLOCK);
            } else {
                fullBlock = null;
            }
        } else if (nmsBlock instanceof BlockMinecartTrackAbstract) {
            fullBlock = BlockData.of(XMaterial.IRON_BLOCK);
        } else if (nmsBlock instanceof BlockRedstoneWire) {
            fullBlock = BlockData.of(XMaterial.RED_WOOL);
        } else if (nmsBlock instanceof BlockRedstoneComparator) {
            fullBlock = BlockData.of(XMaterial.RED_WOOL);
        } else if (nmsBlock instanceof BlockRepeater) {
            fullBlock = BlockData.of(XMaterial.RED_WOOL);
        } else if (nmsBlock instanceof BlockSapling) {
            fullBlock = null;
        } else if (nmsBlock instanceof BlockSign) {
            if (nmsBlock == Blocks.STANDING_SIGN) {
                fullBlock = BlockData.of(XMaterial.OAK_PLANKS);
            } else {
                fullBlock = null;
            }
        } else if (nmsBlock instanceof BlockStepAbstract) {
            if (nmsBlock instanceof BlockDoubleStepAbstract) { // Stone Slab
                BlockDoubleStepAbstract.EnumStoneSlabVariant variant = blockData.get(BlockDoubleStepAbstract.VARIANT);
                switch (variant) {
                    case STONE:
                        fullBlock = BlockData.of(Material.DOUBLE_STEP, (byte) 8);
                        break;
                    case SAND:
                        fullBlock = BlockData.of(XMaterial.SANDSTONE);
                        break;
                    case COBBLESTONE:
                        fullBlock = BlockData.of(XMaterial.COBBLESTONE);
                        break;
                    case BRICK:
                        fullBlock = BlockData.of(XMaterial.BRICK);
                        break;
                    case SMOOTHBRICK:
                        fullBlock = BlockData.of(XMaterial.STONE_BRICKS);
                        break;
                    case NETHERBRICK:
                        fullBlock = BlockData.of(XMaterial.NETHER_BRICK);
                        break;
                    case QUARTZ:
                        fullBlock = BlockData.of(XMaterial.QUARTZ_BLOCK);
                        break;
                    default:
                        fullBlock = null;
                        break;
                }
            } else if (nmsBlock instanceof BlockWoodenStep) { // Wood Slab
                BlockWood.EnumLogVariant variant = blockData.get(BlockWoodenStep.VARIANT);
                fullBlock = BlockData.of(Material.WOOD, (byte) variant.a());
            } else if (nmsBlock instanceof BlockPurpurSlab) { // Purpur Slab
                fullBlock = BlockData.of(XMaterial.PURPUR_BLOCK);
            } else if (nmsBlock instanceof BlockDoubleStoneStepAbstract) { // Red Sandstone Slab
                fullBlock = BlockData.of(XMaterial.RED_SANDSTONE);
            } else {
                fullBlock = null;
            }
        } else if (nmsBlock instanceof BlockSnow) {
            fullBlock = BlockData.of(XMaterial.SNOW_BLOCK);
        } else if (nmsBlock instanceof BlockStairs) {
            if (nmsBlock == Blocks.OAK_STAIRS) {
                fullBlock = BlockData.of(XMaterial.OAK_PLANKS);
            } else if (nmsBlock == Blocks.STONE_STAIRS) {
                fullBlock = BlockData.of(XMaterial.COBBLESTONE);
            } else if (nmsBlock == Blocks.BRICK_STAIRS) {
                fullBlock = BlockData.of(XMaterial.BRICK);
            } else if (nmsBlock == Blocks.STONE_BRICK_STAIRS) {
                fullBlock = BlockData.of(XMaterial.STONE_BRICKS);
            } else if (nmsBlock == Blocks.NETHER_BRICK_STAIRS) {
                fullBlock = BlockData.of(XMaterial.NETHER_BRICK);
            } else if (nmsBlock == Blocks.SANDSTONE_STAIRS) {
                fullBlock = BlockData.of(XMaterial.SANDSTONE);
            } else if (nmsBlock == Blocks.SPRUCE_STAIRS) {
                fullBlock = BlockData.of(XMaterial.SPRUCE_PLANKS);
            } else if (nmsBlock == Blocks.BIRCH_STAIRS) {
                fullBlock = BlockData.of(XMaterial.BIRCH_PLANKS);
            } else if (nmsBlock == Blocks.JUNGLE_STAIRS) {
                fullBlock = BlockData.of(XMaterial.JUNGLE_PLANKS);
            } else if (nmsBlock == Blocks.QUARTZ_STAIRS) {
                fullBlock = BlockData.of(XMaterial.QUARTZ_BLOCK);
            } else if (nmsBlock == Blocks.ACACIA_STAIRS) {
                fullBlock = BlockData.of(XMaterial.ACACIA_PLANKS);
            } else if (nmsBlock == Blocks.DARK_OAK_STAIRS) {
                fullBlock = BlockData.of(XMaterial.DARK_OAK_PLANKS);
            } else if (nmsBlock == Blocks.RED_SANDSTONE_STAIRS) {
                fullBlock = BlockData.of(XMaterial.RED_SANDSTONE);
            } else if (nmsBlock == Blocks.PURPUR_STAIRS) {
                fullBlock = BlockData.of(XMaterial.PURPUR_BLOCK);
            } else {
                fullBlock = null;
            }
        } else if (nmsBlock instanceof BlockStem) {
            fullBlock = BlockData.of(XMaterial.FARMLAND);
        } else if (nmsBlock instanceof BlockReed) {
            fullBlock = null;
        } else if (nmsBlock instanceof BlockTorch) {
            if (nmsBlock instanceof BlockRedstoneTorch) {
                fullBlock = BlockData.of(XMaterial.RED_WOOL);
            } else {
                fullBlock = BlockData.of(XMaterial.OAK_PLANKS);
            }
        } else if (nmsBlock instanceof BlockTrapdoor) {
            boolean open = blockData.get(BlockTrapdoor.OPEN);
            BlockTrapdoor.EnumTrapdoorHalf half = blockData.get(BlockTrapdoor.HALF);
            if (!open && half == BlockTrapdoor.EnumTrapdoorHalf.BOTTOM) {
                if (nmsBlock == Blocks.TRAPDOOR) {
                    fullBlock = BlockData.of(XMaterial.OAK_PLANKS);
                } else if (nmsBlock == Blocks.IRON_TRAPDOOR) {
                    fullBlock = BlockData.of(XMaterial.IRON_BLOCK);
                } else {
                    fullBlock = null;
                }
            } else {
                fullBlock = null;
            }
        } else if (nmsBlock instanceof BlockTripwire) {
            fullBlock = BlockData.of(XMaterial.WHITE_WOOL);
        } else if (nmsBlock instanceof BlockTripwireHook) {
            fullBlock = null;
        } else if (nmsBlock instanceof BlockVine) {
            fullBlock = null;
        } else if (nmsBlock instanceof BlockCobbleWall) {
            BlockCobbleWall.EnumCobbleVariant variant = blockData.get(BlockCobbleWall.VARIANT);
            switch (variant) {
                case NORMAL:
                    fullBlock = BlockData.of(XMaterial.COBBLESTONE);
                    break;
                case MOSSY:
                    fullBlock = BlockData.of(XMaterial.MOSSY_COBBLESTONE);
                    break;
                default:
                    fullBlock = null;
                    break;
            }
        } else if (nmsBlock instanceof BlockFluids) {
            if (nmsBlock == Blocks.WATER || nmsBlock == Blocks.FLOWING_WATER) {
                fullBlock = BlockData.of(XMaterial.LIGHT_BLUE_WOOL);
            } else if (nmsBlock == Blocks.LAVA || nmsBlock == Blocks.FLOWING_LAVA) {
                fullBlock = BlockData.of(XMaterial.ORANGE_WOOL);
            } else {
                fullBlock = null;
            }
        } else {
            return this.getBlockDataAt(location);
        }

        return fullBlock != null ? fullBlock : BlockData.of(XMaterial.AIR);
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
