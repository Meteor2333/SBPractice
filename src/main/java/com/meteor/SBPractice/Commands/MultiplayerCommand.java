package com.meteor.SBPractice.Commands;

import com.meteor.SBPractice.Commands.SubCommands.Admin;
import com.meteor.SBPractice.Commands.SubCommands.Spectator;
import com.meteor.SBPractice.Main;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.PlotStatus;
import com.meteor.SBPractice.Utils.ItemStackBuilder;
import com.meteor.SBPractice.Utils.Message;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.*;

public class MultiplayerCommand extends BukkitCommand {
    private static Map<UUID, ArrayList<UUID>> applyList = new HashMap<>();
    private static Map<UUID, ArrayList<UUID>> deniedList = new HashMap<>();

    public MultiplayerCommand(String name) {
        super(name);
        setAliases(List.of("multiplayer"));
    }

    @Override
    public boolean execute(CommandSender sender, String s,  String[] args) {
        Player player = (Player) sender;
        Plot plot = Plot.getPlotByPlayer(player);

        //Bugs #4
        if (args.length == 0) return true;
        if (args.length == 1 && !args[0].equalsIgnoreCase("leave")) return true;
        switch (args[0]) {
            case "accept" -> {
                Player targetPlayer = Bukkit.getPlayer(UUID.fromString(args[1]));
                if (targetPlayer == null) return true;
                if (!(applyList.getOrDefault(player.getUniqueId(), new ArrayList<>()).contains(targetPlayer.getUniqueId()))) {
                    player.sendMessage(Message.getMessage("no-apply"));
                    return true;
                }
                ArrayList<UUID> ca = applyList.getOrDefault(player.getUniqueId(), new ArrayList<>());
                ca.remove(targetPlayer.getUniqueId());
                applyList.put(player.getUniqueId(), ca);
                if (plot == null) {
                    player.sendMessage(Message.getMessage("not-in-plot"));
                    return true;
                }
                Plot targetPlot = Plot.getPlotByPlayer(targetPlayer);
                plot.addGuest(targetPlayer);
                targetPlayer.getInventory().setArmorContents(null);
                targetPlayer.getInventory().clear();
                targetPlayer.getInventory().setItem(8, new ItemStackBuilder(Material.SNOW_BALL).toItemStack());
                targetPlayer.updateInventory();
                targetPlayer.setAllowFlight(true);
                targetPlayer.setExp(0.0F);
                targetPlayer.setFireTicks(0);
                targetPlayer.setFlying(false);
                targetPlayer.setFoodLevel(20);
                targetPlayer.setGameMode(GameMode.CREATIVE);
                targetPlayer.setHealth(20.0D);
                targetPlayer.setLevel(0);
                Admin.removeFromAdminList(targetPlayer.getUniqueId());
                Spectator.removeFromSpecList(targetPlayer.getUniqueId());
                if (targetPlot != null) {
                    int x1 = Math.min((int) targetPlot.getFirstPoint().getX(), (int) targetPlot.getSecondPoint().getX());
                    int y1 = Math.min((int) targetPlot.getFirstPoint().getY(), (int) targetPlot.getSecondPoint().getY());
                    int z1 = Math.min((int) targetPlot.getFirstPoint().getZ(), (int) targetPlot.getSecondPoint().getZ());
                    int x2 = Math.max((int) targetPlot.getFirstPoint().getX(), (int) targetPlot.getSecondPoint().getX());
                    int y2 = Math.max((int) targetPlot.getFirstPoint().getY(), (int) targetPlot.getSecondPoint().getY());
                    int z2 = Math.max((int) targetPlot.getFirstPoint().getZ(), (int) targetPlot.getSecondPoint().getZ());
                    for (int i = x1; i <= x2; i++) {
                        for (int j = y1; j <= y2; j++) {
                            for (int k = z1; k <= z2; k++) {
                                if (j == y1) {
                                    targetPlot.getSpawnPoint().getWorld().getBlockAt(i, j - 1, k).setType(Material.valueOf(Main.getPlugin().getConfig().getString("default-platform-block")));
                                }
                                targetPlot.getSpawnPoint().getWorld().getBlockAt(i, j, k).setType(Material.AIR);
                            }
                        }
                    }
                    targetPlot.setPlotStatus(PlotStatus.NOT_OCCUPIED);
                    targetPlot.outAction();
                    targetPlot.stopTimer();
                    targetPlot.setTime(0D);
                    targetPlot.canStart(true);
                    targetPlot.setPlayer(null);
                }
                targetPlayer.teleport(player);
                player.sendMessage(Message.getMessage("apply-accept-chat").replace("%player%", targetPlayer.getName()));
                targetPlayer.sendMessage(Message.getMessage("apply-accept-target-chat").replace("%player%", player.getName()));
            }
            case "deny" -> {
                Player targetPlayer = Bukkit.getPlayer(UUID.fromString(args[1]));
                if (targetPlayer == null) return true;
                if (!(applyList.getOrDefault(player.getUniqueId(), new ArrayList<>()).contains(targetPlayer.getUniqueId()))) {
                    player.sendMessage(Message.getMessage("no-apply"));
                    return true;
                }
                ArrayList<UUID> ca = applyList.getOrDefault(player.getUniqueId(), new ArrayList<>());
                ca.remove(targetPlayer.getUniqueId());
                applyList.put(player.getUniqueId(), ca);
                ArrayList<UUID> cac = deniedList.getOrDefault(targetPlayer.getUniqueId(), new ArrayList<>());
                cac.add(player.getUniqueId());
                deniedList.put(targetPlayer.getUniqueId(), cac);
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                    ArrayList<UUID> cach = deniedList.getOrDefault(targetPlayer.getUniqueId(), new ArrayList<>());
                    cach.remove(player.getUniqueId());
                    deniedList.put(targetPlayer.getUniqueId(), cach);
                }, 600L);
                player.sendMessage(Message.getMessage("apply-deny-chat").replace("%player%", targetPlayer.getName()));
                targetPlayer.sendMessage(Message.getMessage("apply-deny-target-chat").replace("%player%", player.getName()));
            }
            case "apply" -> {
                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null || Plot.getPlotByPlayer(targetPlayer) == null) {
                    player.sendMessage(Message.getMessage("apply-player-offline"));
                    return true;
                } if (deniedList.getOrDefault(player.getUniqueId(), new ArrayList<>()).contains(targetPlayer.getUniqueId())) {
                    player.sendMessage(Message.getMessage("denied"));
                    return true;
                } if (applyList.getOrDefault(targetPlayer.getUniqueId(), new ArrayList<>()).contains(player.getUniqueId())) {
                    player.sendMessage(Message.getMessage("already-apply"));
                    return true;
                }

