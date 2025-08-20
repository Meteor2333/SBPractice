package cc.meteormc.sbpractice.gui;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.storage.player.PlayerData;
import cc.meteormc.sbpractice.api.storage.preset.PresetData;
import cc.meteormc.sbpractice.api.util.ItemBuilder;
import cc.meteormc.sbpractice.arena.session.PresetBuildSession;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.config.Messages;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import de.rapha149.signgui.SignGUI;
import fr.mrmicky.fastinv.PaginatedFastInv;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PresetGui extends PaginatedFastInv {
    private String filtered = null;
    private final Player player;
    private final Island island;
    private final List<PresetData> presets;

    private static final EnumSet<Character.UnicodeScript> CJK = EnumSet.of(
            Character.UnicodeScript.HAN,
            Character.UnicodeScript.HIRAGANA,
            Character.UnicodeScript.KATAKANA,
            Character.UnicodeScript.HANGUL
    );

    public PresetGui(Player player, Island island) {
        super(54, Messages.GUI_PRESET_TITLE.getMessage());
        this.player = player;
        this.island = island;
        this.presets = new ArrayList<>(island.getArena().getPresets());

        this.previousPageItem(
                45,
                new ItemBuilder(XMaterial.ARROW)
                        .setDisplayName(Messages.GUI_PREVIOUS_PAGE.getMessage())
                        .build()
        );

        this.refreshFilter();

        this.setItem(
                49,
                new ItemBuilder(XMaterial.BARRIER)
                        .setDisplayName(Messages.GUI_CLOSE.getMessage())
                        .build(),
                event -> event.getWhoClicked().closeInventory()
        );

        this.setItem(
                50,
                new ItemBuilder(XMaterial.OAK_SIGN)
                        .setDisplayName(Messages.GUI_PRESET_SAVE_NAME.getMessage())
                        .setLore(Messages.GUI_PRESET_SAVE_LORE.getMessageList())
                        .build(),
                event -> {
                    Player who = (Player) event.getWhoClicked();
                    PlayerData.getData(who).ifPresent(data -> {
                        int size = data.getPresets().getOrDefault(island.getArena(), Collections.emptyList()).size();
                        if (size < MainConfig.MAX_PRESETS_LIMIT.getInt()) {
                            new PresetBuildSession(this.island, false).start();
                        } else {
                            XSound.ENTITY_ENDERMAN_TELEPORT.play(who);
                            who.sendMessage(Messages.PREFIX.getMessage() + Messages.PRESET_FULL.getMessage());
                        }
                    });
                }
        );

        if (player.isOp()) {
            this.setItem(
                    51,
                    new ItemBuilder(XMaterial.BOOK)
                            .setDisplayName(Messages.GUI_PRESET_SAVE_GLOBAL_NAME.getMessage())
                            .setLore(Messages.GUI_PRESET_SAVE_GLOBAL_LORE.getMessageList())
                            .build(),
                    event -> {
                        new PresetBuildSession(this.island, true).start();
                    }
            );
        }

        this.nextPageItem(
                53,
                new ItemBuilder(XMaterial.ARROW)
                        .setDisplayName(Messages.GUI_NEXT_PAGE.getMessage())
                        .build()
        );
    }

    private void refreshFilter() {
        this.setItem(
                48,
                new ItemBuilder(XMaterial.HOPPER)
                        .setDisplayName(Messages.GUI_PRESET_FILTER_NAME.getMessage())
                        .setLore(
                                Messages.GUI_PRESET_FILTER_LORE.getMessageList()
                                        .stream()
                                        .map(line -> {
                                            return line.replace("%filtered%", filtered != null ? filtered : "");
                                        })
                                        .collect(Collectors.toList()))
                        .build(),
                event -> {
                    if (event.getClick().isRightClick()) {
                        this.filtered = null;
                        this.refreshFilter();
                    } else {
                        player.closeInventory();
                        SignGUI.builder()
                                .setLine(1, "^^^^^")
                                .setLine(2, Messages.GUI_PRESET_FILTER_QUERY.getMessage())
                                .setHandler((p, result) -> {
                                    this.filtered = result.getLineWithoutColor(0);
                                    this.refreshFilter();
                                    this.open(p);
                                    return Collections.emptyList();
                                })
                                .build()
                                .open(player);
                    }
                }
        );

        this.clearContent();
        Consumer<List<PresetData>> action = presets -> {
            for (final PresetData preset : presets) {
                this.addContent(
                        buildPresetItem(preset),
                        event -> {
                            Player player = (Player) event.getWhoClicked();
                            player.closeInventory();
                            island.applyPreset(preset);
                            XSound.ENTITY_PLAYER_LEVELUP.play(player, 1L, 2L);
                            player.sendMessage(Messages.PREFIX.getMessage() + Messages.PRESET_APPLIED.getMessage().replace("%preset%", preset.getName()));
                        }
                );
            }
        };

        PlayerData.getData(player)
                .map(PlayerData::getPresets)
                .map(presets -> presets.get(island.getArena()))
                .map(presets -> filterAndSort(presets, this.filtered).thenAccept(result -> {
                    action.accept(result);
                    if (result.isEmpty()) return;
                    int empty = 9 - Math.max(1, result.size() % 9) + 9;
                    for (int i = 0; i < empty; i++) {
                        this.addContent((ItemStack) null);
                    }
                }))
                .orElse(CompletableFuture.completedFuture(null))
                .thenCompose(v -> filterAndSort(this.presets, this.filtered))
                .thenAccept(action)
                .thenAccept(v -> this.openPage(1));
    }

    private static CompletableFuture<List<PresetData>> filterAndSort(List<PresetData> presets, String filtered) {
        if (filtered == null) {
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
        });
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

    private static ItemStack buildPresetItem(PresetData preset) {
        return new ItemBuilder(preset.getIcon())
                .setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + preset.getName())
                .setLore(
                        Messages.GUI_PRESET_DESCRIPTION.getMessageList().stream()
                                .map(line -> line.replace("%blocks%", String.valueOf(preset.getBlocks().size())))
                                .collect(Collectors.toList())
                )
                .build();
    }
}
