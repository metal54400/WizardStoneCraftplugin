package fr.WizardStoneCraft.Commands.Reputation;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReptopCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("§6Classement des Réputations :");

        // Trie toutes les réputations (en ligne et hors ligne)
        List<Map.Entry<UUID, Integer>> topReputations = WizardStoneCraft.getInstance().reputation.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        int rank = 1;
        for (Map.Entry<UUID, Integer> entry : topReputations) {
            UUID playerId = entry.getKey();
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
            int reps = entry.getValue();

            String prefix = WizardStoneCraft.getInstance().getReputationManager().getReputationPrefixe(reps);
            String playerName = player.getName() != null ? player.getName() : "Inconnu";

            // Affichage du rang et des informations sur la réputation
            sender.sendMessage("§e#" + rank + " §f" + playerName + " : " + prefix + "§7 " + reps + " points");
            rank++;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList(); // Pas de suggestions de tab completion
    }
}
