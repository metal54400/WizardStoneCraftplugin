package fr.WizardStoneCraft.Commands;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.inventory.Merchant;


public class npcaffaire implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        WanderingTrader trader = (WanderingTrader) player.getWorld().spawn(player.getLocation(), WanderingTrader.class);
        trader.setCustomName(ChatColor.GOLD + "Affaire du jour");
        trader.setCustomNameVisible(true);
        trader.setAI(false);

        Merchant merchant = Bukkit.createMerchant(ChatColor.GOLD + "Affaire du jour");
        merchant.setRecipes(WizardStoneCraft.getInstance().dailyDeals);
        trader.setRecipes(WizardStoneCraft.getInstance().dailyDeals);

        player.sendMessage(ChatColor.GREEN + "Un marchand ambulant est apparu avec de nouvelles affaires !");
        return true;
    }

}
