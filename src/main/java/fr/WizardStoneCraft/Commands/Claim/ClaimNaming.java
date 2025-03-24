package fr.WizardStoneCraft.Commands.Claim;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class ClaimNaming implements CommandExecutor {
    private final HashMap<Long, String> claimNames = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Cette commande ne peut être exécutée que par un joueur.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Utilisation: /claim rename <nouveau_nom>");
            return true;
        }

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
        if (claim == null) {
            player.sendMessage(ChatColor.RED + "Vous devez être dans un claim pour renommer.");
            return true;
        }

        if (!claim.getOwnerID().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Vous devez être le propriétaire du claim pour le renommer.");
            return true;
        }

        String newName = String.join(" ", args).substring(args[0].length() + 1);
        claimNames.put(claim.getID(), newName);
        player.sendMessage(ChatColor.GREEN + "Le claim a été renommé en : " + ChatColor.YELLOW + newName);
        return true;
    }

    public String getClaimName(UUID claimID) {
        return claimNames.getOrDefault(claimID, "Sans Nom");
    }

}
