package fr.WizardStoneCraft.Commands;


import fr.WizardStoneCraft.WizardStoneCraft;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class claimoffmobspawn implements CommandExecutor {




        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "§7(§c!§7)§c Seuls les joueurs peuvent exécuter cette commande !");
                return true;
            }

            Player player = (Player) sender;
            Location loc = player.getLocation();
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);

            if (claim == null) {
                player.sendMessage(ChatColor.RED + "§7(§c!§7)§c Vous n'êtes pas dans un claim !");
                return true;
            }

            if (!claim.getOwnerID().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "§7(§c?§7)§c Vous n'êtes pas le propriétaire de ce claim !");
                return true;
            }

            int reputation = WizardStoneCraft.getInstance().getReputation(player);
            if (reputation < 100) {
                player.sendMessage(ChatColor.RED + "§7(§c?§7)§c Vous avez besoin de 100 points de réputation pour désactiver le spawn des mobs !");
                return true;
            }

            if (WizardStoneCraft.getInstance().disabledClaims.contains(claim.getID())) {
                WizardStoneCraft.getInstance().disabledClaims.remove(claim.getID());
                player.sendMessage(ChatColor.GREEN + "§7(§c!§7)§c Le spawn des mobs est maintenant activé dans ce claim.");
            } else {
                WizardStoneCraft.getInstance().disabledClaims.add(claim.getID());
                player.sendMessage(ChatColor.GREEN + "§7(§c!§7)§c Le spawn des mobs est maintenant désactivé dans ce claim.");
            }
            return true;
        }

}
