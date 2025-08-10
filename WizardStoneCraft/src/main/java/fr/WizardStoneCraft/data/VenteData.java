/*package fr.WizardStoneCraft.data;

import java.io.Serializable;
import java.util.*;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VenteData implements Serializable {
    private static final long serialVersionUID = 1L;

    // La carte statique pour gérer les confirmations des joueurs
    public static Map<UUID, VenteData> confirmationMap = new HashMap<>();

    private UUID venteId;
    private UUID proprietaire;
    private ItemStack item;
    private double prix;
    private long dateCreation;
    private long dateExpiration;

    // Constructeur de VenteData
    public VenteData(UUID venteId, UUID proprietaire, ItemStack item, double prix, long dateCreation, long dateExpiration) {
        this.venteId = venteId;
        this.proprietaire = proprietaire;
        this.item = item;
        this.prix = prix;
        this.dateCreation = dateCreation;
        this.dateExpiration = dateExpiration;
    }

    public VenteData() {

    }

    // Méthodes d'accès
    public UUID getVenteId() {
        return venteId;
    }

    public UUID getProprietaire() {
        return proprietaire;
    }

    public ItemStack getItem() {
        return item;
    }

    public double getPrix() {
        return prix;
    }

    public long getDateCreation() {
        return dateCreation;
    }

    public long getDateExpiration() {
        return dateExpiration;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > dateExpiration;
    }

    // Ajouter une vente à la confirmationMap
    public static boolean ajouterConfirmation(UUID joueurId, VenteData vente) {
        if (confirmationMap.containsKey(joueurId)) {
            return false; // Le joueur a déjà une vente en attente de confirmation
        }
        confirmationMap.put(joueurId, vente);
        return true;
    }


    // Retirer une vente de la confirmationMap après confirmation ou annulation
    public static void retirerConfirmation(UUID joueurId) {
        confirmationMap.remove(joueurId);
    }

    // Vérifier si une vente est confirmée pour un joueur spécifique
    public static boolean estConfirme(UUID joueurId) {
        return confirmationMap.containsKey(joueurId);
    }

    // Obtenir la vente liée à un joueur
    public static VenteData getVenteParJoueur(UUID joueurId) {
        return confirmationMap.get(joueurId);
    }
    public static void annulerVente(UUID joueurId) {
        VenteData vente = confirmationMap.remove(joueurId);
        if (vente != null) {
            // Vous pouvez ajouter des actions supplémentaires, comme notifier le joueur que la vente a été annulée
            System.out.println("Vente annulée pour le joueur " + joueurId);
        }
    }

    public void acheterVente(UUID joueurId, VenteData vente) {
        Player joueur = Bukkit.getPlayer(joueurId);
        if (joueur != null) {
            // Vérifier si la vente est expirée
            if (vente.isExpired()) {
                joueur.sendMessage("§cCette vente a expiré.");
                return;
            }

            // Vérifier si le joueur a assez d'argent
            double prix = vente.getPrix();
            if (WizardStoneCraft.getEconomy().getBalance(joueur) >= prix) {
                // Débiter l'argent et donner l'item
                WizardStoneCraft.getEconomy().withdrawPlayer(joueur,prix) ;
                joueur.getInventory().addItem(vente.getItem());

                // Retirer la vente de la map de confirmation
                VenteData.retirerConfirmation(joueurId);

                // Informer le joueur de l'achat réussi
                joueur.sendMessage("§aAchat réussi ! Vous avez acheté " + vente.getItem().getType());
            } else {
                joueur.sendMessage("§cVous n'avez pas assez d'argent.");
            }
        }
    }




}*/
