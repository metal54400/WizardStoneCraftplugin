package fr.WizardStoneCraft.Commands.Anticheat;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AntiCheatMenuCommand implements CommandExecutor {

    // Si tu as besoin d'accéder au plugin ou à des méthodes, ajoute un constructeur
    // Ici je suppose que openMenus est une méthode que tu gères ailleurs, par exemple dans le plugin

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
           openMenus(player);
        } else {
            sender.sendMessage(ChatColor.RED + "Seuls les joueurs peuvent utiliser cette commande !");
        }
        return true;
    }
    public void openMenus(Player player) {
        Inventory menu = Bukkit.createInventory(null, 9, ChatColor.DARK_RED + "⚠ Menu Anti-Cheat ⚠");

        // Item pour activer/désactiver les alertes
        ItemStack toggleItem = new ItemStack(WizardStoneCraft.getInstance().alertEnabledPlayers.contains(player) ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta meta = toggleItem.getItemMeta();
        meta.setDisplayName(WizardStoneCraft.getInstance().alertEnabledPlayers.contains(player) ? ChatColor.GREEN + "✅ Alertes activées" : ChatColor.RED + "❌ Alertes désactivées");
        toggleItem.setItemMeta(meta);

        menu.setItem(4, toggleItem); // Met l'item au centre du menu
        player.openInventory(menu);
    }

}
