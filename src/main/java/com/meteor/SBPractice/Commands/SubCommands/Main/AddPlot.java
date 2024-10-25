package com.meteor.SBPractice.Commands.SubCommands.Main;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Commands.MainCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Messages;
import com.meteor.SBPractice.Plot;
import com.meteor.SBPractice.Utils.NMSSupport;
import com.meteor.SBPractice.Utils.Utils;
import net.md_5.bungee.api.chat.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
        SBPPlayer player = SBPPlayer.getPlayer((Player) sender);
        if (player == null) return;
        if (!Admin.check(player)) {
            player.sendMessage(ChatColor.RED + "Please switch to admin mode first!");
            return;
        } Plot.SetupSession session = Plot.SetupSession.getSessionByPlayer(player.getPlayer());
        if (args.length == 1 && args[0].equals("spawnpoint")) {
            if (session == null) return;
            session.setSpawnPoint(Utils.simplifyLocation(player.getLocation()));
            player.playSound(Utils.Sounds.ORB_PICKUP);
            if (session.save()) return;
            player.sendMessage(ChatColor.GREEN + "Use the iron axe in the first slot of the hotbar to set the build area!");

            ItemStack is = new ItemStack(Material.IRON_AXE);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Select two points");
            is.setItemMeta(im);
            player.getPlayer().getInventory().setItem(0, NMSSupport.setTag(is, "sbp-setup", "setup-build-area"));
        } else {
            if (session != null) {
                player.sendMessage(Messages.CANNOT_DO_THAT.getMessage());
                return;
            } new Plot.SetupSession(player.getPlayer());
            TextComponent text = new TextComponent(ChatColor.YELLOW + "" + ChatColor.BOLD + "[Click Here]");
            text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sbp setup spawnpoint"));
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click Here").create()));
            BaseComponent[] bc = TextComponent.fromLegacyText(ChatColor.GREEN + "Stand at the plot spawn point, then ");
            bc[bc.length - 1].addExtra(text);
            player.getPlayer().spigot().sendMessage(bc);
            player.playSound(Utils.Sounds.ORB_PICKUP);
        }
    }
}
