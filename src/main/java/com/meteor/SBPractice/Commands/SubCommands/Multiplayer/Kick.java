package com.meteor.SBPractice.Commands.SubCommands.Multiplayer;

import com.meteor.SBPractice.Api.SBPPlayer;
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

        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            player.sendMessage(Messages.CANNOT_DO_THAT.getMessage());
            return;
        }

        if (!plot.getGuests().contains(target)) {
            player.sendMessage(Messages.PLAYER_NOT_IN_PLOT.getMessage().replace("%player%", target.getName()));
            return;
        }

        player.sendMessage(Messages.RECEIVER_KICKED.getMessage().replace("%player%", target.getName()));
        target.sendMessage(Messages.VICTIM_KICKED.getMessage().replace("%player%", player.getName()));
        plot.removeGuest(target);

        if (!Plot.autoAddPlayerFromPlot(target, null, false)) {
            NMSSupport.hidePlayer(target.getPlayer(), true);
            target.sendMessage(Messages.PLOT_FULL.getMessage());
            target.teleport(Plot.getPlots().get(0).getSpawnPoint());
        }
    }
}
