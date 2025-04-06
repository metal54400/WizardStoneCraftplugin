package fr.WizardStoneCraft.Commands.Anticheat;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;

public class AntiCheatListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material block = event.getBlock().getType();
        UUID uuid = player.getUniqueId();

        if (block == Material.DIAMOND_ORE || block == Material.EMERALD_ORE || block == Material.ANCIENT_DEBRIS) {
            WizardStoneCraft.getInstance().minedOres.put(uuid, WizardStoneCraft.getInstance().minedOres.getOrDefault(uuid, 0) + 1);
            Bukkit.getScheduler().runTaskLater((Plugin) this, () -> WizardStoneCraft.getInstance().minedOres.put(uuid, WizardStoneCraft.getInstance().minedOres.get(uuid) - 1), 600L);

            if (WizardStoneCraft.getInstance().minedOres.get(uuid) > 5) {
                WizardStoneCraft.getInstance().alertAdmins(player, "X-Ray détecté ! (minage suspect de minerais rares)");
                WizardStoneCraft.getInstance().banPlayer(player, "X-Ray détecté");
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!player.isOnGround() && player.getGameMode() != GameMode.CREATIVE) {
            if (player.getVelocity().getY() > 0.5) {
                WizardStoneCraft.getInstance().flyWarnings.put(uuid, WizardStoneCraft.getInstance().flyWarnings.getOrDefault(uuid, 0) + 1);
                WizardStoneCraft.getInstance().alertAdmins(player, "Fly hack détecté ! (" + WizardStoneCraft.getInstance().flyWarnings.get(uuid) + "/3)");
                if (WizardStoneCraft.getInstance().flyWarnings.get(uuid) >= 3) {
                    WizardStoneCraft.getInstance().kickPlayer(player, "Fly hack détecté ! Vous avez été expulsé.");
                    WizardStoneCraft.getInstance().flyWarnings.remove(uuid);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        WizardStoneCraft.getInstance().lastClickTime.putIfAbsent(uuid, now);
        WizardStoneCraft.getInstance().clickCounts.put(uuid, WizardStoneCraft.getInstance().clickCounts.getOrDefault(uuid, 0) + 1);

        if (now - WizardStoneCraft.getInstance().lastClickTime.get(uuid) >= 1000) {
            if (WizardStoneCraft.getInstance().clickCounts.get(uuid) > 10) {
                WizardStoneCraft.getInstance().kickPlayer(player, "Détection AutoClicker (CPS > 10)");
                WizardStoneCraft.getInstance().alertAdmins(player, "AutoClicker détecté ! CPS: " + WizardStoneCraft.getInstance().clickCounts.get(uuid));
            }
            WizardStoneCraft.getInstance().lastClickTime.put(uuid, now);
            WizardStoneCraft.getInstance().clickCounts.put(uuid, 0);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            double baseDamage = event.getDamage();

            if (baseDamage > 20) {
                WizardStoneCraft.getInstance().alertAdmins(player, "KillAura détecté ! Dégâts: " + baseDamage);
                WizardStoneCraft.getInstance().banPlayer(player, "KillAura (Dégâts anormaux > 20)");
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            UUID killerUUID = killer.getUniqueId();
            UUID victimUUID = victim.getUniqueId();
            long now = System.currentTimeMillis();

            if (WizardStoneCraft.getInstance().lastTeleport.containsKey(killerUUID) && now - WizardStoneCraft.getInstance().lastTeleport.get(killerUUID) < 10000) {
                WizardStoneCraft.getInstance().alertAdmins(killer, "TP Kill détecté !");
            }

           WizardStoneCraft.getInstance().killTracking.putIfAbsent(killerUUID, new HashMap<>());
            WizardStoneCraft.getInstance().killTracking.get(killerUUID).put(victimUUID, WizardStoneCraft.getInstance().killTracking.get(killerUUID).getOrDefault(victimUUID, 0) + 1);

            if (WizardStoneCraft.getInstance().killTracking.get(killerUUID).get(victimUUID) >= 3) {
                WizardStoneCraft.getInstance().alertAdmins(killer, "Focus détecté ! A tué " + victim.getName() + " 3 fois en 5 minutes");
            }
        }
    }

}
