/*package fr.WizardStoneCraft.data.save;

import fr.WizardStoneCraft.API.Base_de_donné.MySQL;
import fr.WizardStoneCraft.Manager.VenteManager;
import fr.WizardStoneCraft.data.VenteData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.*;

public class VenteStorage {

    public static Connection con;

    public VenteStorage(Connection con) {
        this.con = con;
    }



    public static void saveVentes(String nomArticle, int prix) {
        if (MySQL.isConnected()) {
            try {
                Connection con = MySQL.getConnection(); // Récupérer la connexion
                String query = "INSERT INTO ventes (nom_article, prix) VALUES (?, ?)";
                try (PreparedStatement ps = con.prepareStatement(query)) {
                    ps.setString(1, nomArticle);
                    ps.setInt(2, prix);
                    ps.executeUpdate();
                    Bukkit.getConsoleSender().sendMessage("[WizardSQL] ✅ Vente enregistrée.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Bukkit.getConsoleSender().sendMessage("[WizardSQL] ❌ Erreur lors de l'insertion dans la base de données.");
            }
        } else {
            Bukkit.getConsoleSender().sendMessage("[WizardSQL] ❌ Pas de connexion à la base de données.");
        }
    }


    // Méthode pour charger toutes les ventes d'un propriétaire spécifique
    public List<VenteData> loadVentesByProprietaire(UUID proprietaire) {
        List<VenteData> ventes = new ArrayList<>();
        String selectQuery = "SELECT * FROM Ventes WHERE proprietaire = ?";

        try (PreparedStatement stmt = con.prepareStatement(selectQuery)) {
            stmt.setString(1, proprietaire.toString());

            // Utilisation de try-with-resources pour garantir la fermeture du ResultSet
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID venteId = UUID.fromString(rs.getString("venteId"));
                    String itemType = rs.getString("itemType");
                    int itemAmount = rs.getInt("itemAmount");
                    double prix = rs.getDouble("prix");
                    long dateCreation = rs.getLong("dateCreation");
                    long dateExpiration = rs.getLong("dateExpiration");

                    // Créer l'item avec le type et la quantité
                    ItemStack item = new ItemStack(Material.getMaterial(itemType), itemAmount);

                    // Créer un objet VenteData avec toutes les informations
                    VenteData vente = new VenteData(venteId, proprietaire, item, prix, dateCreation, dateExpiration);
                    ventes.add(vente);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ventes;
    }



    // Autres méthodes pour la gestion des ventes (par exemple, saveVente, loadVente, etc.)
}*/
