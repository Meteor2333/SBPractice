package cc.meteormc.sbpractice.gui;

import cc.meteormc.sbpractice.api.util.ItemBuilder;
import cc.meteormc.sbpractice.arena.session.SetupSession;
import cc.meteormc.sbpractice.arena.setup.SetupType;
import com.cryptomorin.xseries.XMaterial;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class SetupArenaGui extends FastInv {
    private final SetupSession session;

    public SetupArenaGui(SetupSession session) {
        super(9, "● Setup Arena: " + session.getName());
        this.session = session;
    }

    @Override
    protected void onOpen(InventoryOpenEvent event) {
        this.update();
    }

    private void update() {
        for (final SetupType value : SetupType.values()) {
            this.setItem(
                    value.ordinal(),
                    new ItemBuilder(value.getIcon())
                            .setDisplayName(value.getHint())
                            .setLore(value.getState(this.session))
                            .build(),
                    event -> {
                        ClickType clickType = event.getClick();
                        if (clickType.isLeftClick()) {
                            SetupArenaGui.this.session.onLeftClickGui(value);
                        }
                        if (clickType.isRightClick()) {
                            SetupArenaGui.this.session.onRightClickGui(value);
                        }
                        update();
                    }
            );
        }

        this.setItem(
                8,
                new ItemBuilder(XMaterial.REDSTONE)
                        .setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Save")
                        .addLore(this.session.isComplete() ? ChatColor.GREEN + "Click to save! ✔" : ChatColor.RED + "Complete the setup first! ✘")
                        .build(),
                event -> {
                    if (session.isComplete()) {
                        session.close();
                        event.getWhoClicked().closeInventory();
                    }
                }
        );
    }
}
