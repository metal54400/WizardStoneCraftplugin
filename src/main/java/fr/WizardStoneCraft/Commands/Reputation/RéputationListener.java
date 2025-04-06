package fr.WizardStoneCraft.Commands.Reputation;

import com.earth2me.essentials.api.Economy;
import fr.WizardStoneCraft.WizardStoneCraft;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;

import static com.booksaw.betterTeams.Main.econ;
import static fr.WizardStoneCraft.Commands.Reputation.repspawnnpc.isPassive;

public class RéputationListener implements Listener {
    //reputation
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Vérifie si la réputation du joueur est inférieure ou égale à la valeur seuil
        int newRep = WizardStoneCraft.getInstance().reputation.getOrDefault(playerId, WizardStoneCraft.getInstance().loadPlayerReputation(playerId)) + WizardStoneCraft.getInstance().pointsJoin;
        WizardStoneCraft.getInstance().reputation.put(playerId, Math.min(newRep, WizardStoneCraft.getInstance().MAX_REP));
        player.sendMessage(WizardStoneCraft.getInstance().getMessage("reputation_gained"));
        WizardStoneCraft.getInstance().savePlayerReputation(playerId, Math.min(newRep, WizardStoneCraft.getInstance().MAX_REP));
        if (isPassive(player)) {
            player.setDisplayName(ChatColor.GREEN + player.getName());
        } else {
            player.setDisplayName(ChatColor.AQUA + player.getName()); // Cyan pour joueurs protégés
        }
        WizardStoneCraft.getInstance().protectedPlayers.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage("§7[§e?§7] §aVous êtes protégé pendant 90 secondes après votre connexion §7[§c!§7]");

    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        int rep = WizardStoneCraft.getInstance().reputation.getOrDefault(playerId, WizardStoneCraft.getInstance().loadPlayerReputation(playerId));
        String prefix = WizardStoneCraft.getInstance().getReputationStatus(rep);
        String gradePrefix = WizardStoneCraft.getInstance().getLuckPermsPrefix(player);
        event.setFormat(prefix + " " + ChatColor.RESET + "<%1$s> %2$s");
        Player players = event.getPlayer();

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
    public void onPlayerDeathsssssssss(PlayerDeathEvent event) {
        Player victim = (Player) event.getEntity();
        ;
        Player killer = victim.getKiller();

        if (killer == null || killer == victim) return; // Pas de suicide ou de mort sans tueur

        UUID killerUUID = killer.getUniqueId();
        UUID victimUUID = victim.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Initialiser l'historique du tueur si inexistant
        WizardStoneCraft.getInstance().killHistory.putIfAbsent(killerUUID, new HashMap<>());

        HashMap<UUID, Long> killerRecords = (HashMap<UUID, Long>) WizardStoneCraft.getInstance().killHistory.get(killerUUID);

        // Vérifie si le tueur a déjà tué cette victime
        if (killerRecords.containsKey(victimUUID)) {
            long lastKillTime = killerRecords.get(victimUUID);

            // Vérifie si c'était dans les dernières 48 heures
            if (currentTime - lastKillTime < 172800000L) { // 48h en millisecondes
                killer.sendMessage(net.md_5.bungee.api.ChatColor.RED + "⚠ Focus de kill détecté ! Vous perdez 20 points de réputation.");
                victim.sendMessage(net.md_5.bungee.api.ChatColor.YELLOW + "🚨 " + killer.getName() + " vous a tué en moins de 48h. Un modérateur peut être alerté.");

                // Appliquer la pénalité de réputation
                applyReputationPenalty(killer, 20);

                // Notifier les modérateurs
                for (Player admin : Bukkit.getOnlinePlayers()) {
                    if (admin.hasPermission("wizardstonecraft.moderator")) {
                        admin.sendMessage(net.md_5.bungee.api.ChatColor.RED + "⚠ " + killer.getName() + " a tué " + victim.getName() + " en moins de 48h !");
                        admin.sendMessage(net.md_5.bungee.api.ChatColor.RED + "➡ Une intervention peut être nécessaire.");
                    }
                }

                return;
            }
        }

        // Mettre à jour l'historique du tueur
        killerRecords.put(victimUUID, currentTime);
    }

