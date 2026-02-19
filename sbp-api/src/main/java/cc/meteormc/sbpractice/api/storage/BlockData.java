package cc.meteormc.sbpractice.api.storage;

import com.cryptomorin.xseries.XMaterial;
import lombok.Data;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

@Data
public class BlockData {
    private final Material type;
    private final MaterialData data;
    private final CompoundTag blockEntity;

    public static BlockData of(XMaterial type) {
        return of(type, null);
    }

    public static BlockData of(XMaterial type, CompoundTag blockEntity) {
        return of(type.get(), type.getData(), blockEntity);
    }

    public static BlockData of(Material type) {
        return of(type, (byte) 0);
    }

    public static BlockData of(Material type, byte data) {
        return of(type, data, null);
    }

    public static BlockData of(Material type, byte data, CompoundTag blockEntity) {
        return new BlockData(type, new MaterialData(type, data), blockEntity);
    }
}
