package cc.meteormc.sbpractice.listener;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.config.MainConfig;
import cc.meteormc.sbpractice.config.Message;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CheckUpdateListener implements Listener {
    private final Supplier<String> CHECK = Suppliers.memoizeWithExpiration(() -> {
        try {

            File file = Main.get().getPlugin().getFile();
            //noinspection VulnerableCodeUsages
            String sha256 = Files.asByteSource(file).hash(Hashing.sha256()).toString();
            URLConnection connection = REPO_URI.toURL().openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
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
    }, 1L, TimeUnit.DAYS);

    private static final URI REPO_URI;

    static {
        REPO_URI = URI.create("https://api.github.com/repos/Meteor2333/SBPractice/releases/latest");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) return;
        if (!MainConfig.CHECK_UPDATE.resolve()) return;
        CompletableFuture.supplyAsync(CHECK::get).thenAcceptAsync(result -> {
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
        }, runnable -> Bukkit.getScheduler().runTask(Main.get(), runnable));
    }
}