    private void applyReputationPenalty(Player player, int amount) {
        // Exemple : Ajout d'une méthode pour gérer la réputation du joueur
        WizardStoneCraft.getInstance().getReputation(player);
        WizardStoneCraft.getInstance().removeReputation(player, amount);
    }

    private final Map<UUID, List<Long>> killTimestamps = new HashMap<>();

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            UUID killerId = killer.getUniqueId();
            long now = System.currentTimeMillis();

            // Charger la liste des kills du joueur
            List<Long> timestamps = killTimestamps.getOrDefault(killerId, new ArrayList<>());

            // Supprimer les kills plus vieux que 48h (172800000 ms)
            timestamps.removeIf(time -> now - time > 172800000);

            // Vérifier si le joueur a tué moins de 2 fois dans les 48h
            if (timestamps.size() < 2) {
                int currentRep = WizardStoneCraft.getInstance().reputation.getOrDefault(killerId, WizardStoneCraft.getInstance().loadPlayerReputation(killerId));
                int newRep = Math.max(currentRep + WizardStoneCraft.getInstance().pointsKills, -2);

                // Mettre à jour la réputation et sauvegarder
                WizardStoneCraft.getInstance().reputation.put(killerId, newRep);
                WizardStoneCraft.getInstance().savePlayerReputation(killerId, newRep);

                // Ajouter le kill actuel à la liste
                timestamps.add(now);
                killTimestamps.put(killerId, timestamps);

                // Envoyer le message au joueur
                String message = WizardStoneCraft.getInstance().getMessage("reputation_lost");
                if (message != null) {
                    killer.sendMessage(ChatColor.RED + message.replace("%points%", String.valueOf(newRep)));
                } else {
                    killer.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous avez perdu " + Math.abs(WizardStoneCraft.getInstance().pointsKills) +
                            " points de réputation. Nouvelle réputation : " + newRep);
                }
            } else {
                killer.sendMessage(ChatColor.GRAY + "§7[§e?§7]§a Vous avez atteint la limite de perte de réputation pour 48h.");
            }
        }
    }


    @EventHandler
    public void onPlayerEnterTrialChamber(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        World world = loc.getWorld();

        // Trouver la Trial Chamber la plus proche
        Location trialChamberLocation = (Location) world.locateNearestStructure(loc, Structure.TRIAL_CHAMBERS, 10, false);

        // Vérifier que la structure existe et que le joueur est dedans
        if (trialChamberLocation != null && loc.distanceSquared(trialChamberLocation) < 100) {
            UUID playerUUID = player.getUniqueId();

            // Vérifie si le joueur a déjà complété la quête
            if (!player.hasMetadata("completed_trial_quest")) {
                player.setMetadata("completed_trial_quest", new FixedMetadataValue(WizardStoneCraft.getInstance(), true));

                // Ajoute la réputation
                WizardStoneCraft.getInstance().addReputation(playerUUID, 10);

                // Ajoute l'argent (si Vault est installé)
                boolean economy = WizardStoneCraft.getInstance().setupEconomy();
                EconomyResponse response = econ.depositPlayer(player, 1000);

                if (response.transactionSuccess()) {
                    player.sendMessage(ChatColor.GREEN + "Vous avez complété la quête ! +10 Réputation et 1000€ !");
                } else {
                    player.sendMessage(ChatColor.RED + "Erreur lors de l'ajout de l'argent.");
                }
            } else {
                player.sendMessage(ChatColor.YELLOW + "Vous avez déjà complété cette quête !");
            }
        }
    }



    // Méthode pour ouvrir l'interface de quête


    // Gestion de l'événement lorsque le joueur clique dans l'interface


    // Démarrer la quête (enregistré dans les métadonnées du joueur)






    //reputation
}
