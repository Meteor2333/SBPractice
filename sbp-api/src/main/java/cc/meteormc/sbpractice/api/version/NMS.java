package cc.meteormc.sbpractice.api.version;

import com.cryptomorin.xseries.XMaterial;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

public abstract class NMS {
    protected final Plugin plugin;

    protected NMS(Plugin plugin) {
        this.plugin = plugin;
    }

    public abstract void registerCommand(Command command);

    public abstract void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut);

    public abstract void sendActionBar(Player player, String message);

    public abstract void setUnbreakable(ItemMeta itemMeta);

    public abstract ItemStack setItemTag(ItemStack itemStack, String key, String value);

    public abstract String getItemTag(ItemStack itemStack, String key);

    public abstract boolean hasItemTag(ItemStack itemStack, String key);

    public abstract ItemStack removeItemTag(ItemStack itemStack, String key);

    public abstract void hidePlayer(Player player);

    public abstract void showPlayer(Player player);

    public abstract void fixOtherPlayerTab(Player player);

    public abstract BlockState createBlockState(Material material, String data);

    public abstract BlockState setBlockStateLocation(BlockState state, Location location);

    public abstract BlockState setBlockStateData(BlockState state, MaterialData data);

    public abstract String getDataByBlockState(BlockState state);

    public abstract BlockState getBlockState(Location location);

    public abstract boolean isSimilarBlockState(BlockState state1, BlockState state2);

    public abstract CompoundTag getTileEntityData(Block block);

    public abstract void setTileEntityData(Block block, CompoundTag tag);

    public abstract ItemStack getItemByBlock(Material material, byte data);

    public void setBlock(Block block, XMaterial material, byte data) {
        Material type = material.or(XMaterial.AIR).parseMaterial();
        assert type != null;
        if (XMaterial.supports(13)) block.setType(type, false);
        else block.setTypeIdAndData(type.getId(), data, false);
    }

    public void setBlock(Block block, int material, byte data, CompoundTag tileEntity) {
        Material type = XMaterial.values()[material].or(XMaterial.AIR).parseMaterial();
        assert type != null;
        if (XMaterial.supports(13)) block.setType(type, false);
        else block.setTypeIdAndData(type.getId(), data, false);
        if (tileEntity != null) this.setTileEntityData(block, tileEntity);
    }
}
