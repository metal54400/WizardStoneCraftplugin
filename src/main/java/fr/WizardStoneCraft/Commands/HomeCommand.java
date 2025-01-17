package fr.WizardStoneCraft.Commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeCommand implements CommandExecutor {

    private final Map<UUID, Map<String, Location>> homeLocations = new HashMap<>();
    private final Map<UUID, Integer> reputation;
    private static final int MIN_REPUTATION = -10;

    public HomeCommand(Map<UUID, Integer> reputation) {
        this.reputation = reputation; // Injecte la map des réputations
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeul un joueur peut exécuter cette commande.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        // Vérification de la réputation
        int playerReputation = reputation.getOrDefault(playerId, 0);
        if (playerReputation <= MIN_REPUTATION) {
            if (playerReputation <= -100) {
                player.sendMessage("§7[§e?§7] Votre réputation est §4horrible§7. Vous n'avez pas accès à cette commande.");
            } else {
                player.sendMessage("§7[§e?§7] Votre réputation est §6mauvaise§7. Vous n'avez pas accès à cette commande.");
            }
            return true;
        }

        if (args.length == 0) {
            // Si aucune sous-commande n'est donnée, afficher une aide
            player.sendMessage("§eUtilisation :");
            player.sendMessage("§e/sethome  <nom> §7- Définit un home nommé.");
            player.sendMessage("§e/home <nom> §7- Téléporte au home nommé.");
            player.sendMessage("§e/homes §7- Liste tous vos homes.");
            player.sendMessage("§e/delhome  <nom> §7- Supprime un home nommé.");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "sethome":
                if (args.length < 2) {
                    player.sendMessage("§cUtilisation : /sethome  <nom>");
                    return true;
                }
                String homeName = args[1].toLowerCase();

                // Ajoute le home à la map
                homeLocations.putIfAbsent(playerId, new HashMap<>());
                homeLocations.get(playerId).put(homeName, player.getLocation());
                player.sendMessage("§aHome '" + homeName + "' défini avec succès !");
                return true;

            case "delhome":
                if (args.length < 2) {
                    player.sendMessage("§cUtilisation : /delhome  <nom>");
                    return true;
                }
                homeName = args[1].toLowerCase();

                // Supprime le home
                if (homeLocations.containsKey(playerId) && homeLocations.get(playerId).remove(homeName) != null) {
                    player.sendMessage("§aHome '" + homeName + "' supprimé avec succès !");
                } else {
                    player.sendMessage("§cHome '" + homeName + "' introuvable.");
                }
                return true;

            case "homes":
                if (homeLocations.containsKey(playerId) && !homeLocations.get(playerId).isEmpty()) {
                    player.sendMessage("§eVos homes :");
                    homeLocations.get(playerId).keySet().forEach(name -> player.sendMessage("§7- §a" + name));
                } else {
                    player.sendMessage("§cVous n'avez aucun home.");
                }
                return true;

            default:
                // Téléportation au home spécifié
                homeName = args[0].toLowerCase();
                if (homeLocations.containsKey(playerId) && homeLocations.get(playerId).containsKey(homeName)) {
                    Location home = homeLocations.get(playerId).get(homeName);
                    player.teleport(home);
                    player.sendMessage("§aTéléporté au home '" + homeName + "'.");
                } else {
                    player.sendMessage("§cHome '" + homeName + "' introuvable.");
                }
                return true;
        }
    }
}
