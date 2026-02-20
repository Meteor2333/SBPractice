package cc.meteormc.sbpractice.gui;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.helper.ItemBuilder;
import cc.meteormc.sbpractice.api.storage.BlockData;
import cc.meteormc.sbpractice.api.storage.PlayerData;
import cc.meteormc.sbpractice.api.storage.PresetData;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.config.Message;
import cc.meteormc.sbpractice.feature.session.BuildPresetSession;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import fr.mrmicky.fastinv.PaginatedFastInv;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PresetGui extends PaginatedFastInv {
    private String filtered = null;
    private final Player player;
    private final Island island;

    private static final EnumSet<Character.UnicodeScript> CJK = EnumSet.of(
            Character.UnicodeScript.HAN,
            Character.UnicodeScript.HIRAGANA,
            Character.UnicodeScript.KATAKANA,
            Character.UnicodeScript.HANGUL
    );

    public PresetGui(Player player, Island island) {
        super(54, Message.GUI.PRESET.TITLE.parseLine(player));
        this.player = player;
        this.island = island;

        this.previousPageItem(
                45,
                new ItemBuilder(XMaterial.ARROW)
                        .setDisplayName(Message.GUI.PREVIOUS_PAGE.parseLine(player))
                        .build()
        );

        this.refresh();

        this.setItem(
                49,
                new ItemBuilder(XMaterial.BARRIER)
                        .setDisplayName(Message.GUI.CLOSE.parseLine(player))
                        .build(),
                event -> event.getWhoClicked().closeInventory()
        );

        this.setItem(
                50,
                new ItemBuilder(XMaterial.OAK_SIGN)
                        .setDisplayName(Message.GUI.PRESET.SAVE.ITEM.NAME.parseLine(player))
                        .setLore(Message.GUI.PRESET.SAVE.ITEM.LORE.parse(player))
                        .build(),
                event -> {
                    Player who = (Player) event.getWhoClicked();
                    PlayerData.getData(who).ifPresent(data -> {
                        int size = data.getPresets().getOrDefault(island.getZone(), Collections.emptyList()).size();
                        if (size < MainConfig.MAX_PRESETS_LIMIT.resolve()) {
                            new BuildPresetSession(this.island, false).start();
                        } else {
                            XSound.ENTITY_VILLAGER_NO.play(who);
                            Message.BASIC.PRESET_FULL.sendTo(who);
                        }
                    });
                }
        );

        if (player.isOp()) {
            this.setItem(
                    51,
                    new ItemBuilder(XMaterial.BOOK)
                            .setDisplayName(Message.GUI.PRESET.SAVE.ITEM_GLOBAL.NAME.parseLine(player))
                            .setLore(Message.GUI.PRESET.SAVE.ITEM_GLOBAL.LORE.parse(player))
                            .build(),
                    event -> {
                        new BuildPresetSession(this.island, true).start();
                    }
            );
        }

        this.nextPageItem(
                53,
                new ItemBuilder(XMaterial.ARROW)
                        .setDisplayName(Message.GUI.NEXT_PAGE.parseLine(player))
                        .build()
        );
    }

    private void refresh() {
        this.setItem(
                48,
                new ItemBuilder(XMaterial.HOPPER)
                        .setDisplayName(Message.GUI.PRESET.FILTER.ITEM.NAME.parseLine(player))
                        .setLore(Message.GUI.PRESET.FILTER.ITEM.LORE.parse(player, filtered != null ? filtered : ""))
                        .build(),
                event -> {
                    if (event.getClick().isRightClick()) {
                        this.filtered = null;
                        this.refresh();
                    } else {
                        player.closeInventory();
                        Main.get().getNms().openSign(
                                player,
                                new String[]{"", "^^^^^^", Message.GUI.PRESET.FILTER.QUERY.parseLine(player), ""},
                                lines -> {
                                    this.filtered = lines[0];
                                    this.refresh();
                                    this.open(player);
                                }
                        );
                    }
                }
        );

        this.clearContent();
        PlayerData.getData(player)
                .map(PlayerData::getPresets)
                .map(presets -> presets.get(island.getZone()))
                // First, load the local preset
                .map(presets -> {
                    return filterAndSort(presets, this.filtered)
                            .thenApply(result -> this.executeAction(false, result))
                            .thenAccept(result -> {
                                if (result.isEmpty()) return;
                                int empty = 9 - Math.max(1, result.size() % 9) + 9;
                                for (int i = 0; i < empty; i++) {
                                    this.addContent((ItemStack) null);
                                }
                            });
                })
                .orElse(CompletableFuture.completedFuture(null))
                // Next, load the global preset
                .thenCompose(v -> filterAndSort(island.getZone().getPresets(), this.filtered))
                .thenAccept(result -> this.executeAction(true, result))
                .thenAcceptAsync(v -> this.openPage(1));
    }

    private List<PresetData> executeAction(boolean isGlobal, List<PresetData> presets) {
        for (PresetData preset : presets) {
            List<String> description;
            long count = preset.getBlocks()
                    .stream()
                    .map(BlockData::getType)
                    .filter(type -> type != Material.AIR)
                    .count();
            if (isGlobal && !player.isOp())
                description = Message.GUI.PRESET.DESCRIPTION_NO_PERMISSION.parse(player, count);
            else description = Message.GUI.PRESET.DESCRIPTION.parse(player, count);
            ItemBuilder item = new ItemBuilder(preset.getIcon())
                    .setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + preset.getName())
                    .addFlag(ItemFlag.values())
                    .setLore(description);

            Consumer<InventoryClickEvent> handler = new Consumer<InventoryClickEvent>() {
                private final PresetData presetData = preset;
                private final boolean global = isGlobal;
                private final boolean canRemove = !global || player.isOp();

                @Override
                public void accept(InventoryClickEvent event) {
                    Player who = (Player) event.getWhoClicked();
                    if (canRemove && event.getClick().isRightClick()) {
                        PlayerData.getData(who).ifPresent(data -> {
                            CompletableFuture.runAsync(() -> presetData.getFile().delete())
                                    .thenAccept(v -> {
                                        if (global) {
                                            island.getZone().getPresets().remove(presetData);
                                        } else {
                                            data.getPresets().getOrDefault(island.getZone(), Collections.emptyList()).remove(presetData);
                                        }
                                        XSound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR.play(player, 1F, 0F);
                                    })
                                    .thenAccept(v -> refresh())
                                    .exceptionally(e -> {
                                        e.printStackTrace();
                                        return null;
                                    });
                        });
                        return;
                    }

                    player.closeInventory();
                    island.applyPreset(this.presetData);
                    XSound.ENTITY_PLAYER_LEVELUP.play(player, 1F, 2F);
                    Message.OPERATION.PRESET.sendTo(player, preset.getName());
                }
            };

            this.addContent(item.build(), handler);
        }
        return presets;
    }

    private static CompletableFuture<List<PresetData>> filterAndSort(List<PresetData> presets, String filtered) {
        if (filtered == null || filtered.isEmpty()) {
            return CompletableFuture.completedFuture(presets);
        }

        return CompletableFuture.supplyAsync(() -> {
            String query = filtered.toLowerCase(Locale.ROOT);
            Map<PresetData, Integer> scores = new HashMap<>();
            for (PresetData preset : presets) {
                String name = preset.getName().toLowerCase(Locale.ROOT);
                if (substringMatch(name, query)) {
                    scores.put(preset, Integer.MAX_VALUE);
                } else if (abbreviationMatch(name, query)) {
                    scores.put(preset, 1);
                } else {
                    int dist = fuzzyMatch(name, query);
                    if (dist <= 3) {
                        scores.put(preset, -dist);
                    }
                }
            }

            return scores.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }).thenApplyAsync( // Ensure subsequent operations run on the main thread
                Function.identity(),
                runnable -> Bukkit.getScheduler().runTask(Main.get(), runnable)
        );
    }

    private static boolean substringMatch(String item, String query) {
        return item.contains(query);
    }

    private static boolean abbreviationMatch(String item, String query) {
        StringBuilder sb = new StringBuilder();
        for (String word : item.split("[\\s_-]+")) {
            if (word.isEmpty()) continue;
            int cp = word.codePointAt(0);
            if (CJK.contains(Character.UnicodeScript.of(cp)) || Character.isLetterOrDigit(cp)) {
                sb.appendCodePoint(cp);
            }
        }

        return sb.toString().contains(query);
    }

    private static int fuzzyMatch(String item, String query) {
        int[] s1 = item.codePoints().toArray();
        int[] s2 = query.codePoints().toArray();

        int[][] dp = new int[s1.length + 1][s2.length + 1];
        for (int i = 0; i <= s1.length; i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length; j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length; i++) {
            for (int j = 1; j <= s2.length; j++) {
                if (s1[i - 1] == s2[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                }
            }
        }
        return dp[s1.length][s2.length];
    }
}
