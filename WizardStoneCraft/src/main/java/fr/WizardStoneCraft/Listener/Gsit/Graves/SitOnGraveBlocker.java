package fr.WizardStoneCraft.Listener.Gsit.Graves;

import dev.geco.gsit.api.event.EntitySitEvent;
import dev.geco.gsit.object.GSeat;
import fr.WizardStoneCraft.API.event.PlayerAttemptSitEvent;
import org.bukkit.Bukkit;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class SitOnGraveBlocker implements Listener {

    @EventHandler
    public void onEntitySit(EntitySitEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        GSeat seat = event.getSeat();

        PlayerAttemptSitEvent customEvent = new PlayerAttemptSitEvent(player, seat);
        Bukkit.getServer().getPluginManager().callEvent(customEvent);

        if (customEvent.isCancelled()) {
            if (player.isInsideVehicle()) {
                player.leaveVehicle();
            }
            String message = "§7[§c!§7] §cVous ne pouvez pas vous asseoir ici !";
            // Tu peux remplacer par : message = plugin.getConfig().getString("noSitMessage", message);
            player.sendMessage(message);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }
    }
}

