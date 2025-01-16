package fr.WizardStoneCraft.Commands;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RepReloadCommand implements TabExecutor {
    private String reloadMessages;
    private final WizardStoneCraft plugin;

    public RepReloadCommand(WizardStoneCraft plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("reputation.reload")) {
            sender.sendMessage(reloadMessages);
            String reloadMessages = ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.noperm", "Vous n'avez pas la permission d'utiliser cette commande"));
            return true;
        }

        // Recharger la configuration et les messages
        plugin.reloadConfig();
        plugin.loadMessages();

        // Lire le message depuis config.yml
        String reloadMessage = ChatColor.translateAlternateColorCodes('&',
               plugin.getConfig().getString("messages.reload_success", "&aConfiguration et messages rechargés avec succès!"));

        sender.sendMessage(reloadMessage); // Envoyer le message au joueur
        return true;

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return List.of();
    }
}


