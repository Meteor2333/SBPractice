package com.meteor.SBPractice.Commands.SubCommands.Multiplayer;

import com.meteor.SBPractice.Commands.MultiplayerCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Utils.NMSSupport;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Kick extends SubCommand {
    public Kick(MultiplayerCommand parent, String name) {
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

        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            player.sendMessage(Messages.getMessage("cannot-do-that"));
            return;
        }

        if (!plot.getGuests().contains(target)) {
            player.sendMessage(Messages.getMessage("player-not-in-plot").replace("%player%", target.getName()));
            return;
        }

        if (!Plot.autoAddPlayerFromPlot(player, null, false)) {
            NMSSupport.hidePlayer(player, true);
            player.sendMessage(Messages.getMessage("plot-full"));
            player.teleport(Plot.getPlots().get(0).getSpawnPoint());
        } plot.removeGuest(target);
        player.sendMessage(Messages.getMessage("receiver-kicked").replace("%player%", target.getName()));
        target.sendMessage(Messages.getMessage("victim-kicked").replace("%player%", player.getName()));
    }
}
