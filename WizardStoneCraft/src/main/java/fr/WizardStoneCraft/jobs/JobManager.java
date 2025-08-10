package fr.WizardStoneCraft.jobs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class JobManager {

    private final JavaPlugin plugin;
    private final File jobsFile;
    private final FileConfiguration jobsConfig;

    public JobManager(JavaPlugin plugin) {
        this.plugin = plugin;

        // Création / chargement du fichier jobs.yml
        jobsFile = new File(plugin.getDataFolder(), "jobs.yml");
        if (!jobsFile.exists()) {
            try {
                jobsFile.getParentFile().mkdirs();
                jobsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        jobsConfig = YamlConfiguration.loadConfiguration(jobsFile);
    }


    public void setJob(Player player, JobType job) {
        jobsConfig.set(player.getUniqueId().toString(), job.name()); // job.name() retourne le nom en majuscules par défaut
        save();
    }

    /**
     * Récupère le métier d'un joueur
     * @param playerUUID UUID du joueur
     * @return le nom du métier, ou "NONE" si aucun métier défini
     */
    public String getJob(UUID playerUUID) {
        return jobsConfig.getString(playerUUID.toString(), "NONE");
    }

    /**
     * Sauvegarde le fichier jobs.yml
     */
    public void save() {
        try {
            jobsConfig.save(jobsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recharge la configuration (utile si tu modifies manuellement le fichier)
     */
    public void reload() {
        try {
            jobsConfig.load(jobsFile);
        } catch (IOException | org.bukkit.configuration.InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

}
