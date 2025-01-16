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

        sender.sendMessage(ChatColor.GOLD + "Commandes de ReputationPlugin:");
        sender.sendMessage(ChatColor.YELLOW + "/repadd <player> <amount> - Ajouter des points de réputation à un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "/repsubtract <player> <amount> - Soustraire des points de réputation à un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "/reptop - Afficher le top 5 des réputations.");
        sender.sendMessage(ChatColor.YELLOW + "/repinfo pour voir les diffrent state des reputation .");
        sender.sendMessage(ChatColor.YELLOW + "/rep <player> - Afficher les informations de réputation d'un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "/rephighlight <player> - Mettre en évidence la réputation d'un joueur.");
        sender.sendMessage(ChatColor.YELLOW + "/rephelp - Afficher cette liste de commandes.");
        sender.sendMessage(ChatColor.YELLOW + "/tabreload - metre a jours le tab.");
        sender.sendMessage(ChatColor.YELLOW + "/repreload - mettre a jours les config.");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Arrays.asList("repadd","tabreload" ,"repreload","repsubtract", "rep","reptop", "repinfo", "rephighlight", "rephelp");
    }
}