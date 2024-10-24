package com.meteor.SBPractice.Commands.SubCommands.Multiplayer;

import com.meteor.SBPractice.Commands.MultiplayerCommand;
import com.meteor.SBPractice.Commands.SubCommand;
import com.meteor.SBPractice.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Deny extends SubCommand {
    // 执行拒绝操作的玩家, 被此玩家拒绝的玩家列表和currentTimeMillis
    public static Map<String, Map<String, Long>> denys = new HashMap<>();

    public Deny(MultiplayerCommand parent, String name) {
        super(parent, name, false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            Bukkit.dispatchCommand(player, "sbp help");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Messages.getMessage("player-not-found"));
            return;
        }

        if (target.getName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(Messages.getMessage("cannot-do-that"));
            return;
        }

        if (Invite.invites.getOrDefault(target.getName(), new ArrayList<>()).contains(player.getName())) {
            List<String> players = Invite.invites.getOrDefault(player.getName(), new ArrayList<>());
            players.remove(target.getName());
            Invite.invites.put(player.getName(), players);
        } else if (Join.joins.getOrDefault(target.getName(), new ArrayList<>()).contains(target.getName())) {
            List<String> players = Join.joins.getOrDefault(player.getName(), new ArrayList<>());
            players.remove(target.getName());
            Join.joins.put(player.getName(), players);
        } else {
            player.sendMessage(Messages.getMessage("requeste-not-found").replace("%player%", target.getName()));
            return;
        }

        Map<String, Long> players = denys.get(player.getName());
        players.put(target.getName(), System.currentTimeMillis());
        denys.put(player.getName(), players);

        player.sendMessage(Messages.getMessage("receiver-denyed").replace("%player%", target.getName()));
        target.sendMessage(Messages.getMessage("victim-denyed").replace("%player%", player.getName()));
    }
}
