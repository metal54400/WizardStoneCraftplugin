package fr.WizardStoneCraft.Commands;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;


public class PassiveCommand implements CommandExecutor {
    public static final HashMap<UUID, Boolean> passivePlayers = new HashMap<>();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seuls les joueurs peuvent exécuter cette commande !");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("passif.manage")) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande !");
            return true;
        }

        if (label.equalsIgnoreCase("passifset")) {
            setPassive(player, true);
            player.sendMessage(ChatColor.GREEN + "Mode Passif activé !");
        } else if (label.equalsIgnoreCase("passifunset")) {
            setPassive(player, false);
            player.sendMessage(ChatColor.RED + "Mode Passif désactivé !");
        }

        return true;
    }
    public static void setPassive(Player player, boolean passive) {
        int reputations = WizardStoneCraft.getInstance().loadPlayerReputation(player.getUniqueId());

        // Désactiver automatiquement si la réputation est < -70
        if (reputations < -70) {
            passive = false;
        }

        passivePlayers.put(player.getUniqueId(), passive);

        if (passive) {
            player.setDisplayName(ChatColor.GREEN + player.getName());
            player.sendMessage(ChatColor.AQUA + "§7[§e?§7]§a Vous êtes désormais en Mode Passif !");
        } else {
            player.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous avez perdu le Mode Passif et ne pouvez plus l'activer.");
        }
    }

}