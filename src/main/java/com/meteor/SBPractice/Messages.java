package com.meteor.SBPractice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
@SuppressWarnings("ResultOfMethodCallIgnored")
public enum Messages {

    COMMAND_HELP("basic.command-help"),
    PRE_START("basic.game.pre-start"),
    PERFECT_MATCH_TITLE("basic.game.perfect-match-title"),
    PERFECT_MATCH_SUBTITLE("basic.game.perfect-match-subtitle"),
    TIMEOUT_PERFECT_MATCH_SUBTITLE("basic.game.timeout-perfect-match-subtitle"),
    ACTIONBAR_TIME("basic.game.actionbar-time"),
    PLOT_FULL("basic.game.plot-full"),
    SUCCESSFUL_BUFFER("basic.game.successful-buffer"),
    CANNOT_DO_THAT("basic.game.cannot-do-that"),
    PLATFORM_ADAPT("basic.game.platform-adapt"),
    SHOW_BUILD("basic.game.show-build"),
    ITEM_CLEAR("basic.item.clear"),
    ITEM_PRESTART("basic.item.prestart"),
    HIGHJUMP_ENABLED("basic.highjump.enabled"),
    HIGHJUMP_DISABLED("basic.highjump.disabled"),
    ADMIN_MODE_ENABLED("basic.admin-mode.enabled"),
    ADMIN_MODE_DISABLED("basic.admin-mode.disabled"),
    COUNTDOWN_ENABLED("basic.countdown.enabled"),
    COUNTDOWN_DISABLED("basic.countdown.disabled"),
    COUNTDOWN_MODE("basic.countdown.mode-text"),
    COUNTDOWN_TIMEOUT("basic.countdown.timed-out"),
    COUNTDOWN_SET_HINT("basic.countdown.set-hint"),

    LEAVE("request.leave"),
    DENIED("request.denied"),
    REQUEST_EXPIRES("request.expires"),
    ANTI_CONFLICT("request.anti-conflict"),
    OWNER_LEAVE("request.owner-leave"),
    PLAYER_NOT_IN_PLOT("request.player-not-in-plot"),
    PLAYER_NOT_FOUND("request.player-not-found"),
    PLOT_NOT_FOUND("request.plot-not-found"),
    REQUEST_NOT_FOUND("request.request-not-found"),
    ALREADY_REQUESTED("request.already-requested"),
    REQUESTED("request.requested"),
    INVITE_MESSAGE("request.invite-message"),
    JOIN_MESSAGE("request.join-message"),
    REQUEST_CLICK_TEXT_ACCEPT("request.request-click-text-accept"),
    REQUEST_HOVER_TEXT_ACCEPT("request.request-hover-text-accept"),
    REQUEST_CLICK_TEXT_DENY("request.request-click-text-deny"),
    REQUEST_HOVER_TEXT_DENY("request.request-hover-text-deny"),
    VICTIM_ALREADY_IN_PLOT("request.victim-already-in-plot"),
    RECEIVER_ALREADY_IN_PLOT("request.receiver-already-in-plot"),
    VICTIM_KICKED("request.victim-kicked"),
    RECEIVER_KICKED("request.receiver-kicked"),
    VICTIM_ACCEPTED("request.victim-accepted"),
    RECEIVER_ACCEPTED("request.receiver-accepted"),
    VICTIM_DENYED("request.victim-denyed"),
    RECEIVER_DENYED("request.receiver-denyed");

    private final String path;

