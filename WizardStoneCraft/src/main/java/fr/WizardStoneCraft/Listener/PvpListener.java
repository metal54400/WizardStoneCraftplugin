package fr.WizardStoneCraft.Listener;

import com.ranull.graves.data.LocationData;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.GravesXAPI;
import fr.WizardStoneCraft.API.Logger.LoggerUtil;
import fr.WizardStoneCraft.WizardStoneCraft;

import fr.WizardStoneCraft.data.TombData;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.realized.duels.api.DuelsAPI;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;



public class PvpListener implements Listener {

    private final WizardStoneCraft plugin;
    private final long PVP_TIMER_DURATION;
    private final GravesXAPI gravesXAPI;
    private final long PROTECTION_TIME;
    private static final Map<UUID, Long> protectedPlayers = new HashMap<>();
    private static final Set<UUID> newPlayers = new HashSet<>();
    private static final Map<UUID, PvPTimerData> pvpTimers = new HashMap<>();

    public PvpListener(WizardStoneCraft plugin, GravesXAPI gravesXAPI) {
        this.plugin = plugin;
        this.PVP_TIMER_DURATION = plugin.combatDurationSeconds * 1000L;
        this.PROTECTION_TIME = plugin.teleportProtectionSeconds * 1000L;
        this.gravesXAPI = gravesXAPI;
    }

    private static class PvPTimerData {
        BossBar bar;
        BukkitRunnable task;

        PvPTimerData(BossBar bar, BukkitRunnable task) {
            this.bar = bar;
            this.task = task;
        }
    }

    private static boolean hasTeleportProtection(Player player) {
        UUID uuid = player.getUniqueId();
        return protectedPlayers.containsKey(uuid) && (System.currentTimeMillis() - protectedPlayers.get(uuid)) < WizardStoneCraft.getInstance().teleportProtectionSeconds * 1000L;
    }

    private static boolean isPassive(Player player) {
        WizardStoneCraft instance = WizardStoneCraft.getInstance();
        DuelsAPI duelsAPI = new DuelsAPI();
        return !duelsAPI.isInMatch(player) && instance.getPassivePlayers().contains(player.getUniqueId());
    }
    Location location;

    public boolean canOpenTomb(Player player, LocationData tombLocationData) {
        if (tombLocationData == null) return false;
        if (!isPassive(player)) return false;

        Location tombLocation = tombLocationData.getLocation();
        if (tombLocation == null) return false;

        Grave grave = gravesXAPI.getGrave(player.getUniqueId());
        if (grave == null) return false;

        World world = tombLocation.getWorld();

        Location playerGraveLocation = grave.getLocation().toLocation(world);
        if (playerGraveLocation == null) return false;

        // Compare la position du coffre ouvert avec la position de la tombe du joueur
        // Tu peux utiliser equals, ou comparer les coordonnées avec une marge d'erreur si besoin
        return playerGraveLocation.equals(tombLocation);
    }






    private boolean canAttackInClaim(Player damager, Player victim) {
        DuelsAPI duelsAPI = new DuelsAPI();
        if (duelsAPI.isInMatch(damager) && duelsAPI.isInMatch(victim)) return true;
        if (isPassive(victim)) return false;
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(victim.getLocation(), true, null);
        return claim == null;
    }

    private void applyReputationLoss(Player player, int amount) {
        UUID uuid = player.getUniqueId();
        int current = plugin.reputation.getOrDefault(uuid, WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(uuid));
        int updated = current - amount;
        plugin.reputation.put(uuid, updated);
        WizardStoneCraft.getInstance().getReputationManager().savePlayerReputation(uuid, updated);
    }

