package com.meteor.SBPractice.Commands.SubCommands.Multiplayer;

import com.meteor.SBPractice.Api.SBPPlayer;
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
        SBPPlayer player = SBPPlayer.getPlayer((Player) sender);
        if (player == null) return;
        if (args.length == 0) {
            Bukkit.dispatchCommand(player.getPlayer(), "sbp help");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Messages.PLAYER_NOT_FOUND.getMessage());
            return;
        }

        if (target.getName().equalsIgnoreCase(player.getName())) {
            player.sendMessage(Messages.CANNOT_DO_THAT.getMessage());
            return;
        }

        if (Invite.invites.getOrDefault(target.getName(), new ArrayList<>()).contains(player.getName())) {
            List<String> players = Invite.invites.getOrDefault(target.getName(), new ArrayList<>());
            players.remove(player.getName());
            Invite.invites.put(target.getName(), players);
        } else if (Join.joins.getOrDefault(target.getName(), new ArrayList<>()).contains(player.getName())) {
            List<String> players = Join.joins.getOrDefault(target.getName(), new ArrayList<>());
            players.remove(player.getName());
            Join.joins.put(target.getName(), players);
        } else {
            player.sendMessage(Messages.REQUEST_NOT_FOUND.getMessage().replace("%player%", target.getName()));
            return;
        }

        Map<String, Long> players = denys.getOrDefault(player.getName(), new HashMap<>());
        players.put(target.getName(), System.currentTimeMillis());
        denys.put(player.getName(), players);

        player.sendMessage(Messages.RECEIVER_DENYED.getMessage().replace("%player%", target.getName()));
        target.sendMessage(Messages.VICTIM_DENYED.getMessage().replace("%player%", player.getName()));
    }
}
