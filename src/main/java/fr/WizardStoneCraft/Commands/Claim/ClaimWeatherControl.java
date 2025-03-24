package fr.WizardStoneCraft.Commands.Claim;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimWeatherControl implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seuls les joueurs peuvent exécuter cette commande.");
            return true;
        }

        Player player = (Player) sender;
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

        World world = player.getWorld();
        switch (args[0].toLowerCase()) {
            case "soleil":
                world.setStorm(false);
                world.setThundering(false);
                player.sendMessage(ChatColor.GREEN + "Météo du claim réglée sur Soleil.");
                break;
            case "pluie":
                world.setStorm(true);
                player.sendMessage(ChatColor.GREEN + "Météo du claim réglée sur Pluie.");
                break;
            case "naturel":
                player.sendMessage(ChatColor.GREEN + "Météo du claim rétablie sur celle du monde.");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Option invalide. Utilisez: soleil, pluie ou naturel.");
                return true;
        }
        return true;
    }

}
