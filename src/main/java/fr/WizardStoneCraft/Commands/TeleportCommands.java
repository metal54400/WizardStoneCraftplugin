package fr.WizardStoneCraft.Commands;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportCommands implements CommandExecutor {
    private final Map<UUID, UUID> pendingRequests; // Map pour suivre les demandes de téléportation

    public TeleportCommands() {
        this.pendingRequests = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("tpa")) {
            return handleTpa(sender, args);
        }
        if (command.getName().equalsIgnoreCase("tpyes")) {
            return handleTpyes(sender);
        }
        if (command.getName().equalsIgnoreCase("tpno")) {
            return handleTpno(sender);
        }
        return false;
    }

    private boolean handleTpa(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "§7[§e?§7]§e Seules les joueurs peuvent envoyer une demande de téléportation.");
            return true;
        }

        Player player = (Player) sender;

        // Vérifier la réputation du joueur
        if (!hasValidReputation(player)) {
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "§7[§e?§7]§e Utilisation: /tpa <joueur>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "§7[§e?§7]§e Le joueur spécifié n'est pas en ligne.");
            return true;
        }

        // Vérifier la réputation du joueur cible
        if (!hasValidReputation(target)) {
            player.sendMessage(ChatColor.RED + "§7[§e?§7]§e Vous ne pouvez pas vous téléporter vers un joueur avec une mauvaise réputation.");
            return true;
        }

        // Créer une demande de téléportation si tous les contrôles sont passés
        pendingRequests.put(target.getUniqueId(), player.getUniqueId());
        target.sendMessage(ChatColor.GREEN + "§7[§e?§7] " + player.getName() + " §asouhaite se téléporter vers vous.");

        sendInteractiveMessage(target, "Accepter", "/tpyes", ChatColor.GREEN);
        sendInteractiveMessage(target, "Refuser", "/tpno", ChatColor.RED);

        player.sendMessage(ChatColor.GREEN + "§7[§e?§7]§a Votre demande de téléportation a été envoyée à " + target.getName() + ".");
        return true;
    }

    private boolean handleTpyes(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "§7[§e?§7]§a Seules les joueurs peuvent accepter une demande de téléportation.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (!pendingRequests.containsKey(playerId)) {
            player.sendMessage(ChatColor.RED + "§7[§e?§7]§e Aucune demande de téléportation en attente.");
            return true;
        }

        UUID requesterId = pendingRequests.get(playerId);
        Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null) {
            player.sendMessage(ChatColor.RED + "Le joueur demandeur n'est plus en ligne.");
            pendingRequests.remove(playerId);
            return true;
        }

        // Effectuer la téléportation du joueur demandeur vers l'accepteur
        requester.teleport(player);
        player.sendMessage(ChatColor.GREEN + "§7[§e?§7]§a Vous avez accepté la demande de téléportation de " + requester.getName() + ".");
        requester.sendMessage(ChatColor.GREEN + "§7[§e?§7]§a Vous avez été téléporté à " + player.getName() + ".");
        pendingRequests.remove(playerId);
        return true;
    }

    private boolean handleTpno(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "§7[§e?§7]§c Seules les joueurs peuvent refuser une demande de téléportation.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        if (!pendingRequests.containsKey(playerId)) {
            player.sendMessage(ChatColor.RED + "§7[§e?§7]§e Aucune demande de téléportation en attente.");
            return true;
        }

        UUID requesterId = pendingRequests.get(playerId);
        Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null) {
            player.sendMessage(ChatColor.RED + "§7[§e?§7]§e Le joueur demandeur n'est plus en ligne.");
            pendingRequests.remove(playerId);
            return true;
        }

        player.sendMessage(ChatColor.RED + "§7[§e?§7]§e Vous avez refusé la demande de téléportation de " + requester.getName() + ".");
        requester.sendMessage(ChatColor.RED + player.getName() + "§7[§e?§7]§e a refusé votre demande de téléportation.");
        pendingRequests.remove(playerId);
        return true;
    }

    // Vérification de la réputation du joueur
    private boolean hasValidReputation(Player player) {
        int playerRep = WizardStoneCraft.reputation.getOrDefault(player.getUniqueId(), 0);
        if (playerRep <= -50) {
            player.sendMessage(ChatColor.RED + "§7[§e?§7]§e Votre réputation est trop faible pour utiliser cette commande.");
            return false;
        }
        return true;
    }

    // Méthode pour envoyer un message interactif
    private void sendInteractiveMessage(Player target, String message, String command, ChatColor color) {
        target.spigot().sendMessage(
                new ComponentBuilder(color + "[" + message + "] Cliquez pour " + message.toLowerCase() + ".")
                        .color(net.md_5.bungee.api.ChatColor.GREEN)  // Définit la couleur du texte
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))  // Commande qui sera exécutée au clic
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.YELLOW + "Cliquez pour " + message.toLowerCase() + ".").create())) // Texte de survol
                        .create()
        );
    }
}
