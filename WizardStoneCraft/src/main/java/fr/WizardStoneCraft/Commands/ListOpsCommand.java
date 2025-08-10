package fr.WizardStoneCraft.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class ListOpsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wizardstonecraft.listop")) {
            sender.sendMessage("§cVous n'avez pas la permission d'exécuter cette commande.");
            return true;
        }
        // Récupérer tous les ops (online ou offline)
        Set<org.bukkit.OfflinePlayer> ops = Bukkit.getServer().getOperators();

        if (ops.isEmpty()) {
            sender.sendMessage("§cAucun opérateur trouvé sur ce serveur.");
            return true;
        }

        StringBuilder opsList = new StringBuilder("§aListe des opérateurs : ");

        for (org.bukkit.OfflinePlayer op : ops) {
            // Indiquer si l'op est en ligne ou non
            if (op.isOnline()) {
                opsList.append("§2").append(op.getName()).append("§a (en ligne), ");
            } else {
                opsList.append("§7").append(op.getName()).append("§c (hors ligne), ");
            }
        }

        // Supprimer la dernière virgule et espace
        if (opsList.length() > 0) {
            opsList.setLength(opsList.length() - 2);
        }

        sender.sendMessage(opsList.toString());
        return true;
    }
}
