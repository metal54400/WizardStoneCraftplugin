package fr.WizardStoneCraft.Listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class élytra implements Listener {
    /*@EventHandler
    public void onElytraBoost(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getItem() != null && event.getItem().getType() == Material.FIREWORK_ROCKET) {
            if (player.isGliding()) { // Vérifie si le joueur est en vol avec l'elytra
                UUID playerId = player.getUniqueId();
                long currentTime = System.currentTimeMillis();

                if (elytraCooldown.containsKey(playerId)) {
                    long lastUse = elytraCooldown.get(playerId);
                    if (currentTime - lastUse < COOLDOWN_TIME) {
                        event.setCancelled(true);
                        player.sendMessage("§7[§e?§7] §cAttendez avant d'utiliser une autre fusée §7[§c!§7]");
                        return;
                    }
                }
                elytraCooldown.put(playerId, currentTime);
            }
        }
    }*/

@EventHandler
public void onElytraFly(PlayerToggleFlightEvent event) {
    Player player = event.getPlayer();

    if (player.isGliding() && player.getGameMode() != GameMode.CREATIVE) {
        player.setVelocity(player.getVelocity().multiply(0.7)); // Réduction de vitesse de 30%
    }
}
}
