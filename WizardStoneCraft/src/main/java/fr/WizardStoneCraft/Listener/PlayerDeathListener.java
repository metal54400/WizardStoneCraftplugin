package fr.WizardStoneCraft.Listener;

import fr.WizardStoneCraft.WizardStoneCraft;
import me.realized.duels.api.DuelsAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Random;

public class PlayerDeathListener implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Si joueur en duel, ne pas drop la tête
        if (DuelsAPI.isInMatch(player)) {
            return;
        }

        // Test de la chance
        if (new Random().nextDouble() < WizardStoneCraft.getInstance().dropChance) {
            // Création de la tête
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(player);
                meta.setDisplayName("§cTête de " + player.getName());
                skull.setItemMeta(meta);
            }

            // Ajouter la tête dans les drops de la mort
            event.getDrops().add(skull);
        }
    }
}
