package fr.WizardStoneCraft.Commands.Anticheat;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Topluck implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seuls les joueurs peuvent exécuter cette commande.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("anticheat.topluck")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        if (WizardStoneCraft.getInstance().topluckConfig.getConfigurationSection("players") == null) {
            player.sendMessage("§cAucun joueur n'a encore miné de minerai.");
            return true;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Top Luck");

        for (String playerName : WizardStoneCraft.getInstance().topluckConfig.getConfigurationSection("players").getKeys(false)) {
            int totalMined = WizardStoneCraft.getInstance().topluckConfig.getConfigurationSection("players." + playerName).getValues(false)
                    .values().stream().mapToInt(value -> (int) value).sum();
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = skull.getItemMeta();
            meta.setDisplayName("§6" + playerName + " : " + totalMined + " minerais");
            skull.setItemMeta(meta);
            inv.addItem(skull);
        }

        player.openInventory(inv);
        return true;
    }
}
