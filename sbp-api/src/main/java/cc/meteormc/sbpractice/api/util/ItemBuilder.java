package cc.meteormc.sbpractice.api.util;


import cc.meteormc.sbpractice.api.version.NMS;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@SuppressWarnings("unused")
public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;

    private static NMS nms;

    public ItemBuilder(ItemStack is) {
        this.item = is.clone();
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(XMaterial material) {
        this(Objects.requireNonNull(material.parseItem()));
    }

    public static void init(NMS nms) {
        ItemBuilder.nms = nms;
    }

    public ItemStack build() {
        this.item.setItemMeta(this.meta);
        return this.item.clone();
    }

    public ItemBuilder setType(XMaterial material) {
        this.item.setType(material.parseMaterial());
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    public ItemBuilder setDisplayName(String name) {
        if (name != null) this.meta.setDisplayName(Utils.colorize(name));
        else this.meta.setDisplayName(null);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        this.meta.setLore(Utils.colorize(lore));
        return this;
    }

    public ItemBuilder setDurability(short durability) {
        this.item.setDurability(durability);
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        nms.setUnbreakable(this.meta);
        return this;
    }

    public XMaterial getType() {
        return XMaterial.matchXMaterial(this.item);
    }

    public int getAmount() {
        return this.item.getAmount();
    }

    public String getDisplayName() {
        return Optional.ofNullable(this.meta.getDisplayName()).orElse("");
    }

    public List<String> getLore() {
        return Optional.ofNullable(this.meta.getLore()).orElse(new ArrayList<>());
    }

    public short getDurability() {
        return this.item.getDurability();
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return Optional.ofNullable(this.meta.getEnchants()).orElse(new HashMap<>());
    }

    public boolean hasDisplayName() {
        return this.meta.hasDisplayName();
    }

    public boolean hasLore() {
        return this.meta.hasLore();
    }

    public boolean hasEnchantment(Enchantment enchantment) {
        return this.meta.hasEnchant(enchantment);
    }

    public boolean hasEnchantments() {
        return this.meta.hasEnchants();
    }

    public boolean hasFlag(ItemFlag flag) {
        return this.meta.hasItemFlag(flag);
    }

    public ItemBuilder addLore(String lore) {
        List<String> list = this.meta.getLore();
        if (list == null) list = new ArrayList<>();
        list.add(Utils.colorize(lore));
        this.meta.setLore(list);
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        this.item.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder addFlag(ItemFlag... flag) {
        this.meta.addItemFlags(flag);
        return this;
    }

    public ItemBuilder removeEnchantment(Enchantment enchantment) {
        this.item.removeEnchantment(enchantment);
        return this;
    }

    public ItemBuilder removeFlag(ItemFlag... flag) {
        this.meta.removeItemFlags(flag);
        return this;
    }
}
