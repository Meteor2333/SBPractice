package cc.meteormc.sbpractice.api.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public abstract class SubCommand {
    private final String name, permission;
    private final int minArgs;

    public abstract void execute(CommandSender sender, String[] args);

    public abstract void onNoPermission(@NotNull CommandSender sender);

    public abstract @Nullable String getCommandUsage(@NotNull CommandSender sender);
}
