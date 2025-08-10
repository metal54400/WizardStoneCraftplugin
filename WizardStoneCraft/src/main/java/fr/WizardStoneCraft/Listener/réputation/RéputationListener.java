package fr.WizardStoneCraft.Listener.réputation;

import com.booksaw.betterTeams.Team;
import fr.WizardStoneCraft.Listener.PvpListener;
import fr.WizardStoneCraft.WizardStoneCraft;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import net.luckperms.api.LuckPerms;
import me.realized.duels.api.DuelsAPI;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;


public class RéputationListener implements Listener {
    public static TabAPI tabAPI;
    public Map<UUID, List<Long>> killTimestamps = new HashMap<>();

    //reputation
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        WizardStoneCraft plugin = WizardStoneCraft.getInstance();
        PvpListener.updateNametag(player);
        long lastJoinTime = plugin.getLastJoinReward(playerId);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastJoinTime >= 24 * 60 * 60 * 1000L) {
            int currentRep = plugin.reputation.getOrDefault(playerId, WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(playerId));
            int newRep = Math.min(currentRep + plugin.pointsJoin, plugin.MAX_REP);

            plugin.reputation.put(playerId, newRep);
            WizardStoneCraft.getInstance().getReputationManager().savePlayerReputation(playerId, newRep);
            plugin.setLastJoinReward(playerId, currentTime);
            player.sendMessage(plugin.getMessage("reputation_gained"));
        }
        plugin.protectedPlayers.put(playerId, System.currentTimeMillis());
        player.sendMessage("§7[§e?§7] §aVous êtes protégé pendant 90 secondes après votre connexion §7[§c!§7]");

        // Affiche la réputation actuelle toutes les 10 minutes
        Bukkit.getScheduler().runTaskTimer(WizardStoneCraft.getInstance(), () -> {
            if (player.isOnline()) {
                int rep = WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(player.getUniqueId());
                int repPrefix = plugin.reputation.getOrDefault(
                        player,
                        WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(player.getUniqueId())
                ); // Suppose que cette méthode existe

                player.sendMessage("§e[?]§7 Votre réputation actuelle " + WizardStoneCraft.getInstance().getReputationManager().getReputationPrefixe(repPrefix) + " (§7" + rep + "§7/§7120§7)");
            }
        }, 0L, 20L * 600); // toutes les 600 secondes = 10 min

    }

    @EventHandler
    public void onPlayerChats(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Vérifie si le joueur est mute
        if (WizardStoneCraft.getInstance().mutedPlayers.containsKey(playerId)) {
            long muteExpiration = WizardStoneCraft.getInstance().mutedPlayers.get(playerId);
            if (muteExpiration > currentTime) {
                player.sendMessage(WizardStoneCraft.getInstance().getMessage("chat_muted"));
                event.setCancelled(true);
            } else {
                unmutePlayer(playerId); // Supprime le mute si expiré
            }
        }
    }

    public void unmutePlayer(UUID playerId) {
        WizardStoneCraft.getInstance().mutedPlayers.remove(playerId);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer == victim) return;

        DuelsAPI duelsAPI = new DuelsAPI();
        // Ignore les kills si l'un des deux est en duel
        if (duelsAPI.isInMatch(killer) || duelsAPI.isInMatch(victim)) return;

        UUID killerId = killer.getUniqueId();
        UUID victimId = victim.getUniqueId();
        long now = System.currentTimeMillis();

        // --- Focus kill (kill répétitif sur même joueur) ---
        WizardStoneCraft.getInstance().killHistory.putIfAbsent(killerId, new HashMap<>());
        Map<UUID, Long> killerRecords = WizardStoneCraft.getInstance().killHistory.get(killerId);

        if (killerRecords.containsKey(victimId)) {
            long lastKillTime = killerRecords.get(victimId);
            if (now - lastKillTime < 172800000L) { // 48h en ms
                killer.sendMessage(ChatColor.RED + "⚠ Focus de kill détecté ! Vous perdez 20 points de réputation.");
                victim.sendMessage(ChatColor.YELLOW + "🚨 " + killer.getName() + " vous a tué en moins de 48h. Un modérateur peut être alerté.");

                applyReputationPenalty(killer, 20);

                for (Player admin : Bukkit.getOnlinePlayers()) {
                    if (admin.hasPermission("wizardstonecraft.moderator")) {
                        admin.sendMessage(ChatColor.RED + "⚠ " + killer.getName() + " a tué " + victim.getName() + " en moins de 48h !");
                    }
                }
                return; // On stop ici
            }
        }
        killerRecords.put(victimId, now);

        // --- Limite globale de kills (max 2 kills en 48h) ---

        List<Long> timestamps = killTimestamps.getOrDefault(killerId, new ArrayList<>());

        // Supprime les timestamps plus vieux que 48h
        timestamps.removeIf(time -> now - time > 172800000);

        if (timestamps.size() < 2) {
            int currentRep = WizardStoneCraft.getInstance().reputation.getOrDefault(killerId,
                    WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(killerId));
            int newRep = WizardStoneCraft.getInstance().getReputationManager().removeReputation(killer, 2);

            WizardStoneCraft.getInstance().reputation.put(killerId, newRep);
            WizardStoneCraft.getInstance().getReputationManager().savePlayerReputation(killerId, newRep);

            timestamps.add(now);
            killTimestamps.put(killerId, (List<Long>) timestamps);

            String message = WizardStoneCraft.getInstance().getMessage("reputation_lost");
            if (message != null) {
                killer.sendMessage(ChatColor.RED + message.replace("%points%", String.valueOf(newRep)));
            } else {
                killer.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous avez perdu " + newRep + " de réputation car vous avez tué plusieurs joueurs !");
            }
        } else {
            killer.sendMessage(ChatColor.GRAY + "§7[§e?§7]§a Vous avez atteint la limite de perte de réputation pour 48h.");
        }
    }

    private void applyReputationPenalty(Player player, int amount) {
        // Exemple : Ajout d'une méthode pour gérer la réputation du joueur
        WizardStoneCraft.getInstance().getReputationManager().getReputation(player.getUniqueId());
        WizardStoneCraft.getInstance().getReputationManager().removeReputation(player, amount);
    }


    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;

        // Ignorer les kills pendant un duel
        final DuelsAPI duelsAPI = new DuelsAPI();
        if (duelsAPI.isInMatch(killer) || duelsAPI.isInMatch(victim)) return;

        UUID killerId = killer.getUniqueId();
        long now = System.currentTimeMillis();

        List<Long> timestamps = killTimestamps.getOrDefault(killerId, new ArrayList<>());

        timestamps.removeIf(time -> now - time > 172800000);

        if (timestamps.size() < 2) {
            int currentRep = WizardStoneCraft.getInstance().reputation.getOrDefault(killerId, WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(killerId));
            int newRep = WizardStoneCraft.getInstance().getReputationManager().removeReputation(killer, 2);

            WizardStoneCraft.getInstance().reputation.put(killerId, newRep);
            WizardStoneCraft.getInstance().getReputationManager().savePlayerReputation(killerId, newRep);

            timestamps.add(now);
         killTimestamps.put(killerId, (List<Long>) timestamps);

            String message = WizardStoneCraft.getInstance().getMessage("reputation_lost");
            if (message != null) {
                killer.sendMessage(ChatColor.RED + message.replace("%points%", String.valueOf(newRep)));
            } else {
                killer.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous avez perdu " + newRep + " de réputation car vous avez tué le joueur plusieurs fois !");
            }
        } else {
            killer.sendMessage(ChatColor.GRAY + "§7[§e?§7]§a Vous avez atteint la limite de perte de réputation pour 48h.");
        }
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase().split(" ")[0]; // Récupère juste la commande

        Player player = event.getPlayer();
        if (WizardStoneCraft.getInstance().blockedCommands.contains(command)) {
            int reputation = WizardStoneCraft.getInstance().getReputationManager().getReputation(player.getUniqueId()); // Obtenir la réputation du joueur

            if (reputation <= -50) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "§7[§e?§7] Votre réputation est §ctrop basse §7pour utiliser cette commande §7[§c!§7]");
            }
        }
    }
    @EventHandler
    public void onCommandPreprocesss(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().toLowerCase().split(" ");
        String command = args[0];

        // Vérifie si c'est une commande de téléportation
        if (command.equals("/tpa") || command.equals("/tpahere")) {
            if (args.length < 2) return; // Vérifie s'il y a bien un pseudo après la commande

            Player sender = event.getPlayer();
            Player target = WizardStoneCraft.getInstance().getServer().getPlayer(args[1]); // Récupère le joueur cible

            if (target != null && WizardStoneCraft.getInstance().getReputationManager().getReputation(target.getUniqueId()) <= -50) {
                event.setCancelled(true);
                sender.sendMessage(ChatColor.RED + "§7[§e?§7] Vous ne pouvez pas vous téléporter à " + target.getName() + " car il a une §cmauvaise réputation §7[§c!§7]");
            }
        }
    }



}
