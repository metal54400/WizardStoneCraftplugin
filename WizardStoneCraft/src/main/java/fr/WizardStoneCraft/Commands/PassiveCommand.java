package fr.WizardStoneCraft.Commands;

import fr.WizardStoneCraft.Listener.PvpListener;
import fr.WizardStoneCraft.WizardStoneCraft;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PassiveCommand implements CommandExecutor {

    public static final HashMap<UUID, Boolean> passivePlayers = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeul un joueur peut exécuter cette commande.");
            return true;
        }

        Player executor = (Player) sender;

        if (!executor.hasPermission("passif.manage")) {
            executor.sendMessage("§7[§e?§7]§c Vous n'avez pas la permission d'utiliser cette commande !");
            return true;
        }

        Player target = executor;

        if (args.length >= 1) {
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                executor.sendMessage("§7[§e?§7]§c Joueur introuvable !");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("passifset")) {
            setPassive(target, true, executor);
        } else if (command.getName().equalsIgnoreCase("passifunset")) {
            setPassive(target, false, executor);
        }

        return true;
    }

    public static void setPassive(Player player, boolean passive, Player executor) {
        int reputation = WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(player.getUniqueId());
        System.out.println("Reputation de " + player.getName() + " : " + reputation); // debug temporaire

        if (passive && reputation < 80) {
            if (player.equals(executor)) {
                player.sendMessage("§7[§e?§7]§c Vous devez avoir au moins 80 points de réputation pour activer le Mode Passif.");
            } else {
                executor.sendMessage("§7[§e?§7]§c " + player.getName() + " n'a pas assez de réputation pour le Mode Passif.");
            }
            return;
        }

        passivePlayers.put(player.getUniqueId(), passive);
        PvpListener.updateNametag(player);

        if (passive) {
            player.sendMessage("§7[§e?§7]§a Vous êtes désormais en Mode Passif !");
            if (!player.equals(executor)) {
                executor.sendMessage("§7[§e?§7]§a Mode Passif activé pour " + player.getName());
            }
        } else {
            player.sendMessage("§7[§e?§7]§c Vous avez désactivé le Mode Passif.");
            if (!player.equals(executor)) {
                executor.sendMessage("§7[§e?§7]§c Mode Passif désactivé pour " + player.getName());
            }
            PvpListener.removePassiveMode(player);
        }
    }
}
