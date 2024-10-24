package com.meteor.SBPractice.Commands.SubCommands.Multiplayer;

import com.meteor.SBPractice.Commands.MultiplayerCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Accept extends SubCommand {
    public Accept(MultiplayerCommand parent, String name) {
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

        if (Invite.invites.getOrDefault(target.getName(), new ArrayList<>()).contains(player.getName())) {
            List<String> players = Invite.invites.getOrDefault(target.getName(), new ArrayList<>());
            players.remove(player.getName());
            Invite.invites.put(target.getName(), players);

            player.sendMessage(Messages.getMessage("receiver-accepted").replace("%player%", target.getName()));
            target.sendMessage(Messages.getMessage("victim-accepted").replace("%player%", player.getName()));

            Plot plot = Plot.getPlotByOwner(target);
            if (plot == null) {
                player.sendMessage(Messages.getMessage("plot-not-found"));
                return;
            }

            Plot p = Plot.getPlotByOwner(player);
            if (p != null) Utils.resetPlot(p);
            else {
                p = Plot.getPlotByGuest(player);
                if (p != null) p.removeGuest(player);
            }

            Plot.autoAddPlayerFromPlot(player, plot, true);
        } else if (Join.joins.getOrDefault(target.getName(), new ArrayList<>()).contains(player.getName())) {
            List<String> players = Join.joins.getOrDefault(target.getName(), new ArrayList<>());
            players.remove(player.getName());
            Join.joins.put(target.getName(), players);

            player.sendMessage(Messages.getMessage("receiver-accepted").replace("%player%", target.getName()));
            target.sendMessage(Messages.getMessage("victim-accepted").replace("%player%", player.getName()));

            Plot plot = Plot.getPlotByOwner(player);
            if (plot == null) {
                target.sendMessage(Messages.getMessage("plot-not-found"));
                player.sendMessage(Messages.getMessage("cannot-do-that"));
                return;
            }

            Plot p = Plot.getPlotByOwner(target);
            if (p != null) Utils.resetPlot(p);
            else {
                p = Plot.getPlotByGuest(target);
                if (p != null) p.removeGuest(target);
            }

            Plot.autoAddPlayerFromPlot(target, plot, true);
        } else {
            player.sendMessage(Messages.getMessage("requeste-not-found").replace("%player%", target.getName()));
        }
    }
}