    private static YamlConfiguration CONFIG;
    static {
        File dir = Main.getPlugin().getDataFolder();
        if (!dir.exists()) dir.mkdir();
        File file = new File(dir, "messages.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        } CONFIG = YamlConfiguration.loadConfiguration(file);

        CONFIG.addDefault(COMMAND_HELP.getPath(), Arrays.asList(
                "&2▪ &7/&emp accept <玩家名> &8- &a同意玩家的申请",
                "&2▪ &7/&emp deny <玩家名> &8- &a拒绝玩家的申请",
                "&2▪ &7/&emp invite <玩家名> &8- &a邀请玩家加入你的岛屿",
                "&2▪ &7/&emp join <玩家名> &8- &a申请一个玩家加入所在的岛屿",
                "&2▪ &7/&emp kick <玩家名> &8- &a踢出你所在的岛屿",
                "&2▪ &7/&emp leave &8- &a离开你当前所在的岛屿"
        ));

        CONFIG.addDefault(PRE_START.getPath(), "&d距离开始还有 &c%time% &d秒");
        CONFIG.addDefault(PERFECT_MATCH_TITLE.getPath(), "&a&l完美还原");
        CONFIG.addDefault(PERFECT_MATCH_SUBTITLE.getPath(), "&6&l%time%");
        CONFIG.addDefault(TIMEOUT_PERFECT_MATCH_SUBTITLE.getPath(), "&c你超时了 %time% 秒");
        CONFIG.addDefault(ACTIONBAR_TIME.getPath(), "&b&l%time%");
        CONFIG.addDefault(PLOT_FULL.getPath(), "&c房间已满！");
        CONFIG.addDefault(SUCCESSFUL_BUFFER.getPath(), "&a已将建筑保存至缓存区");
        CONFIG.addDefault(CANNOT_DO_THAT.getPath(), "&c你不能这样做！");
        CONFIG.addDefault(PLATFORM_ADAPT.getPath(), "&a地板已适应");
        CONFIG.addDefault(SHOW_BUILD.getPath(), "&a已将建筑复制到建造区");
        CONFIG.addDefault(ITEM_CLEAR.getPath(), "&9&l使用&f&l雪球&9&l来清除场地");
        CONFIG.addDefault(ITEM_PRESTART.getPath(), "&d&l使用&e&l鸡蛋&d&l来激活倒计时");
        CONFIG.addDefault(HIGHJUMP_ENABLED.getPath(), "&a高跳已开启");
        CONFIG.addDefault(HIGHJUMP_DISABLED.getPath(), "&c高跳已关闭");
        CONFIG.addDefault(ADMIN_MODE_ENABLED.getPath(), "&a已开启管理员模式");
        CONFIG.addDefault(ADMIN_MODE_DISABLED.getPath(), "&c已关闭管理员模式");
        CONFIG.addDefault(COUNTDOWN_ENABLED.getPath(), "&a已将倒计时时间设为 %time% 秒！");
        CONFIG.addDefault(COUNTDOWN_DISABLED.getPath(), "&c已禁用倒计时！");
        CONFIG.addDefault(COUNTDOWN_MODE.getPath(), "（倒计时）");
        CONFIG.addDefault(COUNTDOWN_TIMEOUT.getPath(), "已超时");
        CONFIG.addDefault(COUNTDOWN_SET_HINT.getPath(), "&a请输入一个数字来代表倒计时时间（0表示禁用倒计时）！");

        CONFIG.addDefault(LEAVE.getPath(), "&e你离开了 %player% 的岛屿");
        CONFIG.addDefault(DENIED.getPath(), "&c对方已经拒绝你的请求了，请稍后再试！");
        CONFIG.addDefault(REQUEST_EXPIRES.getPath(), "&c发送至 %player% 的请求已失效！");
        CONFIG.addDefault(ANTI_CONFLICT.getPath(), "&c请先处理来自对方的请求！");
        CONFIG.addDefault(OWNER_LEAVE.getPath(), "&c岛屿所有者离开了！");
        CONFIG.addDefault(PLAYER_NOT_IN_PLOT.getPath(), "&c%player% 不在你的岛屿上！");
        CONFIG.addDefault(PLAYER_NOT_FOUND.getPath(), "&c未找到玩家！");
        CONFIG.addDefault(PLOT_NOT_FOUND.getPath(), "&c未找到对方所在的岛屿！");
        CONFIG.addDefault(REQUEST_NOT_FOUND.getPath(), "&c未找到来自 %player% 的请求！");
        CONFIG.addDefault(ALREADY_REQUESTED.getPath(), "&c你已经向 %player% 发送了一个请求！");
        CONFIG.addDefault(REQUESTED.getPath(), "&9已将请求发送至 &e%player%&9，对方有 &c60 &9秒的时间来处理");

        CONFIG.addDefault(INVITE_MESSAGE.getPath(), "&6%player% &5邀请你传送至他的岛屿");
        CONFIG.addDefault(JOIN_MESSAGE.getPath(), "&6%player% &5申请传送至你的岛屿");
        CONFIG.addDefault(REQUEST_CLICK_TEXT_ACCEPT.getPath(), "&a&l[接受]");
        CONFIG.addDefault(REQUEST_HOVER_TEXT_ACCEPT.getPath(), "点击接受 %player% 的请求");
        CONFIG.addDefault(REQUEST_CLICK_TEXT_DENY.getPath(), "&c&l[拒绝]");
        CONFIG.addDefault(REQUEST_HOVER_TEXT_DENY.getPath(), "点击拒绝 %player% 的请求");

        CONFIG.addDefault(VICTIM_ALREADY_IN_PLOT.getPath(), "&c你已经在 %player% 的岛屿上了！");
        CONFIG.addDefault(RECEIVER_ALREADY_IN_PLOT.getPath(), "&c%player% 已经在你的岛屿上了！");
        CONFIG.addDefault(VICTIM_KICKED.getPath(), "&c你被 %player% 踢出对方的岛屿！");
        CONFIG.addDefault(RECEIVER_KICKED.getPath(), "&a已将 %player% 踢出你的岛屿！");
        CONFIG.addDefault(VICTIM_ACCEPTED.getPath(), "&a%player% 接受了你的请求！");
        CONFIG.addDefault(RECEIVER_ACCEPTED.getPath(), "&a你接受了来自 %player% 的请求！");
        CONFIG.addDefault(VICTIM_DENYED.getPath(), "&c%player% 拒绝了你的请求！");
        CONFIG.addDefault(RECEIVER_DENYED.getPath(), "&c你拒绝了来自 %player% 的请求！");

        CONFIG.options().copyDefaults(true);
        try {
            CONFIG.save(file);
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        } CONFIG = YamlConfiguration.loadConfiguration(file);
    }

    public String getMessage() {
        return CONFIG.getString(path, "%" + path + "%").replaceAll("&", "§").replace("§§", "&");
    }

    public List<String> getMessageList() {
        List<String> messages = new ArrayList<>(CONFIG.getStringList(path));
        if (messages.isEmpty()) messages.add("%" + path + "%");
        return messages;
    }
}
