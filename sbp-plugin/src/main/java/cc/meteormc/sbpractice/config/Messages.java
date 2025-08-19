package cc.meteormc.sbpractice.config;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.config.ConfigManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Messages {
    PREFIX("basic.prefix", "&8[&bSBPractice&8] "),
    CURRENT_TIME("basic.current-time", "&b&l%time%"),
    PLAYER_NOT_FOUND("basic.player-not-found", "&cPlayer not found!"),
    CANNOT_DO_THAT("basic.cannot-do-that", "&cYou can't do that!"),
    PERFECT_MATCH_TITLE("basic.perfect-match-title", "&a&lGreat Job"),
    PERFECT_MATCH_SUBTITLE("basic.perfect-match-subtitle", "&6&l%time%"),
    START_COUNTDOWN("basic.start-countdown", "&e&lSTART"),
    START_COUNTDOWN_NUMBER("basic.start-countdown-number", "&6&l%number%"),
    APPLY_PRESET("basic.apply-preset", "&aPreset applied: %preset%"),
    CLEAR_ITEM_NAME("basic.item.clear", "&9&lUse &f&lSnowball &9&lto clear building"),
    START_ITEM_NAME("basic.item.start", "&d&lUse &e&lEgg &d&lto start"),

    COMMAND_USAGE("command.command-usage", "&cUsage: %usage%"),
    MAIN_COMMAND_HELP("command.main-command-help", Arrays.asList(
            "&2▪ &7/&esbp adapt &8- &aAdapt to the current ground",
            "&2▪ &7/&esbp arena <arenaName> &8- &aSelect a arena",
            "&2▪ &7/&esbp caching &8- &aCache the current building",
            "&2▪ &7/&esbp clear &8- &aClear the current building",
            "&2▪ &7/&esbp highjump &8- &aToggle highjump state",
            "&2▪ &7/&esbp mode &8- &aToggle build mode",
            "&2▪ &7/&esbp preset [presetName] &8- &aSelect preset building",
            "&2▪ &7/&esbp start &8- &aStart the countdown",
            "&2▪ &7/&esbp view &8- &aView the cached building")),
    MULTIPLAYER_COMMAND_HELP("command.multiplayer-command-help", Arrays.asList(
            "&2▪ &7/&emp accept <player> &8- &aAccept a invite from a player",
            "&2▪ &7/&emp deny <player> &8- &aDeny a invite from a player",
            "&2▪ &7/&emp invite <player> &8- &aInvite another player to your island",
            "&2▪ &7/&emp join <player> &8- &aJoin another player's island",
            "&2▪ &7/&emp kick <player> &8- &aRemove a player from your island",
            "&2▪ &7/&emp leave &8- &aLeaves your current island")),
    NO_PERMISSION("command.no-permission", "&cYou don't have permission to do this!"),
    GROUND("command.ground", "&aThe ground has been adapted!"),
    RECORD("command.record", "&aThe building has been cached!"),
    CLEAR("command.clear", "&aThe building has been cleared!"),
    COUNTDOWN_CONTINUOUS_ENABLE("command.countdown-continuous.enable", "&aThe timer has started!"),
    COUNTDOWN_CONTINUOUS_DISABLE("command.countdown-continuous.disable", "&cThe timer has stopped!"),
    TOGGLE_BUILD_MODE_DEFAULT("command.build-mode.default", "&aSwitched to default mode!"),
    TOGGLE_BUILD_MODE_COUNTDOWN_ONCE("command.build-mode.countdown-once", "&aSwitched to once countdown mode!"),
    TOGGLE_BUILD_MODE_COUNTDOWN_CONTINUOUS("command.build-mode.countdown-continuous", "&aSwitched to continuous countdown mode!"),
    TOGGLE_HIGHJUMP_ENABLE("command.highjump.enable", "&aHighjump has been enabled!"),
    TOGGLE_HIGHJUMP_DISABLE("command.highjump.disable", "&cHigh jump has been disabled!"),
    VIEW_BUILDING("command.view-building", "&aViewing the building!"),

    GUI_PREVIOUS_PAGE("gui.previous-page-item", "&ePrevious page"),
    GUI_NEXT_PAGE("gui.next-page-item", "&eNext page"),
    GUI_CLOSE("gui.close-item", "&cClose"),
    GUI_PRESET_TITLE("gui.preset.title", "Select preset building"),
    GUI_PRESET_DESCRIPTION("gui.preset.description", Arrays.asList("Blocks: %blocks%", "", "&6Click to use this preset!")),
    GUI_PRESET_FILTER_NAME("gui.preset.filter.name", "&aSearch"),
    GUI_PRESET_FILTER_LORE("gui.preset.filter.lore", Arrays.asList("&7Find preset by name.", "", "&7Filtered: &e%filtered%", "", "&bRight-Click to clear!", "&eClick to edit filter!")),
    GUI_PRESET_FILTER_QUERY("gui.preset.filter.query", "Enter query"),

    SIGN_ADAPT_GROUND("sign.adapt-ground", Arrays.asList("", "&aGround", "", "")),
    SIGN_CACHING_CURRENT_BUILDING("sign.caching-current-building", Arrays.asList("", "&6Caching", "", "")),
    SIGN_CLEAR_CURRENT_BUILDING("sign.clear-current-building", Arrays.asList("", "&dClear", "", "")),
    SIGN_SELECT_ARENA("sign.select-arena", Arrays.asList("", "&0Select Arena", "", "")),
    SIGN_SELECT_BUILD_MODE("sign.select-build-mode", Arrays.asList("", "&bMode", "%build_mode%", "")),
    SIGN_OPTIONS_BUILD_MODE_DEFAULT("sign.options.build-mode.default", "&aDefault"),
    SIGN_OPTIONS_BUILD_MODE_COUNTDOWN_ONCE("sign.options.build-mode.countdown-once", "&eOnce"),
    SIGN_OPTIONS_BUILD_MODE_COUNTDOWN_CONTINUOUS("sign.options.build-mode.countdown-continuous", "&cContinuous"),
    SIGN_SELECT_PRESET_BUILDING("sign.select-preset-building", Arrays.asList("", "&6Preset", "", "")),
    SIGN_START_COUNTDOWN("sign.start-countdown", Arrays.asList("", "&0Start", "&0(Not available in default build mode)", "")),
    SIGN_VIEW_CACHED_BUILDING("sign.view-cached-building", Arrays.asList("", "&eView", "", "")),

    INVITE_MESSAGE("multiplayer.invite-message", "&5%player% has invited you to teleport to their island!"),
    JOIN_MESSAGE("multiplayer.join-message", "&5%player% has requested to teleport to your island"),
    CLICK_TEXT_ACCEPT("multiplayer.click-text-accept", "&a[Accept]"),
    CLICK_TEXT_DENY("multiplayer.click-text-deny", "&c[Deny]"),
    LEAVE_ACTIVE("multiplayer.leave-active", "&aYou left %player%'s island!"),
    LEAVE_PASSIVE("multiplayer.leave-passive", "&c%player% left your island!"),
    KICK_ACTIVE("multiplayer.kick-active", "&a%player% has been kicked off your island!"),
    KICK_PASSIVE("multiplayer.kick-passive", "&c%player% has kicked you off their island!"),
    ALREADY_DENIED("multiplayer.already-denied", "&c%player% has already denied you. Please try again later!"),
    ALREADY_INVITED("multiplayer.already-invited", "&cYou have already sent an invite to %player%!"),
    ALREADY_JOINED("multiplayer.already-joined", "&cYou have already sent a request to %player%!"),
    ALREADY_ON_ISLAND_INVITE("multiplayer.already-on-island.invite", "&c%player% is already on your island!"),
    ALREADY_ON_ISLAND_JOIN("multiplayer.already-on-island.join", "&cYou are already on %player%'s island!"),
    NO_MATCHING_PLAYER("multiplayer.no-matching-player", "&cNo matching player found!"),
    ACCEPTED_INVITE_ACTIVE("multiplayer.result.accepted-invite-active", "&aYou have accepted %player%'s invite."),
    ACCEPTED_INVITE_PASSIVE("multiplayer.result.accepted-invite-passive", "&a%player% has accepted your invite!"),
    DENIED_INVITE_ACTIVE("multiplayer.result.denied-invite-active", "&aYou have denied %player%'s invite."),
    DENIED_INVITE_PASSIVE("multiplayer.result.denied-invite-passive", "&a%player% has denied your invite!"),
    ACCEPTED_JOIN_ACTIVE("multiplayer.result.accepted-join-active", "&aYou have accepted %player%'s request."),
    ACCEPTED_JOIN_PASSIVE("multiplayer.result.accepted-join-passive", "&a%player% has accepted your request!"),
    DENIED_JOIN_ACTIVE("multiplayer.result.denied-join-active", "&aYou have denied %player%'s request."),
    DENIED_JOIN_PASSIVE("multiplayer.result.denied-join-passive", "&a%player% has denied your request!");

    private final String path;
    private final Object defaultValue;
    private static final ConfigManager CONFIG;

    static {
        CONFIG = new ConfigManager(Main.getPlugin(), Main.getPlugin().getDataFolder().getPath(), "Messages");

        for (Messages value : values()) {
            CONFIG.addDefault(value.getPath(), value.getDefaultValue());
        }

        CONFIG.copyDefaults();
        CONFIG.save();
    }

    public String getMessage() {
        return CONFIG.getString(this.path);
    }

    public List<String> getMessageList() {
        return CONFIG.getList(this.path).stream().map(Object::toString).collect(Collectors.toList());
    }
}