    public static void updateNametag(Player player) {
        TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
        if (tabPlayer == null) return;

        WizardStoneCraft instance = WizardStoneCraft.getInstance();
        DuelsAPI duelsAPI = new DuelsAPI();

        String color;
        if (duelsAPI.isInMatch(player) || pvpTimers.containsKey(player.getUniqueId())) {
            color = "§c";
        } else if (hasTeleportProtection(player)) {
            color = newPlayers.contains(player.getUniqueId()) ? "§b" : "§9";
        } else if (isPassive(player)) {
            color = "§a";
        } else {
            color = "§7";
        }

        // Chargement forcé de la réputation à chaque appel
        int reps = WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(player.getUniqueId());
        instance.reputation.put(player.getUniqueId(), reps);

        String repPrefix = WizardStoneCraft.getInstance().getReputationManager().getReputationStatus(reps) + " ";
        TabAPI.getInstance().getNameTagManager().setPrefix(tabPlayer, repPrefix + color);
        TabAPI.getInstance().getNameTagManager().setSuffix(tabPlayer, "");
    }


    public void startPvPTimer(Player player) {
        UUID uuid = player.getUniqueId();

        if (pvpTimers.containsKey(uuid)) {
            PvPTimerData existing = pvpTimers.get(uuid);
            existing.task.cancel();
            existing.bar.removeAll();
            player.removePotionEffect(PotionEffectType.GLOWING);
            pvpTimers.remove(uuid);
        }

        int totalSeconds = (int) (PVP_TIMER_DURATION / 1000);
        BossBar bar = Bukkit.createBossBar("§l§4Mode combat (" + totalSeconds + "s)", BarColor.RED, BarStyle.SOLID);
        bar.addPlayer(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, totalSeconds * 20, 0, false, false));

        BukkitRunnable runnable = new BukkitRunnable() {
            int timeLeft = totalSeconds;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    bar.removeAll();
                    pvpTimers.remove(uuid);
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    bar.removeAll();
                    pvpTimers.remove(uuid);
                    player.removePotionEffect(PotionEffectType.GLOWING);
                    updateNametag(player);
                    cancel();
                    return;
                }

