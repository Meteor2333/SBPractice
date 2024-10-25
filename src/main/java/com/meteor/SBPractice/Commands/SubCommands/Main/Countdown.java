package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Countdown extends SubCommand {
    public Countdown(MainCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        SBPPlayer player = SBPPlayer.getPlayer((Player) sender);
        if (player == null) return;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            player.sendMessage(Messages.CANNOT_DO_THAT.getMessage());
            return;
        }

        player.sendMessage(Messages.COUNTDOWN_SET_HINT.getMessage());
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            void onPlayerChat(AsyncPlayerChatEvent e) {
                if (!e.getPlayer().equals(player.getPlayer())) return;
                e.setCancelled(true);
                HandlerList.unregisterAll(this);
                try {
                    int time = Integer.parseInt(e.getMessage());
                    if (player.equals(plot.getPlayer())) {
                        if (time != 0) player.sendMessage(Messages.COUNTDOWN_ENABLED.getMessage().replace("%time%", String.valueOf(time)));
                        else player.sendMessage(Messages.COUNTDOWN_DISABLED.getMessage());
                        plot.setCountdown(time);
                    }
                    else player.sendMessage(Messages.CANNOT_DO_THAT.getMessage());
                } catch (NumberFormatException ex) {
                    player.sendMessage(Messages.CANNOT_DO_THAT.getMessage());
                }
            }


            @EventHandler
            void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
                if (!e.getPlayer().equals(player)) return;
                HandlerList.unregisterAll(this);
            }

            @EventHandler
            void onPlayerQuit(PlayerQuitEvent e) {
                if (!e.getPlayer().equals(player)) return;
                HandlerList.unregisterAll(this);
            }
        }, Main.getPlugin());
    }
}
