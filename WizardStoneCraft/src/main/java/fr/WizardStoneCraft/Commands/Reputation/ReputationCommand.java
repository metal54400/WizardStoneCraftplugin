package fr.WizardStoneCraft.Commands.Reputation;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static org.bukkit.Bukkit.getPlayer;

public class ReputationCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Seul un joueur ou la console peut exécuter la commande
        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            return true;
        }

        Player targetPlayer;
        UUID targetId;
        String targetName;

        // Si pas d'argument => afficher la réputation du joueur qui a tapé la commande (si c'est un joueur)
        if (args.length < 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Vous devez préciser un joueur depuis la console.");
                return true;
            }
            targetPlayer = (Player) sender;
            targetId = targetPlayer.getUniqueId();
            targetName = targetPlayer.getName();
        } else {
            // Si argument, récupérer le joueur ciblé (en ligne ou hors ligne)
            String argPlayerName = args[0];
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(argPlayerName);

            if (offlineTarget == null || !offlineTarget.hasPlayedBefore()) {
                sender.sendMessage(WizardStoneCraft.getInstance().getMessage("player_not_found"));
                return true;
            }

            targetId = offlineTarget.getUniqueId();
            targetName = offlineTarget.getName();

            // Essayer de récupérer le joueur en ligne (optionnel, mais utile pour afficher le nom exact)
            targetPlayer = getPlayer(targetName);
        }

        // Récupérer la réputation, la charger si besoin
        int rep = WizardStoneCraft.getInstance().reputation.getOrDefault(targetId, WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(targetId));
        String status = WizardStoneCraft.getInstance().getReputationManager().getReputationStatus(rep);

        sender.sendMessage(WizardStoneCraft.getInstance().getMessage("reputation_status")
                .replace("%player%", targetName)
                .replace("%rep%", WizardStoneCraft.getInstance().getReputationManager().getReputationPrefixe(rep))
                .replace("%reputation%", String.valueOf(rep))
                .replace("%liste%", "" +
                        "\n" +
                        "§7- §d☬ Honorable§F = 100§7\n" +
                        "- §2☬ Bonne§f = 50§7 \n" +
                        "- §a☬ correct§f = 10§7\n" +
                        "- §7☬ Neutre§f = 0§7\n" +
                        "- §6☬ Dangereux§f = -10§7\n" +
                        "- §c☬ Mauvaise§f = -50§7\n" +
                        "- §4☬ Horrible§f = -100§7\n" +
                        "\n")
                .replace("%status%", status));

        return true;
    }
}
