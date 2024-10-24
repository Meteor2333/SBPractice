package com.meteor.SBPractice.Commands.SubCommands.Multiplayer;

import com.meteor.SBPractice.Commands.MultiplayerCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Utils.NMSSupport;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Leave extends SubCommand {
    public Leave(MultiplayerCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Plot plot = Plot.getPlotByGuest(player);
        if (plot == null) {
            player.sendMessage(Messages.getMessage("cannot-do-that"));
            return;
        }

        if (!Plot.autoAddPlayerFromPlot(player, null, false)) {
            NMSSupport.hidePlayer(player, true);
            player.sendMessage(Messages.getMessage("plot-full"));
            player.teleport(Plot.getPlots().get(0).getSpawnPoint());
        } plot.removeGuest(player);
        Plot.autoAddPlayerFromPlot(player, null, false);
        player.sendMessage(Messages.getMessage("leave").replace("%player%", plot.getPlayer().getName()));
    }
}