                Plot targetPlot = Plot.getPlotByPlayer(targetPlayer);
                if (targetPlot == null) return true;
                TextComponent apply = new TextComponent(Message.getMessage("apply-target").replace("%player%", player.getName()));
                TextComponent accept = new TextComponent(Message.getMessage("apply-accept"));
                accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp accept " + player.getUniqueId().toString()));
                accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Message.getMessage("apply-accept-showtext")).create()));
                TextComponent deny = new TextComponent(Message.getMessage("apply-deny"));
                deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mp deny " + player.getUniqueId().toString()));
                deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Message.getMessage("apply-deny-showtext")).create()));
                targetPlayer.spigot().sendMessage(apply);
                targetPlayer.spigot().sendMessage(accept, new TextComponent("  "), deny);
                player.sendMessage(Message.getMessage("apply").replace("%player%", targetPlayer.getName()));
                ArrayList<UUID> ca = applyList.getOrDefault(targetPlayer.getUniqueId(), new ArrayList<>());
                ca.add(player.getUniqueId());
                applyList.put(targetPlayer.getUniqueId(), ca);
                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                    ArrayList<UUID> cac = applyList.getOrDefault(targetPlayer.getUniqueId(), new ArrayList<>());
                    if (cac.remove(player.getUniqueId())) {
                        targetPlayer.sendMessage(Message.getMessage("apply-lapsed").replace("%player%", player.getName()));
                    }
                    applyList.put(targetPlayer.getUniqueId(), cac);
                }, 600L);
                return true;
            }
            case "leave" -> {
                Plot pl = Plot.getPlotByGuest(player);
                if (pl == null) {
                    pl = Plot.getPlotByPlayer(player);
                    if (pl == null) player.sendMessage(Message.getMessage("not-in-plot"));
                    else player.sendMessage(Message.getMessage("cannot-leave"));
                    return true;
                } pl.removeGuest(player);
                for (Plot plot0 : Plot.getPlots()) {
                    if (plot0.getPlotStatus().equals(PlotStatus.NOT_OCCUPIED)) {
                        plot0.setPlotStatus(PlotStatus.OCCUPIED);
                        plot0.setPlayer(player);
                        plot0.stopTimer();
                        plot0.setTime(0D);
                        plot0.canStart(true);
                        player.getInventory().setArmorContents(null);
                        player.getInventory().clear();
                        player.getInventory().setItem(8, new ItemStackBuilder(Material.SNOW_BALL).toItemStack());
                        player.updateInventory();
                        player.setAllowFlight(true);
                        player.setExp(0.0F);
                        player.setFireTicks(0);
                        player.setFlying(false);
                        player.setFoodLevel(20);
                        player.setGameMode(GameMode.CREATIVE);
                        player.setHealth(20.0D);
                        player.setLevel(0);
                        int x1 = Math.min((int) plot0.getFirstPoint().getX(), (int) plot0.getSecondPoint().getX());
                        int y1 = Math.min((int) plot0.getFirstPoint().getY(), (int) plot0.getSecondPoint().getY());
                        int z1 = Math.min((int) plot0.getFirstPoint().getZ(), (int) plot0.getSecondPoint().getZ());
                        int x2 = Math.max((int) plot0.getFirstPoint().getX(), (int) plot0.getSecondPoint().getX());
                        int y2 = Math.max((int) plot0.getFirstPoint().getY(), (int) plot0.getSecondPoint().getY());
                        int z2 = Math.max((int) plot0.getFirstPoint().getZ(), (int) plot0.getSecondPoint().getZ());

                        List<BlockState> blocks = new ArrayList<>();
                        for (int i = x1; i <= x2; i++) {
                            for (int j = y1; j <= y2; j++) {
                                for (int k = z1; k <= z2; k++) {
                                    Block block = plot0.getSpawnPoint().getWorld().getBlockAt(i, j, k);
                                    blocks.add(block.getState());
                                }
                            }
                        }
                        plot0.setBufferBuildBlock(blocks);
                        plot0.displayAction();
                        player.teleport(plot0.getSpawnPoint());
                        return true;
                    }
                } player.sendMessage(Message.getMessage("plot-full"));
                Bukkit.dispatchCommand(player, "sbp spec");
            }
            case "kick" -> {
                Plot pl = Plot.getPlotByPlayer(player);
                if (pl == null) {
                    pl = Plot.getPlotByGuest(player);
                    if (pl == null) player.sendMessage(Message.getMessage("not-in-plot"));
                    else player.sendMessage(Message.getMessage("no-your-plot"));
                    return true;
                } if (pl.getGuests().contains(Bukkit.getPlayer(args[1]))) {
                    player.sendMessage(Message.getMessage("successful-kick-plot").replace("%player%", args[1]));
                    Bukkit.getPlayer(args[1]).sendMessage(Message.getMessage("kick-from-plot"));
                    Bukkit.dispatchCommand(Bukkit.getPlayer(args[1]), "mp leave");
                } else player.sendMessage(Message.getMessage("no-in-your-plot"));
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) return null;
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            tab.add("apply");
            tab.add("leave");
            tab.add("kick");
            tab.removeIf(filter -> !filter.toLowerCase().startsWith(args[0].toLowerCase()) || filter.equals(sender.getName()));
        } else if (args.length == 2 && (args[0].equals("apply") || args[0].equals("kick"))) {
            for (Player player : Bukkit.getOnlinePlayers()) tab.add(player.getName());
            tab.removeIf(filter -> !filter.toLowerCase().startsWith(args[1].toLowerCase()) || filter.equals(sender.getName()));
        } else return null;
        return tab;
    }
}
