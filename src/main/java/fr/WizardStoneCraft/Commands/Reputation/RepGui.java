package fr.WizardStoneCraft.Commands.Reputation;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;


public class RepGui implements CommandExecutor, Listener {


    @Override
    public boolean onCommand(CommandSender sender, Command cmd,  String s, String[] agrs) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            openMenu(player);
            return true;
        }
        return false;
    }




    public void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 54, "§bListe De joueur");
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            menu.addItem(createPlayerHead(offlinePlayer));
        }
        player.openInventory(menu);
    }

    private ItemStack createPlayerHead(OfflinePlayer player) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName(player.getName());
            meta.setLore(Arrays.asList("§7[§e?§7]§7 check player est en ligne:"+ player.isOnline() ,"§7[§e?§7]§7 Réputation: " + WizardStoneCraft.reputation.getOrDefault(player.getUniqueId(), 0))); // Ajoute une lore
            skull.setItemMeta(meta);

        }
        return skull;
    }
}
