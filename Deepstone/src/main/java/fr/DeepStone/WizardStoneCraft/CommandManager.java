package fr.DeepStone.WizardStoneCraft;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager {

    private final JavaPlugin plugin;
    private final String commandesUrl;
    private final String clearUrl;
    private final File logFile;

    // Ajout d'un flag pour activer/désactiver le traitement
    private volatile boolean enabled = true;

    public CommandManager(JavaPlugin plugin, String commandesUrl, String clearUrl) {
        this.plugin = plugin;
        this.commandesUrl = commandesUrl;
        this.clearUrl = clearUrl;
        this.logFile = new File(plugin.getDataFolder(), "commands.log");
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getLogger().info("Traitement des commandes " + (enabled ? "activé" : "désactivé") + ".");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void traiterCommandesDepuisURL() {
        if (!enabled) {
            plugin.getLogger().info("Traitement des commandes désactivé, aucune action effectuée.");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                List<String> commandes = fetchCommandesDepuisURL();
                if (commandes.isEmpty()) return;

                for (int i = 0; i < commandes.size(); i++) {
                    final String commande = commandes.get(i).trim();
                    if (commande.isEmpty() || commandeDejaTraitee(commande)) {
                        plugin.getLogger().info("Commande ignorée : " + commande);
                        continue;
                    }

                    long delay = i * 10L; // 0.5 sec entre chaque (10 ticks)
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commande);
                        logCommande(commande);
                    }, delay);
                }

                plugin.getLogger().info("Commandes exécutées avec délai.");
                viderFichierCommandes();

            } catch (Exception e) {
                plugin.getLogger().severe("Erreur lors du traitement des commandes : " + e.getMessage());
            }
        });
    }

    private List<String> fetchCommandesDepuisURL() throws IOException {
        URL url = new URL(commandesUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int status = connection.getResponseCode();
        if (status != 200) {
            plugin.getLogger().warning("Erreur HTTP (" + status + ") lors de la récupération des commandes.");
            connection.disconnect();
            return List.of();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
        } finally {
            connection.disconnect();
        }
    }

    private void viderFichierCommandes() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(clearUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int status = connection.getResponseCode();
                if (status == 200) {
                    plugin.getLogger().info("Fichier distant vidé.");
                } else {
                    plugin.getLogger().warning("Échec du vidage distant (HTTP " + status + ").");
                }
                connection.disconnect();
            } catch (Exception e) {
                plugin.getLogger().severe("Erreur vidage commandes : " + e.getMessage());
            }
        });
    }

    private void logCommande(String commande) {
        try {
            if (!logFile.exists()) {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            }
            String logLine = "[" + LocalDateTime.now() + "] " + commande + System.lineSeparator();
            Files.writeString(logFile.toPath(), logLine, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            plugin.getLogger().warning("Erreur écriture log : " + e.getMessage());
        }
    }

    private boolean commandeDejaTraitee(String commande) {
        if (!logFile.exists()) return false;

        try {
            return Files.lines(logFile.toPath(), StandardCharsets.UTF_8)
                    .anyMatch(line -> line.contains(commande));
        } catch (IOException e) {
            plugin.getLogger().warning("Erreur lecture log : " + e.getMessage());
            return false;
        }
    }
}
