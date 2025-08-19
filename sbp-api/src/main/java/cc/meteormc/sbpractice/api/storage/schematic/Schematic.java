package cc.meteormc.sbpractice.api.storage.schematic;

import cc.meteormc.sbpractice.api.util.Region;
import cc.meteormc.sbpractice.api.version.NMS;
import com.cryptomorin.xseries.XMaterial;
import lombok.RequiredArgsConstructor;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;

@RequiredArgsConstructor
public class Schematic {
    private final short width;
    private final short height;
    private final short length;
    private final int[] blocks;
    private final byte[] blockData;
    private final ListTag<CompoundTag> tileEntities;

    private static NMS nms;

    public static void init(NMS nms) {
        Schematic.nms = nms;
    }

    public void pasteSchematic(Location location) {
        int index = 0;
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                for (int z = 0; z < this.length; z++) {
                    Block block = location.clone().add(new Vector(x, y, z)).getBlock();
                    nms.setBlock(block, this.blocks[index], this.blockData[index], this.tileEntities.get(index));
                    index++;
                }
            }
        }
    }

    public static void save(Vector reference, Region region, File file) throws IOException {
        CompoundTag compound = new CompoundTag();
        int width = region.getWidth();
        int height = region.getHeight();
        int length = region.getLength();

        compound.putShort("Width", (short) width);
        compound.putShort("Height", (short) height);
        compound.putShort("Length", (short) length);

        int[] blocks = new int[region.getBlockCount()];
        byte[] blockData = new byte[region.getBlockCount()];
        ListTag<CompoundTag> tileEntities = new ListTag<>(CompoundTag.class);

        int index = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    Block block = reference.clone().add(new Vector(x, y, z)).toLocation(region.getWorld()).getBlock();
                    Material type = block.getType();
                    byte data = block.getData();

                    blocks[index] = XMaterial.matchXMaterial(type.getId(), data).orElse(XMaterial.AIR).ordinal();
                    blockData[index] = data;
                    tileEntities.add(nms.getTileEntityData(block));
                    index++;
                }
            }
        }

        compound.putIntArray("Blocks", blocks);
        compound.putByteArray("Data", blockData);
        compound.put("TileEntities", tileEntities);
        NBTUtil.write(compound, file);
    }

    public static Schematic load(File file) throws IOException {
        CompoundTag compound = (CompoundTag) NBTUtil.read(file).getTag();
        short width = compound.getShort("Width");
        short height = compound.getShort("Height");
        short length = compound.getShort("Length");
        int[] blocks = compound.getIntArray("Blocks");
        byte[] blockData = compound.getByteArray("Data");
        ListTag<CompoundTag> tileEntities = compound.getListTag("TileEntities").asCompoundTagList();
        return new Schematic(width, height, length, blocks, blockData, tileEntities);
    }
}
