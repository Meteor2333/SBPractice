package cc.meteormc.sbpractice.api.command;

import cc.meteormc.sbpractice.api.SBPracticeAPI;
import com.cryptomorin.xseries.XSound;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public abstract class MainCommand extends BukkitCommand {
    private final String permission;
    private final Set<SubCommand> subCommands = new LinkedHashSet<>();

    protected MainCommand(String name, String description, String permission, String... aliases) {
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
                if (!subCommand.getName().equalsIgnoreCase(args[0])) {
                    continue;
                }

                String perm = subCommand.getPermission();
                if (!perm.isEmpty() && !sender.hasPermission(perm)) {
                    subCommand.onNoPermission(sender);
                } else if (args.length > subCommand.getMinArgs()) {
                    subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
                } else {
                    XSound.ENTITY_VILLAGER_NO.play((Entity) sender);
                    String usage = subCommand.getCommandUsage(sender);
                    if (usage != null) sender.sendMessage(usage);
                }
                return true;
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
                String perm = subCommand.getPermission();
                if (perm.isEmpty() || sender.hasPermission(perm)) tab.add(subCommand.getName());
            }
            tab.removeIf(filter -> !filter.toLowerCase().startsWith(args[0].toLowerCase()));
        } else tab.addAll(Bukkit.getOnlinePlayers().stream()
                .filter(player -> sender instanceof Player && player.getWorld().equals(((Player) sender).getWorld()))
                .map(HumanEntity::getName)
                .collect(Collectors.toList()));
        return tab;
    }

    public void sendCommandHelp(@NotNull CommandSender sender) {
        PluginDescriptionFile description = SBPracticeAPI.getInstance().getPlugin().getDescription();
        sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD
                + "þ " + ChatColor.GOLD + description.getName() + " "
                + ChatColor.GRAY + "v" + description + " by "
                + ChatColor.RED + description.getAuthors().get(0));
        sender.sendMessage("");
        sender.sendMessage(this.getCommandHelp(sender).toArray(new String[0]));
    }

    public abstract void onNoPermission(@NotNull CommandSender sender);

    public abstract List<String> getCommandHelp(@NotNull CommandSender sender);
}
