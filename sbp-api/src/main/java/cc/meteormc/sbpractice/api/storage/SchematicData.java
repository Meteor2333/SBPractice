package cc.meteormc.sbpractice.api.storage;

import cc.meteormc.sbpractice.api.SBPracticeAPI;
import cc.meteormc.sbpractice.api.helper.Area;
import lombok.Data;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@Data
public class SchematicData {
    private final short width;
    private final short height;
    private final short length;
    private final int[] blocks;
    private final byte[] blockData;
    private final List<CompoundTag> tileEntities;

    public static SchematicData copy(World world, Area area) {
        short width = (short) area.getWidth();
        short height = (short) area.getHeight();
        short length = (short) area.getLength();
        int[] blocks = new int[area.getBlockCount()];
        byte[] blockData = new byte[area.getBlockCount()];
        List<CompoundTag> tileEntities = new ArrayList<>();
        int index = 0;
        for (Vector point : area.getPoints()) {
            Block block = point.toLocation(world).getBlock();
            blocks[index] = block.getType().ordinal();
            blockData[index] = block.getData();
            CompoundTag nbt = SBPracticeAPI.getInstance().getNms().getBlockEntityNBT(block);
            if (nbt != null) tileEntities.add(nbt);
            index++;
        }
        return new SchematicData(width, height, length, blocks, blockData, tileEntities);
    }

    public static SchematicData load(File file) throws IOException {
        CompoundTag compound = (CompoundTag) NBTUtil.read(file).getTag();
        short width = compound.getShort("Width");
        short height = compound.getShort("Height");
        short length = compound.getShort("Length");
        int[] blocks = compound.getIntArray("Blocks");
        byte[] blockData = compound.getByteArray("Data");
        List<CompoundTag> tileEntities = new ArrayList<>();
        for (CompoundTag tileEntity : compound.getListTag("TileEntities").asCompoundTagList()) {
            tileEntities.add(tileEntity);
        }
        return new SchematicData(width, height, length, blocks, blockData, tileEntities);
    }

    public void paste(Location location) {
        int index = 0;
        ListIterator<CompoundTag> iterator = this.tileEntities.listIterator();
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                for (int z = 0; z < this.length; z++) {
                    Location loc = location.clone().add(x, y, z);
                    CompoundTag entity = null;
                    if (iterator.hasNext()) {
                        CompoundTag next = iterator.next();
                        if (next.getInt("x") == loc.getX()
                                && next.getInt("y") == loc.getY()
                                && next.getInt("z") == loc.getZ()) {
                            entity = next;
                        } else {
                            iterator.previous();
                        }
                    }

                    SBPracticeAPI.getInstance().getNms().setBlock(
                            loc,
                            BlockData.of(
                                    Material.values()[this.blocks[index]],
                                    this.blockData[index],
                                    entity
                            )
                    );
                    index++;
                }
            }
        }
    }

    public void save(File file) throws IOException {
        CompoundTag compound = new CompoundTag();
        compound.putShort("Width", this.width);
        compound.putShort("Height", this.height);
        compound.putShort("Length", this.length);
        compound.putIntArray("Blocks", this.blocks);
        compound.putByteArray("Data", this.blockData);
        ListTag<CompoundTag> tileEntitiesTag = new ListTag<>(CompoundTag.class);
        tileEntitiesTag.addAll(this.tileEntities);
        compound.put("TileEntities", tileEntitiesTag);
        NBTUtil.write(compound, file);
    }
}
