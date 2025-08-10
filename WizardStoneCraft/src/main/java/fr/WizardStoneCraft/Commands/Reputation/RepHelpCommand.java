package fr.WizardStoneCraft.Commands.Reputation;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RepHelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Vérification si l'expéditeur est un joueur ou console
        if (!(sender instanceof Player) && !(sender instanceof CommandSender)) {
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "§7[§e?§7] Commandes de WizardStoneCraft:");

        // Commandes liées à la réputation
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /repadd <player> <amount> - Ajouter ou retirer des points de réputation à un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /reptop - Afficher le top 5 des réputations.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /rep <player> - Afficher les informations de réputation d'un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /rephighlight <player> - Mettre en évidence la réputation d'un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /rephelp - Afficher cette liste de commandes.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /repreload - Mettre à jour la configuration de la réputation.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /repspawnnpc - Faire apparaître le purificateur (NPC).");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /repunmute <player> - Permet de démuter un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /repmute <player> - Permet de muter un joueur.");

        // Autres commandes diverses
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /calendrier - Affiche la saison actuelle et le jour en cours.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /claimgive <joueur> <montant> - Donne un claim.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /mute <joueur> - Mute un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /unmute <joueur> - Unmute un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /restartplugin - Redémarre WizardStoneCraft.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /menueggcustom - Ouvre un menu custom.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /t <message> - Envoie un message aux joueurs proches.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /gemsreconvert - Reconvertir vos gems en monnaie.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /dailyquestselection <clé_de_quête> - Réclame ta quête quotidienne.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /dailyquestfinish - Terminer la quête quotidienne.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /dailyquest - Voir ta quête quotidienne.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /gemshop - Ouvrir la boutique des gems.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /gems - Voir les gems d’un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /gemadd - Ajouter des gems à un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /gemstop - Voir le classement des gems.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /giveremede - Ajouter des points de réputation.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /giveceleste - Ajouter des points de réputation.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /repremove <player> <amount> - Retirer des points de réputation.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /givejukebox - Activer/désactiver l’élytres dans l’End.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /topluck - Voir le classement de la chance.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /elytra <activer|désactiver> - Activer/désactiver l’élytres.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /claimlist - Ouvre le menu des claims.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /claimrename - Renommer un claim.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /anticheatmenu - Ouvre le menu Anti-Cheat.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /cook - Commande custom.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /claimmeteo - Commande custom.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /tradeclaim - Commande custom.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /Claimoffspawnmob - Commande custom.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /affairenpc - Commande custom.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /passifset - Active le mode passif pour un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /passifunset - Désactive le mode passif pour un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /broadcast - Envoie un message global.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /repreload - Recharge la config réputation.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /rephelp - Affiche l’aide sur la réputation.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /rephighlight - Met en évidence la réputation d’un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /job <join|leave|info> - Gère les métiers des joueurs.");
        sender.sendMessage(ChatColor.YELLOW + "- §7[§e?§7] /Wizard-placeholders - Affiche tous les placeholders WizardStoneCraft.");

        return true;
    }
}
