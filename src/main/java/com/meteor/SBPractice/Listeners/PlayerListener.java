package com.meteor.SBPractice.Listeners;

import com.meteor.SBPractice.Commands.SubCommands.Main.Admin;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Utils.NMSSupport;
import com.meteor.SBPractice.Utils.Region;
import com.meteor.SBPractice.Utils.Utils;
import net.md_5.bungee.api.chat.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class PlayerListener implements Listener {
    public static Map<UUID, Double> HighjumpIntensity = new HashMap<>();
    public static Map<UUID, Boolean> HighjumpToggled = new HashMap<>();
    public static final ArrayList<String> HighjumpCooldowns = new ArrayList<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Utils.updatePlots();
        Player player = e.getPlayer();
        e.setJoinMessage(null);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        if (Plot.getPlots().isEmpty()) {
            player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "SBPractice" + ChatColor.DARK_GRAY + "]" + ChatColor.RED + " The plugin is not set");
            if (player.isOp()) {
                player.setGameMode(GameMode.CREATIVE);
                player.setFlying(true);
                player.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "SBPractice" + ChatColor.DARK_GRAY + "]" + ChatColor.YELLOW + " Please enter /sbp addPlot to add the plot");
                Bukkit.dispatchCommand(player, "sbp admin");
            } return;
        } if (!Plot.autoAddPlayerFromPlot(player, null, false)) {
            NMSSupport.hidePlayer(player, true);
            player.sendMessage(Messages.getMessage("plot-full"));
            player.teleport(Plot.getPlots().get(0).getSpawnPoint());
        } Utils.refreshAllPlayerVisibility();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Utils.refreshAllPlayerVisibility();
        Player player = e.getPlayer();
        Admin.removeFromAdminList(player.getUniqueId());
        Plot.autoRemovePlayerFromPlot(player);
        Utils.updatePlots();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Plot plot = Plot.getPlotByOwner(player);
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) {
                e.setCancelled(true);
                return;
            }
        }

        if (e.getItem() == null) return;
        if (Admin.check(player)) {
            if (NMSSupport.getTag(e.getItem(), "sbp-setup") == null) return;
            if (NMSSupport.getTag(e.getItem(), "sbp-setup").equals("setup-build-area")) {
                e.setCancelled(true);
                Plot.SetupSession session = Plot.SetupSession.getSessionByPlayer(player);
                if (session == null) return;
                if (session.getSpawnPoint() == null) return;
                if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    player.sendMessage(ChatColor.GREEN + "First point selected!");
                    session.setBuildAreaPos1(e.getClickedBlock().getLocation());
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
                } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    player.sendMessage(ChatColor.GREEN + "Second point selected!");
                    session.setBuildAreaPos2(e.getClickedBlock().getLocation());
                    player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
                } if (session.getBuildAreaPos1() != null && session.getBuildAreaPos2() != null) {
                    for (ItemStack is : player.getInventory()) {
                        if (is == null) continue;
                        if (NMSSupport.getTag(is, "sbp-setup") == null) continue;
                        is.setType(Material.AIR);
                    } if (!session.save()) {
                        TextComponent text = new TextComponent(ChatColor.YELLOW + "" + ChatColor.BOLD + "[Click Here]");
                        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "sbp setup spawnpoint"));
                        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click Here").create()));
                        BaseComponent[] bc = TextComponent.fromLegacyText(ChatColor.GREEN + "Stand at the plot spawn point, then ");
                        bc[bc.length - 1].addExtra(text);
                        player.spigot().sendMessage(bc);
                    }
                }
            } return;
        }

        Region region = plot.getRegion();
        int range = Main.getPlugin().getConfig().getInt("plot-check-add-range");
        if (!new Region(
                new Location(region.getWorld(), region.getXMax() + range, 0, region.getZMax() + range),
                new Location(region.getWorld(), region.getXMin() - range, 0, region.getZMin() - range)
        ).isInside(player.getLocation(), true)) {
            e.setCancelled(true);
            player.updateInventory();
            return;
        }

        if (e.getItem().getType().equals(Material.SNOW_BALL) || e.getItem().getType().equals(Material.EGG)) {
            if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                Bukkit.dispatchCommand(player, "sbp clear");
            }
        }

        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            Arrays.asList("ARMOR_STAND", "MINECART", "BOAT", "INK_SACK", "PAINTING", "POTION", "MILK")
                    .forEach(type -> {if (e.getItem().getType().toString().contains(type)) e.setCancelled(true);});
        } player.updateInventory();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
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
        ).isInside(e.getTo(), true)) NMSSupport.showPlayer(player, true);
        else NMSSupport.hidePlayer(player, true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (Admin.check(player)) return;
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
        ).isInside(player.getLocation(), true)) e.setCancelled(true);
        player.updateInventory();
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
        if (!e.isFlying()) return;
        Player player = e.getPlayer();
        Plot plot = Plot.getPlotByOwner(e.getPlayer());
        if (plot == null) {
            plot = Plot.getPlotByGuest(player);
            if (plot == null) return;
        }

        Region region = plot.getRegion();
        if (!new Region(
                new Location(region.getWorld(), region.getXMax() + 1, region.getYMax(), region.getZMax() + 1),
                new Location(region.getWorld(), region.getXMin() - 1, region.getYMin(), region.getZMin() - 1)
        ).isInside(player.getLocation(), true)) return;

        if (HighjumpToggled.getOrDefault(player.getUniqueId(), true)) {
            if (!HighjumpCooldowns.contains(player.getName())) {
                HighjumpCooldowns.add(player.getName());
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> HighjumpCooldowns.remove(player.getName()), 20L);

                player.playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1F, 1F);
                player.setVelocity((new Vector(0, 1, 0)).multiply(HighjumpIntensity.getOrDefault(player.getUniqueId(), 1.05D)));
                e.setCancelled(true);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.getLocation().add(0, -1, 0).getBlock().getType() == Material.AIR) player.setAllowFlight(false);
                        else {
                            player.setAllowFlight(true);
                            this.cancel();
                        }
                    }
                }.runTaskTimer(Main.getPlugin(), 0L, 1L);
            }
        }
    }
}
