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

public class SetHomeCommand implements CommandExecutor {
    private final Map<UUID, Location> playerHomes = new HashMap<>(); // Map pour stocker les homes
    private final Map<UUID, Integer> reputation; // Réputation des joueurs
    private static final int MIN_REPUTATION = -50; // Seuil minimum de réputation pour utiliser la commande

    public SetHomeCommand(Map<UUID, Integer> reputation) {
        this.reputation = reputation;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent exécuter cette commande.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();
        int playerReputation = reputation.getOrDefault(playerId, 0);

        // Vérifie la réputation du joueur
        if (playerReputation <= MIN_REPUTATION) {
            if (playerReputation <= -100) {
                player.sendMessage("§7[§e?§7] Votre réputation est §4horrible§7. Vous n'avez pas accès à cette commande.");
            } else {
                player.sendMessage("§7[§e?§7] Votre réputation est §6mauvaise§7. Vous n'avez pas accès à cette commande.");
            }
            return true;
        }

        // Définit le home pour le joueur
        Location homeLocation = player.getLocation();
        playerHomes.put(playerId, homeLocation);
        player.sendMessage("§7[§e?§7] Votre home a été défini à votre position actuelle : §e" +
                homeLocation.getBlockX() + ", " +
                homeLocation.getBlockY() + ", " +
                homeLocation.getBlockZ() + ".");
        return true;
    }

    public Location getHome(UUID playerId) {
        return playerHomes.get(playerId); // Permet de récupérer la position du home d'un joueur
    }
}
