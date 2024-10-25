package com.meteor.SBPractice.Commands.SubCommands.Multiplayer;

import com.meteor.SBPractice.Api.SBPPlayer;
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

        if (Invite.invites.getOrDefault(target.getName(), new ArrayList<>()).contains(player.getName())) {
            List<String> players = Invite.invites.getOrDefault(target.getName(), new ArrayList<>());
            players.remove(player.getName());
            Invite.invites.put(target.getName(), players);

            player.sendMessage(Messages.RECEIVER_ACCEPTED.getMessage().replace("%player%", target.getName()));
            target.sendMessage(Messages.VICTIM_ACCEPTED.getMessage().replace("%player%", player.getName()));

            Plot plot = Plot.getPlotByOwner(target);
            if (plot == null) {
                player.sendMessage(Messages.PLOT_NOT_FOUND.getMessage());
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

            player.sendMessage(Messages.RECEIVER_ACCEPTED.getMessage().replace("%player%", target.getName()));
            target.sendMessage(Messages.VICTIM_ACCEPTED.getMessage().replace("%player%", player.getName()));

            Plot plot = Plot.getPlotByOwner(player);
            if (plot == null) {
                target.sendMessage(Messages.PLOT_NOT_FOUND.getMessage());
                player.sendMessage(Messages.CANNOT_DO_THAT.getMessage());
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
            player.sendMessage(Messages.REQUEST_NOT_FOUND.getMessage().replace("%player%", target.getName()));
        }
    }
}
