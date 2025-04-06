package fr.WizardStoneCraft.Manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GemManager {

    private final FileConfiguration gemsConfig;
    private final File gemsFile;

    public GemManager(JavaPlugin plugin) {
        gemsFile = new File(plugin.getDataFolder(), "gems.yml");
        if (!gemsFile.exists()) {
            plugin.saveResource("gems.yml", false);
        }
        gemsConfig = YamlConfiguration.loadConfiguration(gemsFile);
    }

    public int getGems(String playerName) {
        return gemsConfig.getInt(playerName, 0);
    }

    public void setGems(String playerName, int amount) {
        gemsConfig.set(playerName, amount);
        save();
    }

    public void addGems(String playerName, int amount) {
        setGems(playerName, getGems(playerName) + amount);
    }

    public Map<String, Integer> getTopGems() {
        Map<String, Integer> map = new HashMap<>();
        for (String key : gemsConfig.getKeys(false)) {
            map.put(key, gemsConfig.getInt(key));
        }
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    public void save() {
        try {
            gemsConfig.save(gemsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

