package cc.meteormc.sbpractice.api.command;

import cc.meteormc.sbpractice.api.util.Utils;
import com.cryptomorin.xseries.XSound;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public abstract class MainCommand extends BukkitCommand {
    private final String permission;
    private final Set<SubCommand> subCommands = new LinkedHashSet<>();

    public MainCommand(String name, String description, String permission, String... aliases) {
        super(name, description, "/" + name, Arrays.asList(aliases));
        this.permission = permission;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        if (!this.permission.isEmpty() && !sender.hasPermission(this.permission)) {
            this.onNoPermission(sender);
            return true;
        }

        if (args.length != 0) {
            for (SubCommand subCommand : getSubCommands()) {
                if (subCommand.getName().equalsIgnoreCase(args[0])) {
                    String permission = subCommand.getPermission();
                    if (!permission.isEmpty() && !sender.hasPermission(permission)) {
                        subCommand.onNoPermission(sender);
                    } else {
                        if (args.length > subCommand.getMinArgs()) {
                            subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
                        } else {
                            XSound.ENTITY_VILLAGER_NO.play((Entity) sender);
                            Optional.ofNullable(subCommand.getCommandUsage(sender)).ifPresent(usage -> {
                                sender.sendMessage(Utils.colorize(usage));
                            });
                        }
                    }
                    return true;
                }
            }
        }
        this.sendCommandHelp(sender);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, String[] args) {
        List<String> tab = new ArrayList<>();
        if (!this.permission.isEmpty() && !sender.hasPermission(this.permission)) return tab;
        if (args.length == 1) {
            for (SubCommand subCommand : getSubCommands()) {
                String permission = subCommand.getPermission();
                if (permission.isEmpty() || sender.hasPermission(permission)) tab.add(subCommand.getName());
            }
            tab.removeIf(filter -> !filter.toLowerCase().startsWith(args[0].toLowerCase()));
        } else tab.addAll(Bukkit.getOnlinePlayers().stream()
                .filter(player -> sender instanceof Player && player.getWorld().equals(((Player) sender).getWorld()))
                .map(HumanEntity::getName)
                .collect(Collectors.toList()));
        return tab;
    }

    public abstract void onNoPermission(@NotNull CommandSender sender);

    public abstract void sendCommandHelp(@NotNull CommandSender sender);
}
