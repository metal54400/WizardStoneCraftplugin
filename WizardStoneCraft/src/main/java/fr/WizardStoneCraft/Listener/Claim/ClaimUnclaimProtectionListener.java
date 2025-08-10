package fr.WizardStoneCraft.Listener.Claim;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;

public class ClaimUnclaimProtectionListener implements Listener {

    private final Plugin plugin;

    public ClaimUnclaimProtectionListener(Plugin plugin) {
        this.plugin = plugin;
    }

    // Empêche /unclaim si joueur proche
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().toLowerCase().startsWith("/unclaim")) return;

        Player player = event.getPlayer();
        Claim claim = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore
                .getClaimAt(player.getLocation(), true, null);
        if (claim == null || !claim.ownerID.equals(player.getUniqueId())) return;

        Location center = claim.getLesserBoundaryCorner().clone().add(claim.getGreaterBoundaryCorner()).multiply(0.5);

        for (Player nearby : Bukkit.getOnlinePlayers()) {
            if (nearby.equals(player)) continue;
            if (!nearby.getWorld().equals(center.getWorld())) continue;

            if (nearby.getLocation().distance(center) <= 30) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "§7[§c!§7] Vous ne pouvez pas /unclaim cette zone car un joueur est à proximité !");
                return;
            }
        }
    }

    // Avertit les joueurs proches après un unclaim
    @EventHandler
    public void onClaimDeleted(ClaimDeletedEvent event) {
        Location center = event.getClaim().getLesserBoundaryCorner()
                .clone().add(event.getClaim().getGreaterBoundaryCorner()).multiply(0.5);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(center.getWorld())) continue;
            if (player.getLocation().distance(center) <= 50) {
                player.sendMessage(ChatColor.RED + "§7[§e?§7] Une zone proche vient d'être unclaim. Elle n'est plus sécurisée !");
            }
        }
    }
}