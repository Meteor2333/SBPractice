package cc.meteormc.sbpractice.command;

import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.config.Message;
import cc.meteormc.sbpractice.feature.operation.*;
import cc.meteormc.sbpractice.feature.session.SetupSession;
import cc.meteormc.sbpractice.gui.SettingsGui;
import com.cryptomorin.xseries.XSound;
import com.google.common.collect.ImmutableList;
import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.annotations.Command;
import me.despical.commandframework.annotations.Completer;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class MainCommand {
    private static final ImmutableList<String> SUB_COMMANDS;

    static {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (Method method : MainCommand.class.getMethods()) {
            if (!method.isAnnotationPresent(Command.class)) continue;
            builder.add(method.getName());
        }
        SUB_COMMANDS = builder.build();
    }

    @Completer(
            name = "sbpractice",
            aliases = "sbp"
    )
    public List<String> onTabComplete() {
        return SUB_COMMANDS;
    }

    @Command(
            name = "sbpractice",
            fallbackPrefix = "sbpractice",
            aliases = {"sbpractice.help", "sbp", "sbp.help"}
    )
    public void help(CommandArguments arguments) {
        Player sender = arguments.getSender();
        Message.COMMAND.HELP.MAIN.parse(sender).forEach(sender::sendMessage);
    }

    @Command(
            name = "sbpractice.clear",
            fallbackPrefix = "sbpractice",
            aliases = "sbp.clear",
            desc = "Clear current",
            usage = "/sbp clear",
            senderType = Command.SenderType.PLAYER
    )
    public void clear(CommandArguments arguments) {
        Player sender = arguments.getSender();
        PlayerData.getData(sender).ifPresent(data -> {
            data.getIsland().executeOperation(new ClearOperation());
        });
    }

    @Command(
            name = "sbpractice.ground",
            fallbackPrefix = "sbpractice",
            aliases = "sbp.ground",
            desc = "Sync ground",
            usage = "/sbp ground",
            senderType = Command.SenderType.PLAYER
    )
    public void ground(CommandArguments arguments) {
        Player sender = arguments.getSender();
        PlayerData.getData(sender).ifPresent(data -> {
            data.getIsland().executeOperation(new GroundOperation());
        });
    }

    @Command(
            name = "sbpractice.preview",
            fallbackPrefix = "sbpractice",
            aliases = "sbp.preview",
            desc = "Preview recorded",
            usage = "/sbp preview",
            senderType = Command.SenderType.PLAYER
    )
    public void preview(CommandArguments arguments) {
        Player sender = arguments.getSender();
        PlayerData.getData(sender).ifPresent(data -> {
            data.getIsland().executeOperation(new PreviewOperation());
        });
    }

    @Command(
            name = "sbpractice.record",
            fallbackPrefix = "sbpractice",
            aliases = "sbp.record",
            desc = "Record current",
            usage = "/sbp record",
            senderType = Command.SenderType.PLAYER
    )
    public void record(CommandArguments arguments) {
        Player sender = arguments.getSender();
        PlayerData.getData(sender).ifPresent(data -> {
            data.getIsland().executeOperation(new RecordOperation());
            XSound.ENTITY_EXPERIENCE_ORB_PICKUP.play(sender);
        });
    }

    @Command(
            name = "sbpractice.start",
            fallbackPrefix = "sbpractice",
            aliases = "sbp.start",
            desc = "Start countdown",
            usage = "/sbp start",
            senderType = Command.SenderType.PLAYER
    )
    public void start(CommandArguments arguments) {
        Player sender = arguments.getSender();
        PlayerData.getData(sender).ifPresent(data -> {
            data.getIsland().executeOperation(new StartOperation());
        });
    }

    @Command(
            name = "sbpractice.settings",
            fallbackPrefix = "sbpractice",
            aliases = "sbp.settings",
            desc = "Open settings gui",
            usage = "/sbp settings",
            senderType = Command.SenderType.PLAYER
    )
    public void settings(CommandArguments arguments) {
        Player sender = arguments.getSender();
        new SettingsGui(sender).open(sender);
    }

    @Command(
            name = "sbpractice.setup",
            fallbackPrefix = "sbpractice",
            aliases = "sbp.setup",
            desc = "Setup zone",
            usage = "/sbp setup <zoneName>",
            min = 1,
            onlyOp = true,
            senderType = Command.SenderType.PLAYER
    )
    public void setup(CommandArguments arguments) {
        Player sender = arguments.getSender();
        String name = arguments.getArgument(0);
        SetupSession.getSession(name).map(Optional::of).orElseGet(() -> {
            try {
                return Optional.of(new SetupSession(sender, name));
            } catch (IllegalArgumentException e) {
                sender.sendMessage(e.getMessage());
                return Optional.empty();
            }
        }).ifPresent(ss -> {
            ss.handleCommand(arguments.concatRangeOf(1, arguments.getLength()));
        });
    }
}
