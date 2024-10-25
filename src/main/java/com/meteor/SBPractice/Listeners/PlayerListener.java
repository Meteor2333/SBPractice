package com.meteor.SBPractice.Listeners;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.SubCommands.Main.Admin;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Utils.NMSSupport;
import com.meteor.SBPractice.Utils.Region;
import com.meteor.SBPractice.Utils.Utils;
import net.md_5.bungee.api.chat.*;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        SBPPlayer player = SBPPlayer.getPlayer(e.getPlayer());
        if (player == null) {
            player = new SBPPlayer(e.getPlayer());
        } player.resetPlayer();

        e.setJoinMessage(null);
        if (Plot.getPlots().isEmpty()) {
            player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "SBPractice" + ChatColor.DARK_GRAY + "]" + ChatColor.RED + " The plugin is not set");
            if (player.getPlayer().isOp()) {
                player.getPlayer().setGameMode(GameMode.CREATIVE);
                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "SBPractice" + ChatColor.DARK_GRAY + "]" + ChatColor.YELLOW + " Please enter /sbp addPlot to add the plot");
                Bukkit.dispatchCommand(player.getPlayer(), "sbp admin");
            } return;
        } if (!Plot.autoAddPlayerFromPlot(player, null, false)) {
            NMSSupport.hidePlayer(player.getPlayer(), true);
            player.sendMessage(Messages.PLOT_FULL.getMessage());
            player.teleport(Plot.getPlots().get(0).getSpawnPoint());
        } Utils.refreshAllPlayerVisibility();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Admin.removeFromAdminList(e.getPlayer().getUniqueId());
        ((CraftPlayer) e.getPlayer()).getHandle().server.getPlayerList().sendAll(
                new PacketPlayOutPlayerInfo(
                        PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, ((CraftPlayer) e.getPlayer()).getHandle()
                ), ((CraftPlayer) e.getPlayer()).getHandle()
        );
        SBPPlayer player = SBPPlayer.getPlayer(e.getPlayer());
        if (player == null) return;
        player.resetPlayer();
        Plot.autoRemovePlayerFromPlot(player);
        SBPPlayer.removePlayer(player);
        Utils.refreshAllPlayerVisibility();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        SBPPlayer player = SBPPlayer.getPlayer(e.getPlayer());
        if (player == null) return;

        if (e.getItem() == null) return;
        if (Admin.check(player)) {
            if (NMSSupport.getTag(e.getItem(), "sbp-setup") == null) return;
            if (NMSSupport.getTag(e.getItem(), "sbp-setup").equals("setup-build-area")) {
                e.setCancelled(true);
                Plot.SetupSession session = Plot.SetupSession.getSessionByPlayer(player.getPlayer());
                if (session == null) return;
                if (session.getSpawnPoint() == null) return;
                if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    player.sendMessage(ChatColor.GREEN + "First point selected!");
                    session.setBuildPos1(e.getClickedBlock().getLocation());
                    player.playSound(Utils.Sounds.ORB_PICKUP);
                } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    player.sendMessage(ChatColor.GREEN + "Second point selected!");
                    session.setBuildPos2(e.getClickedBlock().getLocation());
                    player.playSound(Utils.Sounds.ORB_PICKUP);
                } if (session.getBuildPos1() != null && session.getBuildPos2() != null) {
                    for (ItemStack is : player.getPlayer().getInventory()) {
                        if (is == null) continue;
                        if (NMSSupport.getTag(is, "sbp-setup") == null) continue;
                        is.setType(Material.AIR);
                    } if (!session.save()) {
                        TextComponent text = new TextComponent(ChatColor.YELLOW + "" + ChatColor.BOLD + "[Click Here]");
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "sbp setup spawnpoint"));
                        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click Here").create()));
                        BaseComponent[] bc = TextComponent.fromLegacyText(ChatColor.GREEN + "Stand at the plot spawn point, then ");
                        bc[bc.length - 1].addExtra(text);
                        player.getPlayer().spigot().sendMessage(bc);
                    }
                }
            } return;
        }

        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                e.setCancelled(true);
                return;
            }
        }

        Region region = plot.getRegion();
        int range = Main.getPlugin().getConfig().getInt("plot-check-add-range");
        if (!new Region(
                new Location(region.getWorld(), region.getXMax() + range, 0, region.getZMax() + range),
                new Location(region.getWorld(), region.getXMin() - range, 0, region.getZMin() - range)
        ).isInside(player.getLocation(), true)) {
            e.setCancelled(true);
            player.getPlayer().updateInventory();
            return;
        }

        if (e.getItem().getType().equals(Material.SNOW_BALL)) {
            if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Bukkit.dispatchCommand(player.getPlayer(), "sbp clear");
            }
        }

        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            if (e.getItem().getType().equals(Material.SNOW_BALL)) Bukkit.dispatchCommand(player.getPlayer(), "sbp clear");
            if (e.getItem().getType().equals(Material.EGG)) Bukkit.dispatchCommand(player.getPlayer(), "sbp prestart");
            Arrays.asList("ARMOR_STAND", "MINECART", "BOAT", "MAP", "INK_SACK", "PAINTING", "POTION", "MILK", "ITEM_FRAME")
                    .forEach(type -> {if (e.getItem().getType().toString().contains(type)) e.setCancelled(true);});
        } player.getPlayer().updateInventory();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        SBPPlayer player = SBPPlayer.getPlayer(e.getPlayer());
        if (player == null) return;
        if (Admin.check(player)) return;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) return;
        } if (e.getTo().getY() <= 0) player.teleport(plot.getSpawnPoint());

        Region region = plot.getRegion();
        int range = Main.getPlugin().getConfig().getInt("plot-check-add-range");
        if (new Region(
                new Location(region.getWorld(), region.getXMax() + range, 0, region.getZMax() + range),
                new Location(region.getWorld(), region.getXMin() - range, 0, region.getZMin() - range)
        ).isInside(e.getTo(), true)) NMSSupport.showPlayer(player.getPlayer(), true);
        else NMSSupport.hidePlayer(player.getPlayer(), true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        SBPPlayer player = SBPPlayer.getPlayer((Player) e.getWhoClicked());
        if (player == null) return;
        if (Admin.check(player)) return;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                e.setCancelled(true);
                player.getPlayer().updateInventory();
                return;
            }
        }

        Region region = plot.getRegion();
        int range = Main.getPlugin().getConfig().getInt("plot-check-add-range");
        if (!new Region(
                new Location(region.getWorld(), region.getXMax() + range, 0, region.getZMax() + range),
                new Location(region.getWorld(), region.getXMin() - range, 0, region.getZMin() - range)
        ).isInside(player.getLocation(), true)) {
            e.setCancelled(true);
            player.getPlayer().updateInventory();
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
        if (!e.isFlying()) return;
        SBPPlayer player = SBPPlayer.getPlayer(e.getPlayer());
        if (player == null) return;
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) return;
        }

        Region region = plot.getRegion();
        if (!new Region(
                new Location(region.getWorld(), region.getXMax() + 1, region.getYMax(), region.getZMax() + 1),
                new Location(region.getWorld(), region.getXMin() - 1, region.getYMin(), region.getZMin() - 1)
        ).isInside(player.getLocation(), true)) return;

        if (player.isEnableHighjump()) {
            e.setCancelled(true);
            if (System.currentTimeMillis() - player.getHighjumpCooldown() >= 1000) {
                player.setHighjumpCooldown(System.currentTimeMillis());

                player.playSound(Utils.Sounds.BLAZE_SHOOT);
                player.getPlayer().setVelocity((new Vector(0, 1, 0)).multiply(player.getHighjumpIntensity()));

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.getLocation().add(0, -1, 0).getBlock().getType() == Material.AIR) player.getPlayer().setAllowFlight(false);
                        else {
                            player.getPlayer().setAllowFlight(true);
                            this.cancel();
                        }
                    }
                }.runTaskTimer(Main.getPlugin(), 0L, 1L);
            }
        }
    }

    @EventHandler
    public void onPlayerStatisticIncrement(PlayerStatisticIncrementEvent e) {
        SBPPlayer player = SBPPlayer.getPlayer(e.getPlayer());
        if (player == null) return;
        switch (e.getStatistic()) {
            case JUMP:
                player.getStats().setJumps(e.getNewValue());
                break;
            case PLAY_ONE_TICK:
                player.getStats().setOnlineTimes((int) (TimeUnit.SECONDS.toHours(e.getNewValue() / 20)));
            default:
        }
    }

    @EventHandler
    public void onEntityDamageDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) e.setCancelled(true);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof Snowball || e.getEntity() instanceof Egg) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(), () -> e.getEntity().remove(), 3L);
        } else e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent e) {
        e.setCancelled(true);
    }
}
