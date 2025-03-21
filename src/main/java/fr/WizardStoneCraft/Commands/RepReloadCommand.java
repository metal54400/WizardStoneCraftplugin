package fr.WizardStoneCraft.Commands;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RepReloadCommand implements TabExecutor {
    private final WizardStoneCraft plugin;

    public RepReloadCommand(WizardStoneCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("reputation.reload")) {
            String noPermMessage = ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.noperm", "&cVous n'avez pas la permission d'utiliser cette commande."));
            sender.sendMessage(noPermMessage);
            return true;
        }

        // Recharger la configuration et les messages
        plugin.reloadConfig();
        plugin.loadMessages();
        plugin.loadMessagesConfig();

        // Lire le message depuis config.yml
        String reloadMessage = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("messages.reload_success", "&aConfiguration et messages rechargés avec succès!"));

        sender.sendMessage(reloadMessage);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return List.of();
    }
}



