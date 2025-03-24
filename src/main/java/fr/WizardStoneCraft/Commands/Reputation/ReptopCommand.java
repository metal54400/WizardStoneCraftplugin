package fr.WizardStoneCraft.Commands.Reputation;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReptopCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(WizardStoneCraft.getInstance().getMessage("top_reputations"));

        WizardStoneCraft.getInstance().reputation.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    UUID playerId = entry.getKey();
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
                    int reps = entry.getValue();

                    // Obtenir le préfixe de réputation
                    String prefix = WizardStoneCraft.getInstance().getReputationPrefixe(reps);

                    // Afficher le message avec le préfixe
                    sender.sendMessage( offlinePlayer.getName() + ": " + prefix + "§7 " + reps + " points de Réputation §7[§c!§7]");
                });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null; // Implémentation pour les suggestions automatiques
    }
}
