package fr.WizardStoneCraft.Commands.Claim;

import fr.WizardStoneCraft.WizardStoneCraft;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TradeClaimCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "§7[§e?§7] Seuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = WizardStoneCraft.getInstance().griefPrevention.dataStore.getPlayerData(player.getUniqueId());
        int claimBlocks = playerData.getAccruedClaimBlocks();
        int tradeAmount = 1000; // Nombre de blocks échangés

        if (claimBlocks < tradeAmount) {
            player.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous n'avez pas assez de blocks de claim pour cet échange.");
            return true;
        }

        // Retirer les blocks de claim et donner des émeraudes
        playerData.setAccruedClaimBlocks(claimBlocks - tradeAmount);
        player.getInventory().addItem(new ItemStack(Material.EMERALD, 10)); // 10 émeraudes en échange

        player.sendMessage(ChatColor.GREEN + "§7[§e?§7]§a Vous avez échangé " + tradeAmount + " blocks de claim contre 10 émeraudes !");
        return true;
    }
}
