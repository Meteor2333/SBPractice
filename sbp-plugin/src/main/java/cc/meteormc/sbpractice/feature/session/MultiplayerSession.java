package cc.meteormc.sbpractice.feature.session;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.storage.PlayerData;
import cc.meteormc.sbpractice.config.Message;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MultiplayerSession {
    private final Player player;
    private final Map<Player, Long> denyList = new HashMap<>();
    private final Map<Player, Long> inviteList = new HashMap<>();
    private final Map<Player, Long> joinList = new HashMap<>();

    private static final Map<UUID, MultiplayerSession> SESSIONS = new ConcurrentHashMap<>();

    public MultiplayerSession(Player player) {
        this.player = player;
        SESSIONS.put(player.getUniqueId(), this);
    }

    public static Optional<MultiplayerSession> getSession(Player player) {
        return Optional.ofNullable(SESSIONS.get(player.getUniqueId()));
    }

    public static MultiplayerSession getOrCreateSession(Player player) {
        return getSession(player).orElseGet(() -> new MultiplayerSession(player));
    }

    public void acceptPlayer(Player target) {
        MultiplayerSession session = getOrCreateSession(target);
        if (session.inviteList.containsKey(this.player) && System.currentTimeMillis() - session.inviteList.get(this.player) < 60000L) {
            session.inviteList.remove(this.player);
            PlayerData.getData(target).ifPresent(data -> {
                Island island = data.getIsland();
                if (island == null) return;

                PlayerData.getData(this.player).ifPresent(selfData -> {
                    Island selfIsland = selfData.getIsland();
                    if (selfIsland != null) selfIsland.removeAny(this.player, false);
                });

                island.addGuest(this.player);
                Message.MULTIPLAYER.INVITE.ACCEPTED_ACTIVE.sendTo(this.player, target.getName());
                Message.MULTIPLAYER.INVITE.ACCEPTED_PASSIVE.sendTo(target, this.player.getName());
            });
        } else if (session.joinList.containsKey(this.player) && System.currentTimeMillis() - session.joinList.get(this.player) < 60000L) {
            session.joinList.remove(this.player);
            PlayerData.getData(this.player).ifPresent(data -> {
                Island island = data.getIsland();
                if (island == null) return;

                PlayerData.getData(target).ifPresent(targetData -> {
                    Island targetIsland = targetData.getIsland();
                    if (targetIsland != null) targetIsland.removeAny(target, false);
                });

                island.addGuest(target);
                Message.MULTIPLAYER.JOIN.ACCEPTED_ACTIVE.sendTo(this.player, target.getName());
                Message.MULTIPLAYER.JOIN.ACCEPTED_PASSIVE.sendTo(target, this.player.getName());
            });
        } else Message.BASIC.PLAYER_NOT_FOUND.sendTo(this.player);
    }

    public void denyPlayer(Player target) {
        MultiplayerSession session = getOrCreateSession(target);
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

    public void invitePlayer(Player target) {
        Optional<PlayerData> optionalData = PlayerData.getData(this.player);
        if (!optionalData.isPresent()) return;

        PlayerData data = optionalData.get();
        Island island = data.getIsland();
        if (island == null) {
            Message.BASIC.CANNOT_DO_THAT.sendTo(this.player);
            return;
        }

        if (island.getOwner().equals(this.player)) {
            if (this.inviteList.containsKey(target) && System.currentTimeMillis() - this.inviteList.get(target) < 60000L) {
                Message.MULTIPLAYER.INVITE.ALREADY_INVITED.sendTo(this.player, target.getName());
            } else if (this.joinList.containsKey(target) && System.currentTimeMillis() - this.joinList.get(target) < 60000L) {
                Message.MULTIPLAYER.JOIN.ALREADY_JOINED.sendTo(this.player, target.getName());
            } else if (this.denyList.containsKey(target) && System.currentTimeMillis() - this.denyList.get(target) < 60000L) {
                Message.MULTIPLAYER.ALREADY_DENIED.sendTo(this.player, target.getName());
            } else if (island.getGuests().contains(target)) {
                Message.MULTIPLAYER.INVITE.ALREADY_ON_ISLAND.sendTo(this.player, target.getName());
            } else {
                this.inviteList.put(target, System.currentTimeMillis());
                Message.MULTIPLAYER.INVITE.ACTIVE.sendTo(this.player, target.getName());
                Message.MULTIPLAYER.INVITE.PASSIVE.sendTo(target, this.player.getName());
                TextComponent accept = new TextComponent(Message.MULTIPLAYER.ACCEPT.parseLine(target));
                accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp accept " + this.player.getName()));
                TextComponent deny = new TextComponent(Message.MULTIPLAYER.DENY.parseLine(target));
                deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp deny " + this.player.getName()));
                target.spigot().sendMessage(accept, new TextComponent("   "), deny);
            }
        } else Message.BASIC.CANNOT_DO_THAT.sendTo(this.player);
    }

    public void joinPlayer(Player target) {
        Optional<PlayerData> optionalData = PlayerData.getData(target);
        if (!optionalData.isPresent()) return;

        PlayerData data = optionalData.get();
        Island island = data.getIsland();
        if (island == null) {
            Message.BASIC.CANNOT_DO_THAT.sendTo(this.player);
            return;
        }

        target = island.getOwner();
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
            Message.MULTIPLAYER.JOIN.ACTIVE.sendTo(this.player, target.getName());
            Message.MULTIPLAYER.JOIN.PASSIVE.sendTo(target, this.player.getName());
            TextComponent accept = new TextComponent(Message.MULTIPLAYER.ACCEPT.parseLine(player));
            accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp accept " + this.player.getName()));
            TextComponent deny = new TextComponent(Message.MULTIPLAYER.DENY.parseLine(player));
            deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp deny " + this.player.getName()));
            target.spigot().sendMessage(accept, new TextComponent("   "), deny);
        }
    }

    public void kickPlayer(Player target) {
        PlayerData.getData(this.player).ifPresent(data -> {
            Island island = data.getIsland();
            if (island != null && island.getOwner().equals(this.player)) {
                if (island.getGuests().contains(target)) {
                    island.removeAny(target, true);
                    Message.MULTIPLAYER.KICK.ACTIVE.sendTo(player, target.getName());
                    Message.MULTIPLAYER.KICK.PASSIVE.sendTo(target, player.getName());
                } else {
                    Message.BASIC.PLAYER_NOT_FOUND.sendTo(player);
                }
            } else Message.BASIC.CANNOT_DO_THAT.sendTo(player);
        });
    }

    public void close() {
        SESSIONS.remove(player.getUniqueId(), this);
    }
}
