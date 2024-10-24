package com.meteor.SBPractice.Commands.SubCommands.Multiplayer;

import com.meteor.SBPractice.Commands.MultiplayerCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Plot;
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

public class Join extends SubCommand {
    // 发出申请请求的玩家, 被此玩家申请到的玩家列表
    public static Map<String, List<String>> joins = new HashMap<>();

    public Join(MultiplayerCommand parent, String name) {
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

        if (joins.getOrDefault(player.getName(), new ArrayList<>()).contains(target.getName()) ||
                Invite.invites.getOrDefault(player.getName(), new ArrayList<>()).contains(target.getName())) {
            player.sendMessage(Messages.getMessage("already-requested").replace("%player%", target.getName()));
            return;
        }

        if (Invite.invites.getOrDefault(target.getName(), new ArrayList<>()).contains(player.getName())) {
            player.sendMessage(Messages.getMessage("anti-conflict"));
            return;
        }

        if (System.currentTimeMillis() - Deny.denys.getOrDefault(target.getName(), new HashMap<>()).getOrDefault(player.getName(), 0L) < 10000) {
            player.sendMessage(Messages.getMessage("denied"));
            return;
        }

        Plot plot = Plot.getPlotByOwner(target);
        if (plot == null) {
            sender.sendMessage(Messages.getMessage("plot-not-found"));
            return;
        }

        plot = Plot.getPlotByGuest(player);
        if (plot != null) {
            if (target.equals(plot.getPlayer()) && plot.getGuests().contains(target)) {
                player.sendMessage(Messages.getMessage("victim-already-in-plot").replace("%player%", target.getName()));
                return;
            }
        }

        List<String> players = joins.getOrDefault(player.getName(), new ArrayList<>());
        players.add(target.getName());
        joins.put(player.getName(), players);
        player.sendMessage(Messages.getMessage("requested").replace("%player%", target.getName()));

        target.sendMessage(Messages.getMessage("join-message").replace("%player%", player.getName()));
        TextComponent accept = new TextComponent(Messages.getMessage("requeste-click-text-accept"));
        accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Messages.getMessage("requeste-hover-text-accept").replace("%player%", player.getName()))));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp accept " + player.getName()));
        TextComponent deny = new TextComponent(Messages.getMessage("requeste-click-text-deny"));
        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(Messages.getMessage("requeste-hover-text-deny").replace("%player%", player.getName()))));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp deny " + player.getName()));
        target.spigot().sendMessage(accept, new TextComponent("   "), deny);

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            List<String> playersfinal = joins.getOrDefault(player.getName(), new ArrayList<>());
            if (!playersfinal.remove(target.getName())) return;
            joins.put(player.getName(), playersfinal);
            player.sendMessage(Messages.getMessage("requeste-expires").replace("%player%", target.getName()));
        }, 1200L);
    }
}
