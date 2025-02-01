package fr.WizardStoneCraft.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class RepHelpCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && !(sender instanceof CommandSender)) {
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "§7[§e?§7] Commandes de WizardStoneCraft:");
        sender.sendMessage(ChatColor.YELLOW + " §7[§e?§7] /repadd <player> <amount> - Ajouter des points de réputation à un joueur.");
        sender.sendMessage(ChatColor.YELLOW + " §7[§e?§7] /repsubtract <player> <amount> - Soustraire des points de réputation à un joueur.");
        sender.sendMessage(ChatColor.YELLOW + " §7[§e?§7] /reptop - Afficher le top 5 des réputations.");
        sender.sendMessage(ChatColor.YELLOW + " §7[§e?§7] /repinfo pour voir les diffrent state des reputation .");
        sender.sendMessage(ChatColor.YELLOW + " §7[§e?§7] /rep <player> - Afficher les informations de réputation d'un joueur.");
        sender.sendMessage(ChatColor.YELLOW + " §7[§e?§7] /rephighlight <player> - Mettre en évidence la réputation d'un joueur.");
        sender.sendMessage(ChatColor.YELLOW + " §7[§e?§7] /rephelp - Afficher cette liste de commandes.");
        //sender.sendMessage(ChatColor.YELLOW + " §7[§e?§7] /sethome - Premet de créer des homes");
        //sender.sendMessage(ChatColor.YELLOW + " §7[§e?§7] /home - Premet de se téléporté dans un home");
        sender.sendMessage(ChatColor.YELLOW + " §7[§e?§7] /tabreload - metre a jours le tab.");
        sender.sendMessage(ChatColor.YELLOW + " §7[§e?§7] /repmenu - pour modifier la reputation des joueur");
        sender.sendMessage(ChatColor.YELLOW + " §7[§e?§7] /repreload - mettre a jours les config.");
        sender.sendMessage(ChatColor.YELLOW + " §7[§e?§7] /profile - Pour voir les stats des autre joueur");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Arrays.asList("repadd","tabreload" ,"repreload","repsubtract", "rep","profile","repmenu","sethome","home","reptop", "repinfo", "rephighlight", "rephelp");
    }
}