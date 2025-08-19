package cc.meteormc.sbpractice.api.storage.preset;

import cc.meteormc.sbpractice.api.version.NMS;
import com.cryptomorin.xseries.XMaterial;
import lombok.Data;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import org.bukkit.block.BlockState;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class PresetData {
    private final String name;
    private final XMaterial icon;
    private final List<BlockState> blocks;

    private static NMS nms;

    public static void init(NMS nms) {
        PresetData.nms = nms;
    }

    public void save(File file) {
        try {
            CompoundTag compound = new CompoundTag();
            ListTag<CompoundTag> blocks = new ListTag<>(CompoundTag.class);
            for (BlockState block : this.blocks) {
                CompoundTag blockTag = new CompoundTag();
                blockTag.putString("type", XMaterial.matchXMaterial(block.getType()).name());
                blockTag.putString("data", nms.getDataByBlockState(block));
                blocks.add(blockTag);
            }

            compound.putString("name", this.name);
            compound.putString("icon", this.icon.name());
            compound.put("blocks", blocks);
            NBTUtil.write(compound, file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static PresetData load(File file) {
        try {
            CompoundTag compound = (CompoundTag) NBTUtil.read(file).getTag();
            String name = compound.getString("name");
            XMaterial icon = XMaterial.matchXMaterial(compound.getString("icon")).orElse(XMaterial.BEDROCK);
            List<BlockState> blocks = new ArrayList<>();
            ListTag<CompoundTag> blocksTag = compound.getListTag("blocks").asCompoundTagList();
            for (CompoundTag block : blocksTag) {
                XMaterial type = XMaterial.matchXMaterial(block.getString("type")).orElse(XMaterial.AIR);
                String data = block.getString("data");
                blocks.add(nms.createBlockState(type.or(XMaterial.AIR).parseMaterial(), data));
            }
            return new PresetData(name, icon, blocks);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
