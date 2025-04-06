package fr.WizardStoneCraft.Commands.Gems;

import fr.WizardStoneCraft.Manager.GemManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class GemCommand implements CommandExecutor {

    private final GemManager gemManager;

    public GemCommand(GemManager gemManager) {
        this.gemManager = gemManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("gems")) {
            if (args.length == 1) {
                String target = args[0];
                int gems = gemManager.getGems(target);
                sender.sendMessage("§a" + target + " a §e" + gems + " §agems.");
                return true;
            } else {
                sender.sendMessage("§cUsage: /gems <player>");
                return true;
            }
        }

        if (cmd.getName().equalsIgnoreCase("gemadd")) {
            if (args.length == 2) {
                String target = args[0];
                int amount;
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cLe montant doit être un nombre.");
                    return true;
                }

                gemManager.addGems(target, amount);
                sender.sendMessage("§aAjouté §e" + amount + " §agems à §b" + target);
                return true;
            } else {
                sender.sendMessage("§cUsage: /gemadd <player> <amount>");
                return true;
            }
        }

        if (cmd.getName().equalsIgnoreCase("gemstop")) {
            sender.sendMessage("§6Classement des Gems :");
            int rank = 1;
            for (Map.Entry<String, Integer> entry : gemManager.getTopGems().entrySet()) {
                sender.sendMessage("§e#" + rank + " §b" + entry.getKey() + " : §a" + entry.getValue() + " gems");
                rank++;
                if (rank > 10) break; // Limite à top 10
            }
            return true;
        }

        return false;
    }
}


