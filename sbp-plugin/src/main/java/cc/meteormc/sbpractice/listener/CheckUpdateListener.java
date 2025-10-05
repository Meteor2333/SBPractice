package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.config.Message;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class CheckUpdateListener implements Listener {
    private final Supplier<String> CHECK = Suppliers.memoize(() -> {
        try (FileInputStream stream = new FileInputStream(Main.get().getPlugin().getFile())) {
            String sha256 = DigestUtils.sha256Hex(stream);
            URLConnection connection = REPO_URL.openConnection();
            connection.setRequestProperty("User-Agent", "Java-App");
            connection.setRequestProperty("Accept", "application/vnd.github+json");

            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            JsonArray assets = json.getAsJsonArray("assets");
            for (JsonElement asset : assets) {
                String digest = asset.getAsJsonObject().get("digest").getAsString();
                if (sha256.equalsIgnoreCase(digest.replace("sha256:", ""))) return null;
            }
            return json.get("html_url").getAsString();
        } catch (IOException ignored) {
            return null;
        }
    });

    private static final URL REPO_URL;

    static {
        try {
            REPO_URL = new URL("https://api.github.com/repos/Meteor2333/SBPractice/releases/latest");
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Failed to create URL for repository!", e);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) return;
        if (!MainConfig.CHECK_UPDATE.resolve()) return;
        Bukkit.getScheduler().runTaskAsynchronously(Main.get().getPlugin(), () -> {
            String result = CHECK.get();
            if (result == null) return;
            player.spigot().sendMessage(
                    new ComponentBuilder(Message.PREFIX.parseLine(player))
                            .append("A new version is available! Download at ")
                            .color(ChatColor.GREEN)
                            .append(result)
                            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, result))
                            .underlined(true)
                            .append(".")
                            .event((ClickEvent) null)
                            .underlined(false)
                            .create()
            );
        });
    }
}
