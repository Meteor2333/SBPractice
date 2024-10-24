package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Utils.NMSSupport;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AddPlot extends SubCommand {
    public AddPlot(MainCommand parent, String name) {
        super(parent, name, true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (!Admin.check(player)) {
            player.sendMessage(ChatColor.RED + "Please switch to admin mode first!");
            return;
        } Plot.SetupSession session = Plot.SetupSession.getSessionByPlayer(player);
        if (args.length == 1 && args[0].equals("spawnpoint")) {
            if (session == null) return;
            session.setSpawnPoint(player.getLocation());
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
            if (session.save()) return;
            player.sendMessage(ChatColor.GREEN + "Use the iron axe in the first slot of the hotbar to set the build area!");

            ItemStack is = new ItemStack(Material.IRON_AXE);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Select two points");
            is.setItemMeta(im);
            player.getInventory().setItem(0, NMSSupport.setTag(is, "sbp-setup", "setup-build-area"));
        } else {

            if (session != null) {
                player.sendMessage(Messages.getMessage("cannot-do-that"));
                return;
            } new Plot.SetupSession(player);
            TextComponent text = new TextComponent(ChatColor.YELLOW + "" + ChatColor.BOLD + "[Click Here]");
            text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sbp setup spawnpoint"));
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click Here").create()));
            BaseComponent[] bc = TextComponent.fromLegacyText(ChatColor.GREEN + "Stand at the plot spawn point, then ");
            bc[bc.length - 1].addExtra(text);
            player.spigot().sendMessage(bc);
            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
        }
    }
}
