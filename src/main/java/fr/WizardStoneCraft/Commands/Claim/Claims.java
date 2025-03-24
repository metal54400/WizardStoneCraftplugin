package fr.WizardStoneCraft.Commands.Claim;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.ryanhamshire.GriefPrevention.Claim;

import java.util.List;

public class Claims implements CommandExecutor {
    @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Récupère tous les claims du joueur
            List<Claim> claims = (List<Claim>) GriefPrevention.instance.dataStore.getClaims();

            if (claims.isEmpty()) {
                player.sendMessage("Vous n'avez aucun claim.");
            } else {
                // Afficher tous les claims avec leur nom
                player.sendMessage("Liste de vos claims :");
                for (Claim claim : claims) {
                    // Afficher les informations du claim, y compris le nom
                    String claimName = String.valueOf(claim.getID());  // Supposons que tu as stocké le nom du claim sous cette clé
                    player.sendMessage("Claim : " + claimName + " - " + claim.getChunks());
                }
            }
        }
        return true;
    }


}
