package fr.WizardStoneCraft.Commands.Reputation;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RepHelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && !(sender instanceof CommandSender)) {
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "§7[§e?§7] Commandes de WizardStoneCraft:");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /repadd <player> <amount> - Ajouter et retiré des points de réputation à un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /reptop - Afficher le top 5 des réputations.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /rep <player> - Afficher les informations de réputation d'un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /rephighlight <player> - Mettre en évidence la réputation d'un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /rephelp - Afficher cette liste de commandes.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /tabreload - metre a jours le tab.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /repmenu - pour modifier la reputation des joueur");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /repreload - mettre a jours les config.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /repspawnnpc - Pour faire spawn le purificateur");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /jobsstatue - Pour voir les stats des autre joueur");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /repunmute - permet de unmute un joueur");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /repmute - permet de mute un joueur");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /passifset - permetr de mettre le passif a un joueur");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /passifunset - permet d'enlever le passif a un joueur");

        return true;
    }


}