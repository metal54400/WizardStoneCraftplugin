package fr.WizardStoneCraft.Commands.Reputation;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class testrep implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        if (args.length != 1) {
            sender.sendMessage("§cUtilisation : /testrep <valeur>");
            return true;
        }

        try {
            int reputation = Integer.parseInt(args[0]);

            // Appels de test
            String prefix = WizardStoneCraft.getInstance().getReputationManager().getReputationPrefix(reputation);
            String status = WizardStoneCraft.getInstance().getReputationManager().getReputationStatus(reputation);
            String colored = WizardStoneCraft.getInstance().getReputationManager().getColoredReputationPrefix(reputation);
            String prefixe = WizardStoneCraft.getInstance().getReputationManager().getReputationPrefixe(reputation);

            sender.sendMessage("§eRéputation : §f" + reputation);
            sender.sendMessage("§6Prefix: §f" + prefix);
            sender.sendMessage("§bStatus: §f" + status);
            sender.sendMessage("§dPrefixe: §f" + prefixe);
            sender.sendMessage("§aColored: §f" + colored);

        } catch (NumberFormatException e) {
            sender.sendMessage("§cValeur invalide.");
        }

        return true;
    }
}
