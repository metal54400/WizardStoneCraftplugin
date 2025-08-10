package fr.WizardStoneCraft.Listener.Vilager;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.MerchantRecipe;

import java.util.UUID;

public class onVillagerTrade implements Listener {

    @EventHandler
    public void onVillagerTrade(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // Vérifie si l'entité est un villageois
        if (holder instanceof Villager) {
            Player player = (Player) event.getPlayer();
            long time = player.getWorld().getTime(); // Temps du monde (0 = 06:00, 6000 = 12:00)

            // Plage horaire autorisée (8h à 16h in-game, soit 2000 à 12000 ticks)
            if (time < 2000 || time > 12000) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "?" + ChatColor.GRAY + "] "
                        + ChatColor.RED + "Ce n'est pas l'heure d'échanger avec les villageois §7[§c!§7] "
                        + ChatColor.GRAY + "Les heures d'échange sont de §d8h §7à §b16h.");
            }
        }
    }

    @EventHandler
    public void onTrade(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof Villager villager)) return;

        MerchantRecipe trade = villager.getRecipe(event.getSlot());
        Material result = trade.getResult().getType();
        UUID playerUUID = player.getUniqueId();

        // Limitation des livres enchantés (1 par heure)
        if (result == Material.ENCHANTED_BOOK) {
            long lastTrade = WizardStoneCraft.getInstance().bookTradeCooldown.getOrDefault(playerUUID, 0L);
            if (System.currentTimeMillis() - lastTrade < 3600000) {
                event.setCancelled(true);
                player.sendMessage("§7[§e?§7]§c Vous devez attendre avant d'acheter un autre §blivre enchanté §7[§c!§7]");
                return;
            }
            WizardStoneCraft.getInstance().bookTradeCooldown.put(playerUUID, System.currentTimeMillis());
        }
    }
}
