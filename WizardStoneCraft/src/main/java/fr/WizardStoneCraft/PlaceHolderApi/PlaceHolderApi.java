package fr.WizardStoneCraft.PlaceHolderApi;



import de.maxhenkel.voicechat.Voicechat;
import fr.WizardStoneCraft.Commands.PassiveCommand;
import fr.WizardStoneCraft.WizardStoneCraft;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


public class PlaceHolderApi extends PlaceholderExpansion {
    private static boolean placeholderAPILoaded = false;

    public static void checkPlaceholderAPI() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        System.out.println("§7[§e?§7] PlaceholderApi est activé");
        if (plugin != null) {
            placeholderAPILoaded = true;
        }
    }

    public static String parse(Player player, String text) {
        if (placeholderAPILoaded && player != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    @Override
    public String getIdentifier() {
        return "WizardStoneCraft";
    }

    @Override
    public String getAuthor() {
        return "metal54400";
    }

    @Override
    public String getVersion() {
        return "0.0.29.3-Alpha";
    }

    @Override
    public boolean register() {
        return super.register();
    }

    @Override
    public boolean canRegister() {
        return super.canRegister();
    }

    @Override
    public boolean persist() {
        return true; // Important : évite que PlaceholderAPI désenregistre après reload
    }


    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        WizardStoneCraft plugin = WizardStoneCraft.getInstance();

        int reps = plugin.reputation.getOrDefault(
                player,
                WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(player.getUniqueId())
        );


        // %wizardstonecraft_repprefixe% -> Affiche juste la valeur
        if (identifier.equalsIgnoreCase("repprefixe")) {
            return WizardStoneCraft.getInstance().getReputationManager().getReputationPrefixe(reps);
        }

        // %wizardstonecraft_repprefix% -> Affiche juste la valeur
        if (identifier.equalsIgnoreCase("repprefix")) {

            return WizardStoneCraft.getInstance().getReputationManager().getReputationStatus(reps);
        }
// %wizardstonecraft_repamount%
        if (identifier.equalsIgnoreCase("repamount")) {
            int repsss = plugin.reputation.getOrDefault(
                    player,
                    WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(player.getUniqueId())
            );
            return String.valueOf(repsss); // <- retourne le nombre en tant que String
        }
        //%wizardstonecraft_passif%
        if (identifier.equalsIgnoreCase("passif")) {
            UUID uuid = player.getUniqueId();

            // Vérifie si le joueur est dans la map des passifs
            Boolean isPassive = PassiveCommand.passivePlayers.getOrDefault(uuid, false);

            // Recharge la réputation pour s'assurer qu'elle est à jour
            int reputation = WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(uuid);

            // Si réputation < 80, on considère que le mode passif est désactivé, même si la map dit vrai
            if (reputation < 80) {
                isPassive = false;
            }

            return isPassive ? "§PVP Désactivé" : "§cPVP Activé";
        }

        //%wizardstonecraft_Vstatus%
        if (identifier.equalsIgnoreCase("Vstatus")) {
            if (Voicechat.SERVER.isCompatible(player)) {
                return "§a§l🎧"; // Voicechat installé et compatible
            } else {
                return "§7§l🎧"; // Voicechat non installé ou incompatible
            }
        }
        return null;
    }
}