                bar.setTitle("§l§4Mode combat (" + timeLeft + "s)");
                bar.setProgress(timeLeft / (float) totalSeconds);
                player.sendActionBar("§cMode Combat actif - §7Ne quittez pas");
                timeLeft--;
            }
        };

        runnable.runTaskTimer(WizardStoneCraft.getInstance(), 0L, 20L);
        pvpTimers.put(uuid, new PvPTimerData(bar, runnable));
        updateNametag(player);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player damager = null;
        if (event.getDamager() instanceof Player p) damager = p;
        else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player shooter)
            damager = shooter;
        if (damager == null) return;



        DuelsAPI duelsAPI = new DuelsAPI();
        boolean damagerInDuel = duelsAPI.isInMatch(damager);
        boolean victimInDuel = duelsAPI.isInMatch(victim);

        if (damagerInDuel && victimInDuel) {
            startPvPTimer(victim);
            startPvPTimer(damager);
            return;
        }

        if (hasTeleportProtection(victim)) {
            if (!(damagerInDuel && victimInDuel)) { // si pas en duel
                event.setCancelled(true);
                damager.sendMessage("§7[§e?§7] §cCe joueur est protégé.");
                return;
            }
            // si en duel, ne rien faire (laisser passer)
        }

        if (isPassive(victim)) {
            event.setCancelled(true);
            damager.sendMessage("§7[§e?§7] §cCe joueur est en mode passif.");
            applyReputationLoss(damager, plugin.reputationLossAttackPassive);
            damager.sendMessage("§7[§e!§7] §c-" + plugin.reputationLossAttackPassive + " réputation");
            if (plugin.logPassiveAttacks) {
                LoggerUtil.log(damager.getName() + " a tenté d'attaquer " + victim.getName() + " (passif)");
            }
            return;
        }

        if (isPassive(damager)) {
            event.setCancelled(true);
            damager.sendMessage("§7[§e?§7] §cVous êtes en mode passif.");
            applyReputationLoss(damager, plugin.reputationLossPassiveAttacker);
            damager.sendMessage("§7[§e!§7] §c-" + plugin.reputationLossPassiveAttacker + " réputation");
            if (plugin.logPassiveAttacks) {
                LoggerUtil.log(damager.getName() + " (passif) a tenté d'attaquer " + victim.getName());
            }
            return;
        }

        if (!canAttackInClaim(damager, victim)) {
            event.setCancelled(true);
            damager.sendMessage("§7[§e?§7] §cLe PvP est interdit ici, sauf en duel.");
            return;
        }

        startPvPTimer(victim);
        startPvPTimer(damager);
    }

    private void applyReputationLoss(Player player, boolean attackerIsPassive) {
        UUID uuid = player.getUniqueId();
        int current = plugin.reputation.getOrDefault(uuid, WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(uuid));

        int amount;
        if (attackerIsPassive) {
            amount = plugin.getConfig().getInt("reputation_loss_passive_attacker", 1);
        } else {
            amount = plugin.getConfig().getInt("reputation_loss_attack_passive", 20);
        }

        int updated = current - amount;

        int minRep = plugin.getConfig().getInt("minimum-reputation", -120);
        int maxRep = plugin.getConfig().getInt("maximum-reputation", 120);

        if (updated < minRep) updated = minRep;
        if (updated > maxRep) updated = maxRep;

        plugin.reputation.put(uuid, updated);
        WizardStoneCraft.getInstance().getReputationManager().savePlayerReputation(uuid, updated);
    }


    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        protectedPlayers.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage("§7[§e?§7] §aVous êtes protégé pendant " + plugin.teleportProtectionSeconds + " secondes après la téléportation.");
        updateNametag(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && pvpTimers.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§7[§e?§7] §cVous ne pouvez pas interagir avec l'inventaire en combat.");
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().toLowerCase();

        if (pvpTimers.containsKey(player.getUniqueId())) {
            if (command.startsWith("/msg") || command.startsWith("/t")) return;
            event.setCancelled(true);
            player.sendMessage("§7[§e?§7] §cVous ne pouvez pas utiliser de commande pendant le combat.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (pvpTimers.containsKey(uuid)) {
            PvPTimerData data = pvpTimers.get(uuid);
            data.task.cancel();
            data.bar.removeAll();
            pvpTimers.remove(uuid);
        }
        protectedPlayers.remove(uuid);
        updateNametag(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updateNametag(player);
            if (isPassive(player)) player.sendMessage("§7[§e?§7] §aMode passif actif.");
        }, 1L);

        if (!player.hasPlayedBefore()) {
            newPlayers.add(player.getUniqueId());
            protectedPlayers.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage("§7[§e?§7] §bBienvenue ! Protégé " + plugin.teleportProtectionSeconds + " secondes.");
            setTemporaryBlueTag(player);
        }
    }

    public static void setTemporaryBlueTag(Player player) {
        DuelsAPI duelsAPI = new DuelsAPI();
        if (duelsAPI.isInMatch(player)) {
            // Le joueur est en duel, ne pas mettre le tag bleu
            return;
        }

        TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
        if (tabPlayer == null) return;

        TabAPI.getInstance().getNameTagManager().setPrefix(tabPlayer, ChatColor.BLUE.toString());
        TabAPI.getInstance().getNameTagManager().setSuffix(tabPlayer, "");
        Bukkit.getScheduler().runTaskLater(WizardStoneCraft.getInstance(), () -> updateNametag(player), WizardStoneCraft.getInstance().teleportProtectionSeconds * 20L);
    }


    public static void removePassiveMode(Player player) {
        WizardStoneCraft.getInstance().getPassivePlayers().remove(player.getUniqueId());
        updateNametag(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        if (pvpTimers.containsKey(uuid)) {
            PvPTimerData data = pvpTimers.get(uuid);
            data.task.cancel();
            data.bar.removeAll();
            pvpTimers.remove(uuid);
        }

        player.removePotionEffect(PotionEffectType.GLOWING);
        updateNametag(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> updateNametag(player), 1L);
    }


}
