package com.meteor.SBPractice.Commands.SubCommands.Main;

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
        Player player = (Player) sender;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            player.sendMessage(Messages.getMessage("cannot-do-that"));
            return;
        }

        player.sendMessage(Messages.getMessage("set-countdown-hint"));
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            void onPlayerChat(AsyncPlayerChatEvent e) {
                if (!e.getPlayer().equals(player)) return;
                e.setCancelled(true);
                HandlerList.unregisterAll(this);
                try {
                    int time = Integer.parseInt(e.getMessage());
                    if (player.equals(plot.getPlayer())) {
                        if (time != 0) player.sendMessage(Messages.getMessage("countdown-enabled").replace("%time%", String.valueOf(time)));
                        else player.sendMessage(Messages.getMessage("countdown-disabled"));
                        plot.setCountdown(time);
                    }
                    else player.sendMessage(Messages.getMessage("cannot-do-that"));
                } catch (NumberFormatException ex) {
                    player.sendMessage(Messages.getMessage("cannot-do-that"));
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
