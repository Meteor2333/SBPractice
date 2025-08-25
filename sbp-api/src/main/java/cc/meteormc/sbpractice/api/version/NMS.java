package cc.meteormc.sbpractice.api.version;

import cc.meteormc.sbpractice.api.storage.data.BlockData;
import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.Setter;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Stairs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class NMS {
    /* --------------------- Command --------------------- */

    public abstract void registerCommand(@NotNull Command command);

    /* --------------------------------------------------- */

    /* ---------------------- Item ----------------------- */

    public abstract void setItemUnbreakable(@NotNull ItemMeta itemMeta);

    public abstract @NotNull ItemStack setItemTag(@NotNull ItemStack itemStack, @NotNull String key, @NotNull String value);

    public abstract @Nullable String getItemTag(@NotNull ItemStack itemStack, @NotNull String key);

    public abstract boolean hasItemTag(@NotNull ItemStack itemStack, @NotNull String key);

    public abstract @NotNull ItemStack removeItemTag(@NotNull ItemStack itemStack, @NotNull String key);

    public abstract @NotNull ItemStack getItemByBlock(Material material, byte data);

    /* --------------------------------------------------- */

    /* --------------------- Player ---------------------- */

    public abstract void hidePlayer(@NotNull Player player);

    public abstract void showPlayer(@NotNull Player player);

    /* --------------------------------------------------- */

    /* ---------------------- Block ---------------------- */

    public abstract @NotNull BlockData getBlockDataAt(@NotNull Location location);

    public abstract boolean isSimilarBlock(@NotNull BlockData b1, @NotNull BlockData b2);

    public abstract boolean toFullBlock(@NotNull Block block);

    public abstract @Nullable CompoundTag getBlockEntityNBT(@NotNull Block block);

    public abstract void updateBlockEntityNBT(@NotNull Block block, @NotNull CompoundTag tag);

    public void setBlock(@NotNull Location location, @NotNull BlockData block) {
        Block b = location.getBlock();
        Material type = block.getType();
        if (XMaterial.supports(13)) b.setType(type, false);
        else b.setTypeIdAndData(type.getId(), block.getData().getData(), false);
        CompoundTag blockEntity = block.getBlockEntity();
        if (blockEntity != null) this.updateBlockEntityNBT(b, blockEntity);
    }

    protected @Nullable CompoundTag filterBlockEntityNBT(@Nullable CompoundTag tag) {
        if (tag == null) return tag;
        tag = tag.clone();
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");
        return tag;
    }

    @Setter
    @Getter
    protected static class FixedStairs extends Stairs {
        private StairShape shape = StairShape.STRAIGHT;

        public FixedStairs(@NotNull MaterialData data) {
            super(data.getItemType(), data.getData());
        }

        public enum StairShape {
            STRAIGHT,
            INNER_LEFT,
            INNER_RIGHT,
            OUTER_LEFT,
            OUTER_RIGHT
        }
    }

    /* --------------------------------------------------- */
}
