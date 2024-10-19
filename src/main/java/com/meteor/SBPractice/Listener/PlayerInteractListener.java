package com.meteor.SBPractice.Listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;

public class PlayerInteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (e.getItem() == null) return;
        if (e.getItem().getType().toString().contains("POTION")) e.setCancelled(true);
        if (e.getItem().getType().equals(Material.SNOW_BALL) || e.getItem().getType().equals(Material.EGG)) {
            if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Bukkit.dispatchCommand(player, "sbp clear");
            }
        }

        //Bugs #1 <anti ojang>//////////////////////////////////////

        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            Arrays.asList("ARMOR_STAND", "MINECART", "BOAT", "INK_SACK")
                .forEach(type -> {if (e.getItem().getType().toString().contains(type)) e.setCancelled(true);});
        }



        ////////////////////////////////////////////////////////////
    }
}
