package fr.DeepStone.WizardStoneCraft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.Random;

public class PhantomDropListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onPhantomDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Phantom)) return;

        // Vérifie que c'est un joueur qui a tué le phantom
        if (!(event.getEntity().getKiller() instanceof Player)) return;

        // 1% de chance
        if (random.nextDouble() <= 0.50) {
            ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) enchantedBook.getItemMeta();

            if (meta != null) {
                meta.addStoredEnchant(Enchantment.FEATHER_FALLING, 5, true);
                enchantedBook.setItemMeta(meta);
                event.getDrops().add(enchantedBook);

                Player killer = event.getEntity().getKiller();
                killer.sendMessage("§7[§e?§7]§a Tu as obtenu un §dLivre Plume V §agrâce à ta victoire contre un Phantom !");
            }
        }
    }
}
