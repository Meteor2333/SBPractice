package com.meteor.SBPractice.Commands.SubCommands.Multiplayer;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.MultiplayerCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Invite extends SubCommand {
    // 发起邀请请求的玩家, 被此玩家邀请到的玩家列表
    public static Map<String, List<String>> invites = new HashMap<>();

    public Invite(MultiplayerCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        SBPPlayer player = SBPPlayer.getPlayer((Player) sender);
        if (player == null) return;
        if (args.length == 0) {
            Bukkit.dispatchCommand(player.getPlayer(), "sbp help");
            return;
        }

        SBPPlayer target = SBPPlayer.getPlayer(Bukkit.getPlayer(args[0]));
        if (target == null) {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND.getMessage());
            return;
        }

        if (target.getName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(Messages.CANNOT_DO_THAT.getMessage());
            return;
        }

        if (invites.getOrDefault(player.getName(), new ArrayList<>()).contains(target.getName()) ||
                Join.joins.getOrDefault(player.getName(), new ArrayList<>()).contains(target.getName())) {
            player.sendMessage(Messages.ALREADY_REQUESTED.getMessage().replace("%player%", target.getName()));
            return;
        }

        if (Join.joins.getOrDefault(target.getName(), new ArrayList<>()).contains(player.getName())) {
            player.sendMessage(Messages.ANTI_CONFLICT.getMessage());
            return;
        }

        if (System.currentTimeMillis() - Deny.denys.getOrDefault(target.getName(), new HashMap<>()).getOrDefault(player.getName(), 0L) < 60000) {
            player.sendMessage(Messages.DENIED.getMessage());
            return;
        }

        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            player.sendMessage(Messages.CANNOT_DO_THAT.getMessage());
            return;
        }

        if (plot.getGuests().contains(target)) {
            player.sendMessage(Messages.RECEIVER_ALREADY_IN_PLOT.getMessage().replace("%player%", target.getName()));
            return;
        }

        List<String> players = invites.getOrDefault(player.getName(), new ArrayList<>());
        players.add(target.getName());
        invites.put(player.getName(), players);
        player.sendMessage(Messages.REQUESTED.getMessage().replace("%player%", target.getName()));

        target.sendMessage(Messages.INVITE_MESSAGE.getMessage().replace("%player%", player.getName()));
        TextComponent accept = new TextComponent(Messages.REQUEST_CLICK_TEXT_ACCEPT.getMessage());
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Messages.REQUEST_HOVER_TEXT_ACCEPT.getMessage().replace("%player%", player.getName()))));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp accept " + player.getName()));
        TextComponent deny = new TextComponent(Messages.REQUEST_CLICK_TEXT_DENY.getMessage());
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Messages.REQUEST_HOVER_TEXT_DENY.getMessage().replace("%player%", player.getName()))));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp deny " + player.getName()));
        target.getPlayer().spigot().sendMessage(accept, new TextComponent("   "), deny);

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            List<String> playersfinal = invites.getOrDefault(player.getName(), new ArrayList<>());
            if (!playersfinal.remove(target.getName())) return;
            invites.put(player.getName(), playersfinal);
            player.sendMessage(Messages.REQUEST_EXPIRES.getMessage().replace("%player%", target.getName()));
        }, 1200L);
    }
}
