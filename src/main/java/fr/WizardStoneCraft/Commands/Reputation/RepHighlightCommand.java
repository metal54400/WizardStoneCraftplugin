package fr.WizardStoneCraft.Commands.Reputation;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getPlayer;

public class RepHighlightCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(WizardStoneCraft.getInstance().getMessage("usage").replace("%command%", label));
            return true;
        }

        String targetPlayer = args[0];
        Player target = getPlayer(targetPlayer);
        if (target == null) {
            sender.sendMessage(WizardStoneCraft.getInstance().getMessage("player_not_found"));
            return true;
        }

        UUID targetId = target.getUniqueId();
        int rep = WizardStoneCraft.getInstance().reputation.getOrDefault(targetId, WizardStoneCraft.getInstance().loadPlayerReputation(targetId));
        String status = WizardStoneCraft.getInstance().getReputationStatus(rep);
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return List.of();
    }
}