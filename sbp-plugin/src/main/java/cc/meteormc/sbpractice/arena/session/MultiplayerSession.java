package cc.meteormc.sbpractice.arena.session;

import cc.meteormc.sbpractice.SBPractice;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.storage.player.PlayerData;
import cc.meteormc.sbpractice.config.Messages;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiplayerSession implements Listener {
    private final Player player;
    private final Map<Player, Long> denyList = new HashMap<>();
    private final Map<Player, Long> inviteList = new HashMap<>();
    private final Map<Player, Long> joinList = new HashMap<>();

    private static final List<MultiplayerSession> SESSIONS = new ArrayList<>();

    public MultiplayerSession(Player player) {
        this.player = player;
        SESSIONS.add(this);
        Bukkit.getPluginManager().registerEvents(this, SBPractice.getPlugin());
    }

    public void acceptPlayer(Player target) {
        MultiplayerSession session = getSession(target);
        if (session.inviteList.containsKey(this.player) && System.currentTimeMillis() - session.inviteList.get(this.player) < 60000L) {
            session.inviteList.remove(this.player);
            PlayerData.getData(this.player).ifPresent(data -> {
                Island island = data.getIsland();
                if (island.getOwner().equals(this.player)) island.remove();
                else {
                    island.removeGuest(this.player);
                    island.getOwner().sendMessage(Messages.PREFIX.getMessage() + Messages.LEAVE_PASSIVE.getMessage().replace("%player%", this.player.getName()));
                }
            });

            PlayerData.getData(target).ifPresent(data -> {
                Island island = data.getIsland();
                island.addGuest(this.player);
                this.player.sendMessage(Messages.ACCEPTED_INVITE_ACTIVE.getMessage().replace("%player%", target.getName()));
                target.sendMessage(Messages.ACCEPTED_INVITE_PASSIVE.getMessage().replace("%player%", this.player.getName()));
            });
        } else if (session.joinList.containsKey(this.player) && System.currentTimeMillis() - session.joinList.get(this.player) < 60000L) {
            session.joinList.remove(this.player);
            PlayerData.getData(target).ifPresent(data -> {
                Island island = data.getIsland();
                if (island.getOwner().equals(target)) island.remove();
                else {
                    island.removeGuest(target);
                    island.getOwner().sendMessage(Messages.PREFIX.getMessage() + Messages.LEAVE_PASSIVE.getMessage().replace("%player%", target.getName()));
                }
            });

            PlayerData.getData(this.player).ifPresent(data -> {
                Island island = data.getIsland();
                island.addGuest(target);
                this.player.sendMessage(Messages.ACCEPTED_JOIN_ACTIVE.getMessage().replace("%player%", target.getName()));
                target.sendMessage(Messages.ACCEPTED_JOIN_PASSIVE.getMessage().replace("%player%", this.player.getName()));
            });
        } else this.player.sendMessage(Messages.NO_MATCHING_PLAYER.getMessage());
    }

    public void denyPlayer(Player target) {
        MultiplayerSession session = getSession(target);
        if (session.inviteList.containsKey(this.player) && System.currentTimeMillis() - session.inviteList.get(this.player) < 60000L) {
            session.inviteList.remove(this.player);
            session.denyList.put(this.player, System.currentTimeMillis());

            this.player.sendMessage(Messages.DENIED_INVITE_ACTIVE.getMessage().replace("%player%", target.getName()));
            target.sendMessage(Messages.DENIED_INVITE_PASSIVE.getMessage().replace("%player%", this.player.getName()));
        } else if (session.joinList.containsKey(this.player) && System.currentTimeMillis() - session.joinList.get(this.player) < 60000L) {
            session.joinList.remove(this.player);
            session.denyList.put(this.player, System.currentTimeMillis());

            this.player.sendMessage(Messages.DENIED_JOIN_ACTIVE.getMessage().replace("%player%", target.getName()));
            target.sendMessage(Messages.DENIED_JOIN_PASSIVE.getMessage().replace("%player%", this.player.getName()));
        } else this.player.sendMessage(Messages.NO_MATCHING_PLAYER.getMessage());
    }

    public void invitePlayer(Player player) {
        PlayerData.getData(this.player).ifPresent(data -> {
            Island island = data.getIsland();
            if (island.getOwner().equals(this.player)) {
                if (this.inviteList.containsKey(player) && System.currentTimeMillis() - this.inviteList.get(player) < 60000L) {
                    this.player.sendMessage(Messages.PREFIX.getMessage() + Messages.ALREADY_INVITED.getMessage().replace("%player%", player.getName()));
                } else if (this.joinList.containsKey(player) && System.currentTimeMillis() - this.joinList.get(player) < 60000L) {
                    this.player.sendMessage(Messages.PREFIX.getMessage() + Messages.ALREADY_JOINED.getMessage().replace("%player%", player.getName()));
                } else if (this.denyList.containsKey(player) && System.currentTimeMillis() - this.denyList.get(player) < 60000L) {
                    this.player.sendMessage(Messages.PREFIX.getMessage() + Messages.ALREADY_DENIED.getMessage().replace("%player%", player.getName()));
                } else if (island.getGuests().contains(player)) {
                    this.player.sendMessage(Messages.PREFIX.getMessage() + Messages.ALREADY_ON_ISLAND_INVITE.getMessage().replace("%player%", player.getName()));
                } else {
                    this.inviteList.put(player, System.currentTimeMillis());
                    player.sendMessage(Messages.PREFIX.getMessage() + Messages.INVITE_MESSAGE.getMessage().replace("%player%", this.player.getName()));
                    TextComponent accept = new TextComponent(Messages.CLICK_TEXT_ACCEPT.getMessage());
                    accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp accept " + this.player.getName()));
                    TextComponent deny = new TextComponent(Messages.CLICK_TEXT_DENY.getMessage());
                    deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp deny " + this.player.getName()));
                    player.spigot().sendMessage(accept, new TextComponent("   "), deny);
                }
            } else this.player.sendMessage(Messages.PREFIX.getMessage() + Messages.CANNOT_DO_THAT.getMessage());
        });
    }

    public void joinPlayer(Player player) {
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            Player target = island.getOwner();
            if (this.inviteList.containsKey(target) && System.currentTimeMillis() - this.inviteList.get(target) < 60000L) {
                this.player.sendMessage(Messages.PREFIX.getMessage() + Messages.ALREADY_INVITED.getMessage().replace("%player%", target.getName()));
            } else if (this.joinList.containsKey(target) && System.currentTimeMillis() - this.joinList.get(target) < 60000L) {
                this.player.sendMessage(Messages.PREFIX.getMessage() + Messages.ALREADY_JOINED.getMessage().replace("%player%", target.getName()));
            } else if (this.denyList.containsKey(target) && System.currentTimeMillis() - this.denyList.get(target) < 60000L) {
                this.player.sendMessage(Messages.PREFIX.getMessage() + Messages.ALREADY_DENIED.getMessage().replace("%player%", target.getName()));
            } else if (island.getGuests().contains(this.player)) {
                this.player.sendMessage(Messages.PREFIX.getMessage() + Messages.ALREADY_ON_ISLAND_JOIN.getMessage().replace("%player%", target.getName()));
            } else {
                this.joinList.put(target, System.currentTimeMillis());
                target.sendMessage(Messages.PREFIX.getMessage() + Messages.JOIN_MESSAGE.getMessage().replace("%player%", this.player.getName()));
                TextComponent accept = new TextComponent(Messages.CLICK_TEXT_ACCEPT.getMessage());
                accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp accept " + this.player.getName()));
                TextComponent deny = new TextComponent(Messages.CLICK_TEXT_DENY.getMessage());
                deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp deny " + this.player.getName()));
                target.spigot().sendMessage(accept, new TextComponent("   "), deny);
            }
        });
    }

    public void close() {
        SESSIONS.remove(this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        getSession(event.getPlayer()).close();
    }

    public static @NotNull MultiplayerSession getSession(Player player) {
        return SESSIONS.stream()
                .filter(session -> session.player.equals(player))
                .findFirst()
                .orElse(new MultiplayerSession(player));
    }
}
