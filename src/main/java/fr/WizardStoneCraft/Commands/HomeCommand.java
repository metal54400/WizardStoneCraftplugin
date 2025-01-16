package fr.WizardStoneCraft.Commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class HomeCommand implements CommandExecutor {
    private final Map<UUID, Location> playerHomes;

    public HomeCommand(Map<UUID, Location> playerHomes) {
        this.playerHomes = playerHomes;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§7[§e?§7] Seuls les joueurs peuvent exécuter cette commande.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (!playerHomes.containsKey(playerId)) {
            player.sendMessage("§7[§e?§7] §cVous n'avez pas encore défini de home. Utilisez §a/sethome §cpour définir votre position.");
            return true;
        }

        Location homeLocation = playerHomes.get(playerId);
        player.teleport(homeLocation);
        player.sendMessage("§7[§e?§7] Vous avez été §atéléporté §7à votre home.");
        return true;
    }
}