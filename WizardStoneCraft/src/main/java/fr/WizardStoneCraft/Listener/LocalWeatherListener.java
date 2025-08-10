package fr.WizardStoneCraft.Listener;

import fr.WizardStoneCraft.Commands.Claim.ClaimWeatherControl;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class LocalWeatherListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, null);
        if (claim == null) return;

        String weather = ClaimWeatherControl.getClaimWeather(claim);
        if ("pluie".equals(weather)) {
            Location loc = player.getLocation().add(0, 2, 0);
            player.spawnParticle(Particle.FALLING_WATER, loc, 50, 1, 1, 1, 0.1);
            player.playSound(loc, Sound.WEATHER_RAIN, 0.05f, 1.0f);
        }
    }
}
