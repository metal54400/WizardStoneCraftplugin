package fr.WizardStoneCraft.Listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShulkerShellDropListener implements Listener {

    private final Map<UUID, Object> lastDamagerMap = new HashMap<>();

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        if (!(damaged instanceof Shulker)) return;

        Entity damager = event.getDamager();

        if (damager instanceof Snowball snowball && snowball.getShooter() instanceof Snowman) {
            lastDamagerMap.put(damaged.getUniqueId(), EntityType.SNOW_GOLEM);
        } else if (damager instanceof Player player) {
            lastDamagerMap.put(damaged.getUniqueId(), player.getUniqueId()); // Stocke l'UUID du joueur
        } else if (damager instanceof Shulker) {
            lastDamagerMap.put(damaged.getUniqueId(), EntityType.SHULKER);
        } else {
            lastDamagerMap.remove(damaged.getUniqueId());
        }
    }


    @EventHandler
    public void onShulkerDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Shulker)) return;

        Object damagerInfo = lastDamagerMap.remove(entity.getUniqueId());

        // Supprimer les drops par défaut
        event.getDrops().removeIf(item -> item.getType() == Material.SHULKER_SHELL);

        double chance = 0;

        if (damagerInfo instanceof UUID playerUUID) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                ItemStack weapon = player.getInventory().getItemInMainHand();
                int lootingLevel = weapon.getEnchantmentLevel(Enchantment.LOOTING);

                chance = (lootingLevel > 0) ? 0.3 : 0.5;
            }

        } else if (damagerInfo == EntityType.SNOW_GOLEM || damagerInfo == EntityType.SHULKER) {
            chance = 0.2;
        }

        if (Math.random() < chance) {
            event.getDrops().add(new ItemStack(Material.SHULKER_SHELL));
        }
    }

}
