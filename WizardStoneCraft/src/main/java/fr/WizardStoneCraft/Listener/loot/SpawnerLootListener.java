package fr.WizardStoneCraft.Listener.loot;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.*;

public class SpawnerLootListener implements Listener {

    private static final int SEARCH_RADIUS = 64;

    @EventHandler
    public void onSpawnerSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SPAWNER) return;

        LivingEntity mob = event.getEntity();
        Location loc = mob.getLocation();

        // Empêche le mob de spawn
        event.setCancelled(true);

        // Cherche un conteneur proche
        Container container = findNearestContainer(loc, SEARCH_RADIUS);
        if (container == null) return;

        // Récupère le loot vanilla
        List<ItemStack> drops = getLootFromVanillaLootTable(mob);
        if (drops.isEmpty()) return;

        // Ajoute les loots dans le conteneur
        Inventory inv = container.getInventory();
        for (ItemStack drop : drops) {
            inv.addItem(drop);
        }
    }

    private Container findNearestContainer(Location center, int radius) {
        Container closest = null;
        double closestDist = Double.MAX_VALUE;
        for (int x = center.getBlockX() - radius; x <= center.getBlockX() + radius; x++) {
            for (int y = Math.max(0, center.getBlockY() - radius); y <= Math.min(255, center.getBlockY() + radius); y++) {
                for (int z = center.getBlockZ() - radius; z <= center.getBlockZ() + radius; z++) {
                    Block block = new Location(center.getWorld(), x, y, z).getBlock();
                    if (block.getState() instanceof Container container) {
                        double dist = center.distance(block.getLocation());
                        if (dist < closestDist && hasFreeSpace(container.getInventory())) {
                            closest = container;
                            closestDist = dist;
                        }
                    }
                }
            }
        }
        return closest;
    }

    private boolean hasFreeSpace(Inventory inv) {
        return Arrays.stream(inv.getStorageContents()).anyMatch(i -> i == null || i.getType().isAir());
    }

    private List<ItemStack> getLootFromVanillaLootTable(LivingEntity mob) {
        Location loc = mob.getLocation();
        NamespacedKey key = getLootTableKey(mob.getType());

        if (key == null) return List.of();

        LootTable lootTable = Bukkit.getLootTable(key);
        if (lootTable == null) return List.of();

        LootContext context = new LootContext.Builder(loc)
                .luck(0f)
                .lootedEntity(mob)
                .killer(null)
                .build();

        return List.copyOf(lootTable.populateLoot(new Random(), context));
    }

    private NamespacedKey getLootTableKey(EntityType type) {
        return new NamespacedKey("minecraft", "entities/" + type.name().toLowerCase());
    }
}
