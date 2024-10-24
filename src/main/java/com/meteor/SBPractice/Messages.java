package com.meteor.SBPractice;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Messages {
    private static YamlConfiguration yml;

    public Messages() {
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
        } yml = YamlConfiguration.loadConfiguration(file);

        yml.addDefault("command-help", Arrays.asList(
                "&2▪ &7/&emp accept <玩家名> &8- &a同意玩家的申请",
                "&2▪ &7/&emp deny <玩家名> &8- &a拒绝玩家的申请",
                "&2▪ &7/&emp invite <玩家名> &8- &a邀请玩家加入你的岛屿",
                "&2▪ &7/&emp join <玩家名> &8- &a申请一个玩家加入所在的岛屿",
                "&2▪ &7/&emp kick <玩家名> &8- &a踢出你所在的岛屿",
                "&2▪ &7/&emp leave &8- &a离开你当前所在的岛屿"
        ));

        yml.addDefault("pre-start", "&d距离开始还有 &c%time% &d秒");
        yml.addDefault("perfect-match-title", "&a&l完美还原");
        yml.addDefault("perfect-match-subtitle", "&6&l%time%");
        yml.addDefault("timeout-perfect-match-subtitle", "&c你超时了 %time% 秒");
        yml.addDefault("actionbar-time", "&b&l%time%");
        yml.addDefault("plot-full", "&c房间已满！");
        yml.addDefault("cannot-do-that", "&c你不能这样做！");
        yml.addDefault("highjump-enabled", "&a高跳已开启");
        yml.addDefault("highjump-disabled", "&c高跳已关闭");
        yml.addDefault("successful-buffer", "&a已将建筑保存至缓存区");
        yml.addDefault("admin-mode-enabled", "&a已开启管理员模式");
        yml.addDefault("admin-mode-disabled", "&c已关闭管理员模式");
        yml.addDefault("platform-adapt", "&a地板已适应");
        yml.addDefault("show-build", "&a已将建筑复制到建造区");
        yml.addDefault("countdown-mode", "（倒计时）");
        yml.addDefault("countdown-timeout", "已超时");
        yml.addDefault("countdown-enabled", "&a已将倒计时时间设为 %time% 秒！");
        yml.addDefault("countdown-disabled", "&c已禁用倒计时！");
        yml.addDefault("set-countdown-hint", "&a请输入一个数字来代表倒计时时间（0表示禁用倒计时）！");

        yml.addDefault("leave", "&e你离开了 %player% 的岛屿");
        yml.addDefault("denied", "&c对方已经拒绝你的请求了，请稍后再试！");
        yml.addDefault("requeste-expires", "&c发送至 %player% 的请求已失效！");
        yml.addDefault("anti-conflict", "&c请先处理来自对方的请求！");
        yml.addDefault("owner-leave", "&c岛屿所有者离开了！");
        yml.addDefault("player-not-found", "&c未找到玩家！");
        yml.addDefault("player-not-in-plot", "&c%player% 不在你的岛屿上！");
        yml.addDefault("plot-not-found", "&c未找到对方所在的岛屿！");
        yml.addDefault("requeste-not-found", "&c未找到来自 %player% 的请求！");
        yml.addDefault("already-requested", "&c你已经向 %player% 发送了一个请求！");
        yml.addDefault("requested", "&9已将请求发送至 &e%player%&9，对方有 &c60 &9秒的时间来处理");

        yml.addDefault("invite-message", "&6%player% &5邀请你传送至他的岛屿");
        yml.addDefault("join-message", "&6%player% &5申请传送至你的岛屿");
        yml.addDefault("requeste-click-text-accept", "&a&l[接受]");
        yml.addDefault("requeste-hover-text-accept", "点击接受 %player% 的请求");
        yml.addDefault("requeste-click-text-deny", "&c&l[拒绝]");
        yml.addDefault("requeste-hover-text-deny", "点击拒绝 %player% 的请求");

        yml.addDefault("victim-already-in-plot", "&c你已经在 %player% 的岛屿上了！");
        yml.addDefault("receiver-already-in-plot", "&c%player% 已经在你的岛屿上了！");
        yml.addDefault("victim-kicked", "&c你被 %player% 踢出对方的岛屿！");
        yml.addDefault("receiver-kicked", "&a已将 %player% 踢出你的岛屿！");
        yml.addDefault("victim-accepted", "&a%player% 接受了你的请求！");
        yml.addDefault("receiver-accepted", "&a你接受了来自 %player% 的请求！");
        yml.addDefault("victim-denyed", "&c%player% 拒绝了你的请求！");
        yml.addDefault("receiver-denyed", "&c你拒绝了来自 %player% 的请求！");
        yml.options().copyDefaults(true);
        try {
            yml.save(file);
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public static String getMessage(String path) {
        return parseColor(yml.getString(path, "%" + path + "%"));
    }

    public static List<String> getMessageList(String path) {
        List<String> messages = new ArrayList<>();
        for (String message : yml.getStringList(path)) messages.add(parseColor(message));
        return messages;
    }

    private static String parseColor(String text) {
        return text.replaceAll("&", "§").replace("§§", "&");
    }
}
