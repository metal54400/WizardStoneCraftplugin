package fr.WizardStoneCraft.Listener.réputation;

import fr.WizardStoneCraft.API.event.WeeklyReputationGiveEvent;
import fr.WizardStoneCraft.Manager.ReputationManager;
import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RepuListener implements Listener {

    private final ReputationManager reputationManager;

    public RepuListener(WizardStoneCraft plugin) {
        this.reputationManager = plugin.getReputationManager();
    }

    @EventHandler
    public void onWeeklyReputationGain(WeeklyReputationGiveEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayerId());

        if (player != null && player.isOnline()) {
            reputationManager.addReputation(player, 1);
            player.sendMessage("§7[§e?§7] §aVous avez obtenu 1 point de réputation !");
        }
    }
}
