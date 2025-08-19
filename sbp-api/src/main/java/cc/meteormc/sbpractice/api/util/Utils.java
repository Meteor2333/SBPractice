package cc.meteormc.sbpractice.api.util;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Utils {
    private static final Pattern DECOLORIZE_COLOR_PATTERN = Pattern.compile("(?i)" + '§' + "[0-9A-FK-OR]");

    private Utils() {
        throw new IllegalStateException("Utility class");
    }

    public static String decolorize(String text) {
        return DECOLORIZE_COLOR_PATTERN.matcher(text).replaceAll("&");
    }

    public static List<String> decolorize(List<String> texts) {
        return texts.stream().map(Utils::decolorize).collect(Collectors.toList());
    }

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> colorize(List<String> texts) {
        return texts.stream().map(Utils::colorize).collect(Collectors.toList());
    }

    public static void resetPlayer(Player player) {
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setExp(0F);
        player.setFireTicks(0);
        player.setFlying(false);
        player.setFoodLevel(20);
        player.setHealth(20D);
        player.setLevel(0);
        player.getInventory().setArmorContents(null);
        player.getInventory().clear();
        player.updateInventory();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }
}
