package simpleRep;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class ReputationMenu implements Listener {

    private final ReputationPlugin plugin;

    public ReputationMenu(ReputationPlugin plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player, UUID targetId) {
        // Crée un inventaire de 9 slots
        Inventory menu = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "Changer la Réputation");

        // Item pour augmenter la réputation
        ItemStack increaseItem = new ItemStack(Material.EMERALD);
        ItemMeta increaseMeta = increaseItem.getItemMeta();
        increaseMeta.setDisplayName(ChatColor.GREEN + "Augmenter la réputation");
        increaseItem.setItemMeta(increaseMeta);

        // Item pour diminuer la réputation
        ItemStack decreaseItem = new ItemStack(Material.REDSTONE);
        ItemMeta decreaseMeta = decreaseItem.getItemMeta();
        decreaseMeta.setDisplayName(ChatColor.RED + "Diminuer la réputation");
        decreaseItem.setItemMeta(decreaseMeta);

        // Item pour afficher la réputation actuelle
        int currentRep = plugin.reputation.getOrDefault(targetId, plugin.loadPlayerReputation(targetId));
        ItemStack currentRepItem = new ItemStack(Material.PAPER);
        ItemMeta currentRepMeta = currentRepItem.getItemMeta();
        currentRepMeta.setDisplayName(ChatColor.YELLOW + "Réputation actuelle: " + currentRep);
        currentRepItem.setItemMeta(currentRepMeta);

        // Ajoute les items dans le menu
        menu.setItem(3, increaseItem);
        menu.setItem(4, currentRepItem);
        menu.setItem(5, decreaseItem);

        // Ouvre le menu pour le joueur
        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Vérifie si le menu est le bon
        if (event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Changer la Réputation")) {
            event.setCancelled(true); // Empêche la prise des items

            Player player = (Player) event.getWhoClicked();
            UUID targetId = player.getUniqueId(); // Ici, vous devrez probablement passer le bon UUID

            // Vérifie quel item a été cliqué
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            String itemName = clickedItem.getItemMeta().getDisplayName();

            if (itemName.equals(ChatColor.GREEN + "Augmenter la réputation")) {
                // Augmente la réputation
                int newRep = plugin.reputation.getOrDefault(targetId, plugin.loadPlayerReputation(targetId)) + 10;
                plugin.reputation.put(targetId, Math.min(newRep, plugin.MAX_REP));
                plugin.savePlayerReputation(targetId, newRep);
                player.sendMessage(ChatColor.GREEN + "Réputation augmentée de 10 points !");
            } else if (itemName.equals(ChatColor.RED + "Diminuer la réputation")) {
                // Diminue la réputation
                int newRep = plugin.reputation.getOrDefault(targetId, plugin.loadPlayerReputation(targetId)) - 10;
                plugin.reputation.put(targetId, Math.max(newRep, plugin.MIN_REP));
                plugin.savePlayerReputation(targetId, newRep);
                player.sendMessage(ChatColor.RED + "Réputation diminuée de 10 points !");
            }

            // Met à jour le menu
            openMenu(player, targetId);
        }
    }
}
