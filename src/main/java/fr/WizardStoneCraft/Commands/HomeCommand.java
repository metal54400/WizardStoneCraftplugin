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

    private final Map<UUID, Location> homeLocations = new HashMap<>(); // Stocke les homes
    private final Map<UUID, Integer> reputation; // Map des réputations des joueurs
    private static final int MIN_REPUTATION = -10; // Limite de réputation pour accéder à /home

    public HomeCommand(Map<UUID, Integer> reputation) {
        this.reputation = reputation; // Injecte la map des réputations
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§7[§e?§7] Seul un joueur peut exécuter cette commande.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        // Vérification de la réputation
        int playerReputation = reputation.getOrDefault(playerId, 0); // Récupère la réputation ou 0 si absent
        if (playerReputation <= MIN_REPUTATION) {
            if (playerReputation <= -100) {
                player.sendMessage("§7[§e?§7] Votre réputation est §4horrible§7. Vous n'avez pas accès à cette commande.");
            } else {
                player.sendMessage("§7[§e?§7] Votre réputation est §6mauvaise§7. Vous n'avez pas accès à cette commande.");
            }
            return true;
        }

        // Téléportation au home
        if (homeLocations.containsKey(playerId)) {
            Location home = homeLocations.get(playerId);
            player.teleport(home);
            player.sendMessage("§7[§e?§7] Vous avez été téléporté à votre point de home.");
        } else {
            player.sendMessage("§7[§e?§7] Vous n'avez pas encore défini de point de home. Utilisez §e/home set §cpour définir votre home.");
        }

        return true;
    }
}
