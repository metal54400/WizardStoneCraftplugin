package fr.WizardStoneCraft.Commands.Gems;

import fr.WizardStoneCraft.Manager.GemManager;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class GemCommand implements CommandExecutor {

    private final GemManager gemManager;
    private final Economy economy;  // Assure-toi que tu as une API d'économie comme Vault

    public GemCommand(GemManager gemManager, Economy economy) {
        this.gemManager = gemManager;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("gems")) {
            if (args.length == 1) {
                String target = args[0];
                int gems = gemManager.getGems(target);
                sender.sendMessage("§a" + target + " a §e" + gems + " §agems.");
                return true;
            } else {
                sender.sendMessage("§cUsage: /gems <player>");
                return true;
            }
        }

        if (cmd.getName().equalsIgnoreCase("gemadd")) {
            if (args.length == 2) {
                String target = args[0];
                int amount;
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cLe montant doit être un nombre.");
                    return true;
                }

                gemManager.addGems(target, amount);
                sender.sendMessage("§aAjouté §e" + amount + " §agems à §b" + target);
                return true;
            } else {
                sender.sendMessage("§cUsage: /gemadd <player> <amount>");
                return true;
            }
        }

        if (cmd.getName().equalsIgnoreCase("gemstop")) {
            sender.sendMessage("§6Classement des Gems :");
            int rank = 1;
            for (Map.Entry<String, Integer> entry : gemManager.getTopGems().entrySet()) {
                sender.sendMessage("§e#" + rank + " §b" + entry.getKey() + " : §a" + entry.getValue() + " gems");
                rank++;
                if (rank > 10) break; // Limite à top 10
            }
            return true;
        }

        // Commande pour reconvertir des gems en argent
        if (cmd.getName().equalsIgnoreCase("gemsreconvert")) {
            if (args.length == 1) {
                Player player = (Player) sender;
                int gems = gemManager.getGems(player.getName());

                if (gems <= 0) {
                    player.sendMessage("§cVous n'avez pas de gems à convertir.");
                    return true;
                }

                // Taux de conversion (1 gem = 1 monnaie, tu peux ajuster)
                double conversionRate = 1.0; // 1 gem = 1 monnaie
                double amountToConvert = gems * conversionRate;

                // Ajouter de l'argent au joueur via l'API d'économie (comme Vault)
                double balanceBefore = economy.getBalance(player);
                economy.depositPlayer(player, amountToConvert);

                // Retirer les gems du joueur
                gemManager.setGems(player.getName(), 0);

                // Message de confirmation
                double newBalance = economy.getBalance(player);
                player.sendMessage("§aVous avez converti §e" + gems + " §agems en §a" + amountToConvert + " §emonnaie.");
                player.sendMessage("§6Votre solde actuel est de §a" + newBalance + " §emonnaie.");
                return true;
            } else {
                sender.sendMessage("§cUsage: /gemsreconvert");
                return true;
            }
        }

        return false;
    }
}



