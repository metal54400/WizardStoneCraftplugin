package fr.DeepStone.WizardStoneCraft;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DragonRewardListener implements Listener {

    private final Deepstone plugin;
    private final File dataFile;
    private final YamlConfiguration data;

    public DragonRewardListener(Deepstone plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;
        if (!(event.getEntity().getKiller() instanceof Player)) return;

        Player player = event.getEntity().getKiller();
        UUID uuid = player.getUniqueId();

        // Check 1 : a-t-il déjà reçu ?
        if (data.getBoolean(uuid.toString(), false)) {
            player.sendMessage("§7[§c!§7] Vous avez déjà reçu une récompense pour avoir vaincu l'Ender Dragon.");
            return;
        }

        // Check 2 : a-t-il 7 jours de jeu ?
        long ticks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long hours = ticks / (20 * 60 * 60);
        if (hours < 168) {
            player.sendMessage("§7[§c!§7] §eVous devez avoir au moins 7 jours de jeu pour recevoir les élytres.");
            return;
        }

        // Récompense
        player.getInventory().addItem(new ItemStack(Material.ELYTRA));
        player.sendMessage("§7[§e?§7]§aFélicitations ! Vous recevez une paire d’élytres pour avoir vaincu l'Ender Dragon !");

        // Enregistre l’UUID
        data.set(uuid.toString(), true);
        try {
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (to == null) return;

        List<LivingEntity> leashed = player.getNearbyEntities(10, 10, 10).stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(e -> e.isLeashed() && player.equals(e.getLeashHolder()))
                .collect(Collectors.toList());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (LivingEntity entity : leashed) {
                entity.teleport(to);
            }
        }, 2L);
    }
}

