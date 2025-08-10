package fr.WizardStoneCraft.Commands.Claim;

import fr.WizardStoneCraft.WizardStoneCraft;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimTransferPlugin implements CommandExecutor {

    private final Map<UUID, ClaimOffer> pendingOffers = new HashMap<>();
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§7[§e?§7] Seul un joueur peut utiliser cette commande.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "§7[§e?§7] Usage: /claimgive <joueur> <montant>|accept|deny");
            return true;
        }

        if (args[0].equalsIgnoreCase("accept")) return handleAccept(player);
        if (args[0].equalsIgnoreCase("deny")) return handleDeny(player);

        // /claimgive <joueur> <montant>
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "§7[§e?§7] Usage: /claimgive <joueur> <montant>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || target == player) {
            player.sendMessage(ChatColor.RED + "§7[§c!§7] Joueur invalide ou vous-même.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Montant invalide.");
            return true;
        }

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), true, null);
        if (claim == null || !claim.ownerID.equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "§7[§e?§7] Vous devez être dans un de vos claims pour le transférer.");
            return true;
        }

        // Supprimer toute offre existante pour ce joueur
        pendingOffers.remove(target.getUniqueId());

        // Créer une nouvelle offre
        ClaimOffer offer = new ClaimOffer(player.getUniqueId(), claim, amount);
        pendingOffers.put(target.getUniqueId(), offer);

        player.sendMessage(ChatColor.GREEN + "§7[§e?§7] Offre envoyée à " + target.getName() + " pour " + amount + "€.");
        target.sendMessage(ChatColor.GOLD + player.getName() + " vous propose un claim pour " + amount + "€.");
        target.sendMessage(ChatColor.GRAY + "§7[§c!§7] Utilisez /claimgive accept pour accepter ou /claimgive deny pour refuser. (60s)");

        // Planifier expiration
        new BukkitRunnable() {
            @Override
            public void run() {
                if (pendingOffers.containsKey(target.getUniqueId())) {
                    pendingOffers.remove(target.getUniqueId());
                    target.sendMessage(ChatColor.RED + "§7[§e?§7] L'offre de claim a expiré.");
                    player.sendMessage(ChatColor.RED + "§7[§e?§7] L'offre à " + target.getName() + " a expiré.");
                }
            }
        }.runTaskLater(WizardStoneCraft.getInstance(), 20L * 60); // 60s

        return true;
    }

    private boolean handleAccept(Player player) {
        ClaimOffer offer = pendingOffers.remove(player.getUniqueId());
        if (offer == null) {
            player.sendMessage(ChatColor.RED + "§7[§c!§7] Vous n'avez aucune offre en attente.");
            return true;
        }

        Player sender = Bukkit.getPlayer(offer.sender);
        if (sender == null) {
            player.sendMessage(ChatColor.RED + "§7[§e!§7] Le joueur à l’origine de l’offre n’est plus connecté.");
            return true;
        }

        if (!WizardStoneCraft.getInstance().economy.has(player, offer.price)) {
            player.sendMessage(ChatColor.RED + "§7[§e?§7] Vous n'avez pas assez d'argent. Prix : " + offer.price + "€.");
            return true;
        }

        // Paiement
        WizardStoneCraft.getInstance().economy.withdrawPlayer(player, offer.price);
        WizardStoneCraft.getInstance().economy.depositPlayer(sender, offer.price);

        // Transfert de claim
        offer.claim.ownerID = player.getUniqueId();
        GriefPrevention.instance.dataStore.saveClaim(offer.claim);

        player.sendMessage(ChatColor.GREEN + "§7[§e?§7] Vous avez accepté l'offre. Le claim vous appartient désormais.");
        sender.sendMessage(ChatColor.GREEN + player.getName() + "§7[§e?§7] a accepté l'offre. Vous avez reçu " + offer.price + "€.");

        return true;
    }

    private boolean handleDeny(Player player) {
        ClaimOffer offer = pendingOffers.remove(player.getUniqueId());
        if (offer == null) {
            player.sendMessage(ChatColor.RED + "§7[§c!§7] Vous n'avez aucune offre en attente.");
            return true;
        }

        Player sender = Bukkit.getPlayer(offer.sender);
        if (sender != null) {
            sender.sendMessage(ChatColor.YELLOW + player.getName() + "§7[§c!§7] a refusé votre offre de claim.");
        }

        player.sendMessage(ChatColor.GRAY + "§7[§c!§7] Vous avez refusé l'offre.");

        return true;
    }

    private static class ClaimOffer {
        UUID sender;
        Claim claim;
        double price;

        ClaimOffer(UUID sender, Claim claim, double price) {
            this.sender = sender;
            this.claim = claim;
            this.price = price;
        }
    }
}

