package cc.meteormc.sbpractice.arena.session;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.config.Message;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultiplayerSession implements Listener {
    private final Player player;
    private final Map<Player, Long> denyList = new HashMap<>();
    private final Map<Player, Long> inviteList = new HashMap<>();
    private final Map<Player, Long> joinList = new HashMap<>();

    private static final List<MultiplayerSession> SESSIONS = new CopyOnWriteArrayList<>();

    public MultiplayerSession(Player player) {
        this.player = player;
        SESSIONS.add(this);
        Bukkit.getPluginManager().registerEvents(this, Main.getPlugin());
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
                    Message.MULTIPLAYER.LEAVE.PASSIVE.sendTo(island.getOwner(), this.player.getName());
                }
            });

            PlayerData.getData(target).ifPresent(data -> {
                Island island = data.getIsland();
                island.addGuest(this.player);
                Message.MULTIPLAYER.INVITE.ACCEPTED_ACTIVE.sendTo(this.player, target.getName());
                Message.MULTIPLAYER.INVITE.ACCEPTED_PASSIVE.sendTo(target, this.player.getName());
            });
        } else if (session.joinList.containsKey(this.player) && System.currentTimeMillis() - session.joinList.get(this.player) < 60000L) {
            session.joinList.remove(this.player);
            PlayerData.getData(target).ifPresent(data -> {
                Island island = data.getIsland();
                if (island.getOwner().equals(target)) island.remove();
                else {
                    island.removeGuest(target);
                    Message.MULTIPLAYER.LEAVE.PASSIVE.sendTo(island.getOwner(), target.getName());
                }
            });

            PlayerData.getData(this.player).ifPresent(data -> {
                Island island = data.getIsland();
                island.addGuest(target);
                Message.MULTIPLAYER.JOIN.ACCEPTED_ACTIVE.sendTo(this.player, target.getName());
                Message.MULTIPLAYER.JOIN.ACCEPTED_PASSIVE.sendTo(target, this.player.getName());
            });
        } else Message.BASIC.PLAYER_NOT_FOUND.sendTo(this.player);
    }

    public void denyPlayer(Player target) {
        MultiplayerSession session = getSession(target);
        if (session.inviteList.containsKey(this.player) && System.currentTimeMillis() - session.inviteList.get(this.player) < 60000L) {
            session.inviteList.remove(this.player);
            session.denyList.put(this.player, System.currentTimeMillis());

            Message.MULTIPLAYER.INVITE.DENIED_ACTIVE.sendTo(this.player, target.getName());
            Message.MULTIPLAYER.INVITE.DENIED_PASSIVE.sendTo(target, this.player.getName());
        } else if (session.joinList.containsKey(this.player) && System.currentTimeMillis() - session.joinList.get(this.player) < 60000L) {
            session.joinList.remove(this.player);
            session.denyList.put(this.player, System.currentTimeMillis());

            Message.MULTIPLAYER.JOIN.DENIED_ACTIVE.sendTo(this.player, target.getName());
            Message.MULTIPLAYER.JOIN.DENIED_PASSIVE.sendTo(target, this.player.getName());
        } else Message.BASIC.PLAYER_NOT_FOUND.sendTo(this.player);
    }

    public void invitePlayer(Player player) {
        PlayerData.getData(this.player).ifPresent(data -> {
            Island island = data.getIsland();
            if (island.getOwner().equals(this.player)) {
                if (this.inviteList.containsKey(player) && System.currentTimeMillis() - this.inviteList.get(player) < 60000L) {
                    Message.MULTIPLAYER.INVITE.ALREADY_INVITED.sendTo(this.player, player.getName());
                } else if (this.joinList.containsKey(player) && System.currentTimeMillis() - this.joinList.get(player) < 60000L) {
                    Message.MULTIPLAYER.JOIN.ALREADY_JOINED.sendTo(this.player, player.getName());
                } else if (this.denyList.containsKey(player) && System.currentTimeMillis() - this.denyList.get(player) < 60000L) {
                    Message.MULTIPLAYER.ALREADY_DENIED.sendTo(this.player, player.getName());
                } else if (island.getGuests().contains(player)) {
                    Message.MULTIPLAYER.INVITE.ALREADY_ON_ISLAND.sendTo(this.player, player.getName());
                } else {
                    this.inviteList.put(player, System.currentTimeMillis());
                    Message.MULTIPLAYER.INVITE.ON_INVITE.sendTo(player, this.player.getName());
                    TextComponent accept = new TextComponent(Message.MULTIPLAYER.ACCEPT.parseLine(player));
                    accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp accept " + this.player.getName()));
                    TextComponent deny = new TextComponent(Message.MULTIPLAYER.DENY.parseLine(player));
                    deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp deny " + this.player.getName()));
                    player.spigot().sendMessage(accept, new TextComponent("   "), deny);
                }
            } else Message.BASIC.CANNOT_DO_THAT.sendTo(this.player);
        });
    }

    public void joinPlayer(Player player) {
        PlayerData.getData(player).ifPresent(data -> {
            Island island = data.getIsland();
            Player target = island.getOwner();
            if (this.inviteList.containsKey(target) && System.currentTimeMillis() - this.inviteList.get(target) < 60000L) {
                Message.MULTIPLAYER.INVITE.ALREADY_INVITED.sendTo(this.player, target.getName());
            } else if (this.joinList.containsKey(target) && System.currentTimeMillis() - this.joinList.get(target) < 60000L) {
                Message.MULTIPLAYER.JOIN.ALREADY_JOINED.sendTo(this.player, target.getName());
            } else if (this.denyList.containsKey(target) && System.currentTimeMillis() - this.denyList.get(target) < 60000L) {
                Message.MULTIPLAYER.ALREADY_DENIED.sendTo(this.player, target.getName());
            } else if (island.getGuests().contains(this.player)) {
                Message.MULTIPLAYER.JOIN.ALREADY_ON_ISLAND.sendTo(this.player, target.getName());
            } else {
                this.joinList.put(target, System.currentTimeMillis());
                Message.MULTIPLAYER.JOIN.ON_JOIN.sendTo(target, this.player.getName());
                TextComponent accept = new TextComponent(Message.MULTIPLAYER.ACCEPT.parseLine(player));
                accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp accept " + this.player.getName()));
                TextComponent deny = new TextComponent(Message.MULTIPLAYER.DENY.parseLine(player));
                deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp deny " + this.player.getName()));
                target.spigot().sendMessage(accept, new TextComponent("   "), deny);
            }
        });
    }

    public void close() {
        SESSIONS.remove(this);
    }

    public static @NotNull MultiplayerSession getSession(Player player) {
        return SESSIONS.stream()
                .filter(session -> session.player.equals(player))
                .findFirst()
                .orElse(new MultiplayerSession(player));
    }
}
