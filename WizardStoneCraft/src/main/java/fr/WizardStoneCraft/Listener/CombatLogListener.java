package fr.WizardStoneCraft.Listener;

import fr.WizardStoneCraft.WizardStoneCraft;
import me.realized.duels.api.DuelsAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;



public class CombatLogListener implements Listener {
    public final DuelsAPI duelsAPI;
    private final Map<UUID, Long> combatLog;

    public CombatLogListener(DuelsAPI duelsAPI, Map<UUID, Long> combatLog) {
        this.duelsAPI = duelsAPI;
        this.combatLog = combatLog;

    }

    @EventHandler
    public void onPlayerQuits(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Ne rien faire si le joueur est en duel (protégé)
        if (duelsAPI != null && duelsAPI.isInMatch(player)) {
            return;
        }

        // Vérifie si le joueur est en combat et le sanctionne
        Long combatTime = combatLog.get(uuid);
        if (combatTime != null && System.currentTimeMillis() - combatTime <= WizardStoneCraft.getInstance().COMBAT_TIME) {
            Bukkit.broadcastMessage(ChatColor.RED  + " §7[§e?§7]§c s'est déconnecté en combat et a été sanctionné !");
            player.setHealth(0.0);
            WizardStoneCraft.getInstance().getReputationManager().removeReputation(player, 20);
        }
    }
}
