package fr.WizardStoneCraft.Listener;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.*;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.*;

public class CreativeItemLoreListener implements Listener {

    private final NamespacedKey refundTagKey = new NamespacedKey(WizardStoneCraft.getInstance(), "rembourse");

    // Liste des items qui doivent RESTER échangeables avec les villageois (donc pas de lore)
    private static final Set<Material> VILLAGER_TRADE_ITEMS = Set.of(
            Material.EMERALD,
            Material.PAPER,
            Material.STRING,
            Material.WHEAT,
            Material.COAL,
            Material.IRON_INGOT,
            Material.GOLD_INGOT,
            Material.DIAMOND,
            Material.ROTTEN_FLESH,
            Material.CHICKEN,
            Material.BEEF,
            Material.BOOK,
            Material.CLAY_BALL,
            Material.BRICK,
            Material.CARROT,
            Material.POTATO,
            Material.PUMPKIN,
            Material.MELON_SLICE,
            Material.BREAD
            // Ajoute d'autres si besoin
    );

    private void tagAndOptionallyLore(ItemStack item, Player player) {
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Évite de doubler le tag
        if (!container.has(refundTagKey, PersistentDataType.STRING)) {
            container.set(refundTagKey, PersistentDataType.STRING, player.getName());

            // Ajouter un lore SEULEMENT si l’item n’est pas utilisé pour les trades
            if (!VILLAGER_TRADE_ITEMS.contains(item.getType())) {
                String loreLine = WizardStoneCraft.getInstance().getConfig().getString("loreitem", "&7Remboursé par &b{staff}")
                        .replace("{staff}", player.getName());
                loreLine = ChatColor.translateAlternateColorCodes('&', loreLine);

                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                if (!lore.contains(loreLine)) {
                    lore.add(loreLine);
                    meta.setLore(lore);
                }
            }

            item.setItemMeta(meta);
            Bukkit.getLogger().info("[ItemsLog] Un item a été remboursé par " + player.getName());
        }
    }

    private boolean isValidItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    @EventHandler
    public void onCreativeItemClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (player.getGameMode() != GameMode.CREATIVE) return;
        if (event.getView().getType() != InventoryType.CREATIVE) return;
        if (!player.hasPermission("moderator") && !player.isOp()) return;
        if (!WizardStoneCraft.getInstance().getConfig().getBoolean("addLoreOnCreative", true)) return;

        ItemStack item = event.getCursor();
        if (isValidItem(item)) {
            tagAndOptionallyLore(item, player);
        }
    }

    @EventHandler
    public void onCrystalPvP(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof EnderCrystal)) return;
        if (!(event.getEntity() instanceof Player player)) return;

        if (player.getWorld().getEnvironment() == World.Environment.NORMAL) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "§7[§c!§7] Les cristaux de l'end sont désactivés en PvP dans l'Overworld.");
        }
    }
}
