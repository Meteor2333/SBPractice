package com.meteor.SBPractice.Commands.SubCommands.Multiplayer;

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
        Player player = (Player) sender;
        if (args.length == 0) {
            Bukkit.dispatchCommand(player, "sbp help");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Messages.getMessage("player-not-found"));
            return;
        }

        if (target.getName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(Messages.getMessage("cannot-do-that"));
            return;
        }

        if (invites.getOrDefault(player.getName(), new ArrayList<>()).contains(target.getName()) ||
                Join.joins.getOrDefault(player.getName(), new ArrayList<>()).contains(target.getName())) {
            player.sendMessage(Messages.getMessage("already-requested").replace("%player%", target.getName()));
            return;
        }

        if (Join.joins.getOrDefault(target.getName(), new ArrayList<>()).contains(player.getName())) {
            player.sendMessage(Messages.getMessage("anti-conflict"));
            return;
        }

        if (System.currentTimeMillis() - Deny.denys.getOrDefault(target.getName(), new HashMap<>()).getOrDefault(player.getName(), 0L) < 10000) {
            player.sendMessage(Messages.getMessage("denied"));
            return;
        }

        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            player.sendMessage(Messages.getMessage("cannot-do-that"));
            return;
        }

        if (plot.getGuests().contains(target)) {
            player.sendMessage(Messages.getMessage("receiver-already-in-plot").replace("%player%", target.getName()));
            return;
        }

        List<String> players = invites.getOrDefault(player.getName(), new ArrayList<>());
        players.add(target.getName());
        invites.put(player.getName(), players);
        player.sendMessage(Messages.getMessage("requested").replace("%player%", target.getName()));

        target.sendMessage(Messages.getMessage("invite-message").replace("%player%", player.getName()));
        TextComponent accept = new TextComponent(Messages.getMessage("requeste-click-text-accept"));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Messages.getMessage("requeste-hover-text-accept").replace("%player%", player.getName()))));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp accept " + player.getName()));
        TextComponent deny = new TextComponent(Messages.getMessage("requeste-click-text-deny"));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Messages.getMessage("requeste-hover-text-deny").replace("%player%", player.getName()))));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp deny " + player.getName()));
        target.spigot().sendMessage(accept, new TextComponent("   "), deny);

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            List<String> playersfinal = invites.getOrDefault(player.getName(), new ArrayList<>());
            if (!playersfinal.remove(target.getName())) return;
            invites.put(player.getName(), playersfinal);
            player.sendMessage(Messages.getMessage("requeste-expires").replace("%player%", target.getName()));
        }, 1200L);
    }
}
