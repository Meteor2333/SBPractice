package com.meteor.SBPractice.Utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

//By EasyPlugin
public class ItemStackBuilder {
    ItemStack item;
    public ItemStackBuilder(Material type) {
        this(type, 1);
    }

    public ItemStackBuilder(Material type, int amount) {
        this(type, amount, (short) 0);
    }

    public ItemStackBuilder(Material type, int amount, short data) {
        this.item = new ItemStack(type, amount, data);
    }

    public ItemStack toItemStack() {
        return this.item;
    }

    public ItemStackBuilder setDisplayName(String name) {
        ItemMeta im = this.item.getItemMeta();
        if (im != null) {
            im.setDisplayName(name);
            this.item.setItemMeta(im);
        }
        return this;
    }
}
