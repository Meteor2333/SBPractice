package com.meteor.SBPractice.Utils;

import com.meteor.SBPractice.Main;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

@SuppressWarnings({"CallToPrintStackTrace", "ResultOfMethodCallIgnored"})
public class Message {
    private static YamlConfiguration yml;

    public Message() {
        File dir = Main.getPlugin().getDataFolder();
        if (!dir.exists()) dir.mkdir();
        File file = new File(dir, "message.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } yml = YamlConfiguration.loadConfiguration(file);
        yml.addDefault("perfect-match-title", "&a&l完美还原");
        yml.addDefault("perfect-match-subtitle", "&6%time%");
        yml.addDefault("actionbar-time", "&b&l%time%");
        yml.addDefault("you-cannot-leave", "&c&l你不能离开这个区域！");
        yml.addDefault("plot-full", "&c&l房间已满！当前处于旁观模式！");
        yml.addDefault("cannot-buffer-build", "&c&l只有岛屿所有者能够缓存建筑");
        yml.addDefault("highjump-enabled", "&a&l高跳已开启");
        yml.addDefault("highjump-disabled", "&c&l高跳已关闭");
        yml.addDefault("successful-buffer", "&a&l已将建筑保存至缓存区");
        yml.addDefault("admin-mode-enabled", "&a&l已开启管理员模式");
        yml.addDefault("admin-mode-disabled", "&c&l已关闭管理员模式");
        yml.addDefault("spec-mode-enabled", "&a&l已切换至旁观模式");
        yml.addDefault("spec-mode-disabled", "&a&l已切换至创造模式");
        yml.addDefault("apply", "&a&l已将申请发给 %player%");
        yml.addDefault("apply-target", "&5&l%player% 申请和你一起建造");
        yml.addDefault("apply-accept", "&a&l[接受]");
        yml.addDefault("apply-accept-showtext", "&a点击以接受此申请");
        yml.addDefault("apply-accept-chat", "&a&l你接受了来自 %player% 的申请！若要踢出玩家，请输入/mp kick <玩家名>");
        yml.addDefault("apply-accept-target-chat", "&a&l%player% 接受了你的申请！若要离开，请输入/mp leave");
        yml.addDefault("apply-deny", "&c&l[拒绝]");
        yml.addDefault("apply-deny-showtext", "&c点击以拒绝此申请");
        yml.addDefault("apply-deny-chat", "&c&l你拒绝了来自 %player% 的申请！");
        yml.addDefault("apply-deny-target-chat", "&c&l%player% 拒绝了你的申请！");
        yml.addDefault("denied", "&c&l对方已经拒绝过你的申请了！请稍后重试！");
        yml.addDefault("no-apply", "&c&l你没有接到此玩家的申请！");
        yml.addDefault("apply-player-offline", "&c&l对方不在线或处于旁观模式！");
        yml.addDefault("not-in-plot", "&c&l你当前不在任何一个岛屿上面！");
        yml.addDefault("in-same-plot", "&c&l你已经在这个玩家的岛屿上了！");
        yml.addDefault("kick-from-plot", "&c&l你被岛屿的所有者踢出！");
        yml.addDefault("no-in-your-plot", "&c&l此玩家不在你的岛屿上！");
        yml.addDefault("no-your-plot", "&c&l你不是这个岛屿的所有者！");
        yml.addDefault("cannot-leave", "&c&l你不能离开自己的岛屿！");
        yml.addDefault("successful-kick-plot", "&a&l已将 %player% 踢出你的岛屿");
        yml.addDefault("apply-lapsed", "&5&l来自 %player% 的申请已失效");
        yml.addDefault("already-apply", "&c&l你已经向 %player% 发送了一个申请！");
        yml.options().copyDefaults(true);
        try {
            yml.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMessage(String path) {
        return yml.getString(path).replaceAll("&", "§").replace("§§", "&");
    }
}
