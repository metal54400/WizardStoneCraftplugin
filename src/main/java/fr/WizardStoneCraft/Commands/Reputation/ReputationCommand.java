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

public class ReputationCommand  implements CommandExecutor {
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

        UUID targetId;
        String targetName;

        Player target = getPlayer(targetPlayer);
        if (target == null) {
            sender.sendMessage(WizardStoneCraft.getInstance().getMessage("player_not_found"));
            return true;
        } else {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetPlayer);
            if (!offlineTarget.hasPlayedBefore()) {
                sender.sendMessage(WizardStoneCraft.getInstance().getMessage("player_not_found"));
                return true;
            }
            targetId = offlineTarget.getUniqueId();
            targetName = offlineTarget.getName();
        }
        UUID targetid = target.getUniqueId();

        int rep = WizardStoneCraft.getInstance().reputation.getOrDefault(targetid, WizardStoneCraft.getInstance().loadPlayerReputation(targetid));
        String status = WizardStoneCraft.getInstance().getReputationStatus(rep);
        sender.sendMessage(WizardStoneCraft.getInstance().getMessage("reputation_status")
                .replace("%player%", target.getName())
                .replace("%rep%", WizardStoneCraft.getInstance().getReputationPrefixe(rep))
                .replace("%reputation%", String.valueOf(rep))
                .replace("%liste%", "" +
                        "\n"+
                        "§7- §dΩ Honorable§F = 100§7\n" +
                        "- §2Ω Bonne§f = 50§7 \n" +
                        "- §aΩ correct§f = 10§7\n" +
                        "- §7Ω Neutre§f = 0§7\n" +
                        "- §6Ω Dangereux§f = -10§7\n" +
                        "- §cΩ Mauvaise§f = -50§7\n" +
                        "- §4Ω Horrible§f = -100§7\n"+
                        "\n")
                .replace("%status%", status));

        return true;
    }


}
