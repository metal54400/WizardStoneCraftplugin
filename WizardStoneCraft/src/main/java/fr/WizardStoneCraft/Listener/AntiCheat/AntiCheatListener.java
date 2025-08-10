package fr.WizardStoneCraft.Listener.AntiCheat;

import fr.WizardStoneCraft.WizardStoneCraft;
import me.realized.duels.api.DuelsAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class AntiCheatListener implements Listener {
    public DuelsAPI duelsAPI;
    public Map<UUID, List<Long>> killTimestamps = new HashMap<>();
    public Map<UUID, List<Long>> timestamps = new HashMap<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!WizardStoneCraft.getInstance().antiCheatEnabled) return;

        Player player = event.getPlayer();
        if (player.hasPermission("wizardstonecraft.anticheat.bypass")) return;

        Material block = event.getBlock().getType();
        UUID uuid = player.getUniqueId();

        if (block == Material.DIAMOND_ORE || block == Material.EMERALD_ORE || block == Material.ANCIENT_DEBRIS) {
            WizardStoneCraft.getInstance().minedOres.put(uuid, WizardStoneCraft.getInstance().minedOres.getOrDefault(uuid, 0) + 1);
            Bukkit.getScheduler().runTaskLater(WizardStoneCraft.getInstance(), () -> WizardStoneCraft.getInstance().minedOres.put(uuid, WizardStoneCraft.getInstance().minedOres.get(uuid) - 1), 600L);

            if (WizardStoneCraft.getInstance().minedOres.get(uuid) > 5) {
                alertAdmins(player, "X-Ray détecté ! (minage suspect de minerais rares)");
                WizardStoneCraft.getInstance().banPlayer(player, "X-Ray détecté");
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!WizardStoneCraft.getInstance().antiCheatEnabled) return;

        Player player = event.getPlayer();
        if (player.hasPermission("wizardstonecraft.anticheat.bypass")) return;

        UUID uuid = player.getUniqueId();

        if (player.getGameMode() == GameMode.CREATIVE || player.isGliding()) {
            return;
        }

        if (!player.isOnGround() && player.getVelocity().getY() > 0.5) {
            WizardStoneCraft plugin = WizardStoneCraft.getInstance();
            plugin.flyWarnings.put(uuid, plugin.flyWarnings.getOrDefault(uuid, 0) + 1);

            alertAdmins(player, "Fly hack détecté ! (" + plugin.flyWarnings.get(uuid) + "/3)");

            if (plugin.flyWarnings.get(uuid) >= 3) {
                plugin.kickPlayer(player, "Fly hack détecté ! Vous avez été expulsé.");
                plugin.flyWarnings.remove(uuid);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!WizardStoneCraft.getInstance().antiCheatEnabled) return;

        Player player = event.getPlayer();

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            return;
        }

        WizardStoneCraft plugin = WizardStoneCraft.getInstance();

        plugin.lastClickTime.putIfAbsent(uuid, now);
        plugin.clickCounts.put(uuid, plugin.clickCounts.getOrDefault(uuid, 0) + 1);

        if (now - plugin.lastClickTime.get(uuid) >= 1000) {
            int cps = plugin.clickCounts.get(uuid);
            if (cps > 10) {
                plugin.kickPlayer(player, "Détection AutoClicker (CPS > 10)");
                alertAdmins(player, "AutoClicker détecté ! CPS: " + cps);
            }

            plugin.lastClickTime.put(uuid, now);
            plugin.clickCounts.put(uuid, 0);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!WizardStoneCraft.getInstance().antiCheatEnabled) return;
        if (event.getDamager() instanceof Player player) {
            if (player.hasPermission("wizardstonecraft.anticheat.bypass")) return;

            double baseDamage = event.getDamage();

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item != null && item.getType() == Material.MACE) {
                return;
            }

            if (baseDamage > 20) {
                alertAdmins(player, "KillAura détecté ! Dégâts: " + baseDamage);
                WizardStoneCraft.getInstance().banPlayer(player, "KillAura (Dégâts anormaux > 20)");
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!WizardStoneCraft.getInstance().antiCheatEnabled) return;

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;
        if (killer.hasPermission("wizardstonecraft.anticheat.bypass")) return;

        UUID killerUUID = killer.getUniqueId();
        UUID victimUUID = victim.getUniqueId();

        WizardStoneCraft plugin = WizardStoneCraft.getInstance();


        // Vérifie si killer ou victime sont en duel via DuelsAPI
        if (duelsAPI.isInMatch(killer) || duelsAPI.isInMatch(victim)) {
            // En duel, on ignore la détection et la perte de réputation
            return;
        }

        long now = System.currentTimeMillis();

        // TP Kill détection
        if (plugin.lastTeleport.containsKey(killerUUID) && now - plugin.lastTeleport.get(killerUUID) < 10_000) {
            alertAdmins(killer, "TP Kill détecté !");
        }

        // Initialisation des maps pour suivi des kills
        plugin.killTracking.putIfAbsent(killerUUID, new HashMap<>());
        Map<UUID, Integer> kills = plugin.killTracking.get(killerUUID);
        kills.put(victimUUID, kills.getOrDefault(victimUUID, 0) + 1);

        killTimestamps.putIfAbsent(killerUUID, new ArrayList<>());
        Map<UUID, List<Long>> victimKillTimes = (Map<UUID, List<Long>>) killTimestamps.get(killerUUID);
        victimKillTimes.putIfAbsent(victimUUID, new ArrayList<>());
        List<Long> killTimes = victimKillTimes.get(victimUUID);

        killTimes.add(now);
        killTimes.removeIf(t -> now - t > 5 * 60 * 1000);

        if (killTimes.size() >= 3) {
            alertAdmins(killer, "Focus détecté ! A tué " + victim.getName() + " 3 fois en 5 minutes");

            WizardStoneCraft.getInstance().getReputationManager().removeReputation(killer, 20);
            killer.sendMessage(ChatColor.RED + "§7[§e?§7]§c -20 de réputation pour avoir focus " + victim.getName() + " !");
            killTimes.clear();
        }
    }



    public void alertAdmins(Player player, String reason) {


        String message = org.bukkit.ChatColor.RED + "[AntiCheat] " + org.bukkit.ChatColor.YELLOW + player.getName() + " suspecté de triche ! (" + reason + ")";
        Bukkit.getOnlinePlayers().stream()
                .filter(admin -> admin.hasPermission("anticheat.alerts"))
                .forEach(admin -> admin.sendMessage(message));
        WizardStoneCraft.getInstance().getLogger().warning(message);
    }

    public void sendAntiCheatAlert(Player suspect, String reason) {


        String message = org.bukkit.ChatColor.RED + "[AntiCheat] " + org.bukkit.ChatColor.YELLOW + suspect.getName() + org.bukkit.ChatColor.RED + " est suspecté de triche ! (" + reason + ")";
        Bukkit.getOnlinePlayers().stream()
                .filter(admin -> admin.hasPermission("anticheat.alerts") && WizardStoneCraft.getInstance().alertEnabledPlayers.contains(admin))
                .forEach(admin -> admin.sendMessage(message));
        for (Player admin : WizardStoneCraft.getInstance().alertEnabledPlayers) {
            admin.sendMessage(message);
        }

        WizardStoneCraft.getInstance().getLogger().warning("[AntiCheat] " + suspect.getName() + " suspecté de triche (" + reason + ")");
    }
}
