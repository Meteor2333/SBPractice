package com.meteor.SBPractice.Commands.SubCommands.Multiplayer;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.MultiplayerCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Plot;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Leave extends SubCommand {
    public Leave(MultiplayerCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        SBPPlayer player = SBPPlayer.getPlayer((Player) sender);
        if (player == null) return;
        Plot plot = Plot.getPlotByGuest(player);
        if (plot == null) {
            plot = Plot.getPlotByOwner(player);
            if (plot == null) player.sendMessage(Messages.CANNOT_DO_THAT.getMessage());
            else player.getPlayer().teleport(plot.getSpawnPoint());
            return;
        }

        player.sendMessage(Messages.LEAVE.getMessage().replace("%player%", plot.getPlayer().getName()));
        plot.removeGuest(player);

        if (!Plot.autoAddPlayerFromPlot(player, null, false)) {
            player.setVisibility(false);
            player.sendMessage(Messages.PLOT_FULL.getMessage());
            player.teleport(Plot.getPlots().get(0).getSpawnPoint());
        }
    }
}
