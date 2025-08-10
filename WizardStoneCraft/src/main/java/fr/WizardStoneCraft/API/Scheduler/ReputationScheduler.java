package fr.WizardStoneCraft.API.Scheduler;

import fr.WizardStoneCraft.API.event.WeeklyReputationGiveEvent;
import fr.WizardStoneCraft.Manager.ReputationManager;
import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ReputationScheduler {

    private final WizardStoneCraft plugin;
    private final ReputationManager reputationManager;

    public ReputationScheduler(WizardStoneCraft plugin) {
        this.plugin = plugin;
        this.reputationManager = plugin.getReputationManager();
    }

    public void startWeeklyTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Bukkit.getPluginManager().callEvent(new WeeklyReputationGiveEvent(player.getUniqueId()));
                }
            }
        }.runTaskTimer(plugin, 0L, 7 * 24 * 60 * 60 * 20L); // 7 jours * 24h * 60min * 60sec * 20 ticks
    }
}
