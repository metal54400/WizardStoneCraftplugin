package fr.WizardStoneCraft.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class RepUnmuteCommand implements CommandExecutor {
    private final Map<UUID, Long> mutedPlayers;

    public RepUnmuteCommand(Map<UUID, Long> mutedPlayers) {
        this.mutedPlayers = mutedPlayers;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wizardstonecraft.unmute")) {
            sender.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.YELLOW + "Utilisation: /repunmute <joueur>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "§7[§e?§7]§e Le joueur n'est pas en ligne.");
            return true;
        }

        UUID targetId = target.getUniqueId();
        if (!mutedPlayers.containsKey(targetId)) {
            sender.sendMessage(ChatColor.RED + "§7[§e?§7]§e Ce joueur n'est pas muet.");
            return true;
        }

        mutedPlayers.remove(targetId);
        sender.sendMessage(ChatColor.GREEN + "§7[§e?§7]§a Le joueur " + target.getName() + " a été démuté !");
        target.sendMessage(ChatColor.AQUA + "§7[§e?§7]§a Vous avez été démuté par un administrateur.");

        return true;
    }

    public boolean onCommands(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wizardstonecraft.mute")) {
            sender.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.YELLOW + "Utilisation: /repmute <joueur>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "§7[§e?§7]§e Le joueur n'est pas en ligne.");
            return true;
        }

        UUID targetId = target.getUniqueId();
        if (mutedPlayers.containsKey(targetId)) {
            sender.sendMessage(ChatColor.RED + "§7[§e?§7]§4 Ce joueur est déjà muet.");
            return true;
        }

        // Set mute duration (for example, mute for 5 minutes)
        long muteDuration = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes
        mutedPlayers.put(targetId, muteDuration);

        sender.sendMessage(ChatColor.GREEN + "§7[§e?§7]§a Le joueur " + target.getName() + " a été mis en sourdine pendant 5 minutes.");
        target.sendMessage(ChatColor.AQUA + "§7[§e?§7]§a Vous avez été mis en sourdine par un administrateur pendant 5 minutes. car votre reputation est basse");

        return true;
    }
}
