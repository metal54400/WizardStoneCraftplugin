package fr.WizardStoneCraft.Commands.Claim;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ClaimWeatherControl implements CommandExecutor {

    // Map pour stocker la météo locale par claim (à initialiser dans ta classe principale)
    private static final Map<Claim, String> claimWeather = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Seuls les joueurs peuvent exécuter cette commande.");
            return true;
        }

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, null);
        if (claim == null) {
            player.sendMessage(ChatColor.RED + "Vous devez être dans un claim pour modifier la météo.");
            return true;
        }

        if (!claim.getOwnerID().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Seul le propriétaire du claim peut modifier la météo.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /claimmeteo <soleil|pluie|naturel>");
            return true;
        }

        String option = args[0].toLowerCase();

        switch (option) {
            case "soleil":
            case "pluie":
            case "naturel":
                claimWeather.put(claim, option);
                player.sendMessage(ChatColor.GREEN + "Météo locale du claim réglée sur " + option + ".");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Option invalide. Utilisez: soleil, pluie ou naturel.");
                return true;
        }
        return true;
    }

    public static String getClaimWeather(Claim claim) {
        return claimWeather.getOrDefault(claim, "naturel");
    }
}
