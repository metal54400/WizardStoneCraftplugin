package fr.DeepStone.WizardStoneCraft;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DataManager {

    private final Deepstone plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    public DataManager(Deepstone plugin) {
        this.plugin = plugin;
        createDataFile();
    }

    private void createDataFile() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
                plugin.getLogger().info("Fichier data.yml créé !");
            } catch (IOException e) {
                plugin.getLogger().severe("Impossible de créer data.yml !");
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public boolean hasReceivedElytra(UUID uuid) {
        return dataConfig.getBoolean(uuid.toString(), false);
    }

    public void setReceivedElytra(UUID uuid) {
        dataConfig.set(uuid.toString(), true);
        saveDataFile();
    }

    public void saveDataFile() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erreur lors de la sauvegarde de data.yml");
            e.printStackTrace();
        }
    }

    public FileConfiguration getDataConfig() {
        return dataConfig;
    }
}

