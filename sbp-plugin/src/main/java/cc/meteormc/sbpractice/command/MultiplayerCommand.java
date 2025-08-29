package cc.meteormc.sbpractice.command;

import cc.meteormc.sbpractice.api.Island;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.config.Message;
import cc.meteormc.sbpractice.feature.session.MultiplayerSession;
import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.annotations.Command;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class MultiplayerCommand {
    @Command(
            name = "multiplayer",
            fallbackPrefix = "sbpractice",
            aliases = {"multiplayer.help", "mp", "mp.help"}
    )
    public void help(CommandArguments arguments) {
        Player sender = arguments.getSender();
        Message.COMMAND.HELP.MULTIPLAYER.parse(sender).forEach(sender::sendMessage);
    }

    @Command(
            name = "multiplayer.accept",
            fallbackPrefix = "sbpractice",
            aliases = "mp.accept",
            desc = "Accept an invitation from a player",
            usage = "/mp accept <player>",
            min = 1,
            senderType = Command.SenderType.PLAYER
    )
    public void accept(CommandArguments arguments) {
        this.executeAction(arguments, MultiplayerSession::acceptPlayer);
    }

    @Command(
            name = "multiplayer.deny",
            fallbackPrefix = "sbpractice",
            aliases = "mp.deny <player>",
            desc = "Deny an invitation from a player",
            usage = "/mp deny",
            min = 1,
            senderType = Command.SenderType.PLAYER
    )
    public void deny(CommandArguments arguments) {
        this.executeAction(arguments, MultiplayerSession::denyPlayer);
    }

    @Command(
            name = "multiplayer.invite",
            fallbackPrefix = "sbpractice",
            aliases = "mp.invite",
            desc = "Invite another player to your island",
            usage = "/mp invite <player>",
            min = 1,
            senderType = Command.SenderType.PLAYER
    )
    public void invite(CommandArguments arguments) {
        this.executeAction(arguments, MultiplayerSession::invitePlayer);
    }

    @Command(
            name = "multiplayer.join",
            fallbackPrefix = "sbpractice",
            aliases = "mp.join",
            desc = "Join another player's island",
            usage = "/mp join <player>",
            min = 1,
            senderType = Command.SenderType.PLAYER
    )
    public void join(CommandArguments arguments) {
        this.executeAction(arguments, MultiplayerSession::joinPlayer);
    }

    @Command(
            name = "multiplayer.kick",
            fallbackPrefix = "sbpractice",
            aliases = "mp.kick",
            desc = "Remove a player from your island",
            usage = "/mp kick <player>",
            min = 1,
            senderType = Command.SenderType.PLAYER
    )
    public void kick(CommandArguments arguments) {
        this.executeAction(arguments, MultiplayerSession::kickPlayer);
    }

    @Command(
            name = "multiplayer.leave",
            fallbackPrefix = "sbpractice",
            aliases = "mp.leave",
            desc = "Leave your current island",
            usage = "/mp leave",
            senderType = Command.SenderType.PLAYER
    )
    public void leave(CommandArguments arguments) {
        Player sender = arguments.getSender();
        PlayerData.getData(sender).ifPresent(data -> {
            Island island = data.getIsland();
            if (island.getOwner().equals(sender)) {
                Message.BASIC.CANNOT_DO_THAT.sendTo(sender);
            } else {
                island.removeAny(sender, true);
                Message.MULTIPLAYER.LEAVE.ACTIVE.sendTo(sender, island.getOwner().getName());
                Message.MULTIPLAYER.LEAVE.PASSIVE.sendTo(island.getOwner(), sender.getName());
            }
        });
    }

    private void executeAction(CommandArguments arguments, BiConsumer<MultiplayerSession, Player> action) {
        Player sender = arguments.getSender();
        Player target = Bukkit.getPlayerExact(arguments.getArgument(0));
        if (target == null) {
            Message.BASIC.PLAYER_NOT_FOUND.sendTo(sender);
            return;
        }

        if (target.getName().equalsIgnoreCase(sender.getName())) {
            Message.BASIC.CANNOT_DO_THAT.sendTo(sender);
            return;
        }

        action.accept(MultiplayerSession.getOrCreateSession(sender), target);
    }
}
