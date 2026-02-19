package cc.meteormc.sbpractice.gui;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.helper.ItemBuilder;
import cc.meteormc.sbpractice.api.storage.PlayerData;
import cc.meteormc.sbpractice.config.Message;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class SettingsGui extends FastInv {
    private final Player player;

    public SettingsGui(Player player) {
        super(45, Message.GUI.SETTINGS.TITLE.parseLine(player));
        this.player = player;

        this.refresh();
    }

    private void refresh() {
        PlayerData.getData(this.player).ifPresent(data -> {
            String state;
            int height = data.getSettings().getHighjumpHeight();
            if (height <= 0) {
                state = Message.GUI.SETTINGS.HIGHJUMP.STATE.DISABLED.parseLine(this.player);
            } else {
                state = Message.GUI.SETTINGS.HIGHJUMP.STATE.ENABLED.parseLine(this.player, height);
            }

            this.setItem(
                    22,
                    new ItemBuilder(XMaterial.POTION)
                            .setDisplayName(Message.GUI.SETTINGS.HIGHJUMP.ITEM.NAME.parseLine(this.player))
                            .setLore(Message.GUI.SETTINGS.HIGHJUMP.ITEM.LORE.parse(this.player, state))
                            .setAmount(Math.max(1, height))
                            .build(),
                    event -> this.switchHeight(event.getClick())
            );
        });
    }

    private void switchHeight(ClickType type) {
        PlayerData.getData(this.player).ifPresent(data -> {
            int height = data.getSettings().getHighjumpHeight();
            switch (type) {
                case LEFT:
                case SHIFT_LEFT:
                    height++;
                    break;
                case RIGHT:
                case SHIFT_RIGHT:
                    height--;
                    break;
            }

            int maxHeight;
            Island island = data.getIsland();
            if (island != null) maxHeight = island.getBuildArea().getHeight() + 3;
            else maxHeight = 10;

            if (height < 0) height = maxHeight;
            if (height > maxHeight) height = 0;

            data.getSettings().setHighjumpHeight(height);
            this.refresh();
            XSound.BLOCK_NOTE_BLOCK_HAT.play(player);
        });
    }
}
