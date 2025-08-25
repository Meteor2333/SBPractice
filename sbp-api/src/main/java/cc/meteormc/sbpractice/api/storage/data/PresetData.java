package cc.meteormc.sbpractice.api.storage.data;

import com.cryptomorin.xseries.XMaterial;
import lombok.Data;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class PresetData {
    private final String name;
    private final XMaterial icon;
    private final List<BlockData> blocks;
    private final File file;

    public void save() {
        try {
            CompoundTag compound = new CompoundTag();
            ListTag<CompoundTag> blocksTag = new ListTag<>(CompoundTag.class);
            for (BlockData block : this.blocks) {
                CompoundTag tag = new CompoundTag();
                tag.putString("type", block.getType().name());
                tag.putByte("data", block.getData().getData());
                if (block.getBlockEntity() != null) {
                    tag.put("entity", block.getBlockEntity());
                }
                blocksTag.add(tag);
            }

            compound.putString("name", this.name);
            compound.putString("icon", this.icon.name());
            compound.put("blocks", blocksTag);
            NBTUtil.write(compound, this.file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static PresetData load(File file) {
        try {
            CompoundTag compound = (CompoundTag) NBTUtil.read(file).getTag();
            String name = compound.getString("name");
            XMaterial icon = XMaterial.matchXMaterial(compound.getString("icon")).orElse(XMaterial.BEDROCK);
            List<BlockData> blocks = new ArrayList<>();

            ListTag<CompoundTag> blocksTag = compound.getListTag("blocks").asCompoundTagList();
            for (CompoundTag blockTag : blocksTag) {
                Material type = XMaterial.matchXMaterial(blockTag.getString("type"))
                        .orElse(XMaterial.AIR)
                        .or(XMaterial.AIR)
                        .get();
                assert type != null;
                byte data = blockTag.getByte("data");
                CompoundTag entity = null;
                if (blockTag.containsKey("entity")) {
                    entity = blockTag.getCompoundTag("entity");
                }

                blocks.add(BlockData.of(type, data, entity));
            }
            return new PresetData(name, icon, blocks, file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
