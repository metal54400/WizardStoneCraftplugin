package fr.WizardStoneCraft.Manager;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import fr.WizardStoneCraft.WizardStoneCraft;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReputationManager {

    private final WizardStoneCraft plugin;
    private final FileConfiguration config;
    private final Map<UUID, Integer> reputationCache;

    public ReputationManager(WizardStoneCraft plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.reputationCache = new HashMap<>();
    }

    public int getReputation(UUID playerId) {
        if (reputationCache.containsKey(playerId)) {
            return reputationCache.get(playerId);
        }
        return loadPlayerReputation(playerId);
    }

    public int addReputation(Player player, int amount) {
        UUID playerId = player.getUniqueId();
        int currentRep = getReputation(playerId);
        int minRep = config.getInt("minimum-reputation", -120);
        int maxRep = config.getInt("maximum-reputation", 120);

        int newRep = currentRep + amount;
        newRep = Math.max(minRep, Math.min(maxRep, newRep));

        savePlayerReputation(playerId, newRep);
        reputationCache.put(playerId, newRep);
        return newRep;
    }

    public int removeReputation(Player player, int amount) {
        return addReputation(player, -amount);
    }

    public void savePlayerReputation(UUID playerId, int rep) {
        File repFolder = new File(plugin.getDataFolder(), "rep");
        if (!repFolder.exists()) repFolder.mkdirs();

        File playerFile = new File(repFolder, playerId + ".dat");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(playerFile))) {
            oos.writeObject(rep);
            reputationCache.put(playerId, rep);
            plugin.getLogger().info("✅ Réputation sauvegardée : " + playerId + " = " + rep);
        } catch (IOException e) {
            plugin.getLogger().severe("❌ Échec sauvegarde réputation pour : " + playerId);
            e.printStackTrace();
        }
    }

    public int loadPlayerReputation(UUID playerId) {
        File playerFile = new File(plugin.getDataFolder(), "rep/" + playerId + ".dat");
        if (!playerFile.exists()) return 0;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(playerFile))) {
            int rep = (int) ois.readObject();
            reputationCache.put(playerId, rep);
            return rep;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean hasReputationFile(UUID playerId) {
        File file = new File(plugin.getDataFolder(), "rep/" + playerId + ".dat");
        return file.exists();
    }

    // --- Méthodes pour récupérer les préfixes et status ---

    public String getReputationPrefix(int reputation) {
        if (reputation >= 100)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.honorable"));

        if (reputation >= 50)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.good"));

        if (reputation >= 10)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.correct"));

        if (reputation >= 0)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.neutral"));

        if (reputation >= -9)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.neutral"));

        if (reputation >= -49)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.dangerous"));

        if (reputation >= -99)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.bad"));

        return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.horrible"));
    }

    public String getColoredReputationPrefix(int reputation) {
        if (reputation >= 100)
            return config.getString("reputation-status.honorable");

        if (reputation >= 50)
            return config.getString("reputation-status.good");

        if (reputation >= 10)
            return config.getString("reputation-status.correct");

        if (reputation >= 0)
            return config.getString("reputation-status.neutral");

        if (reputation >= -9)
            return config.getString("reputation-status.neutral");

        if (reputation >= -49)
            return config.getString("reputation-status.dangerous");

        if (reputation >= -99)
            return config.getString("reputation-status.bad");

        return config.getString("reputation-status.horrible");
    }

    public String getReputationStatus(int reputation) {
        // Je te conseille de vérifier que tu veux bien renvoyer ici les mêmes clés que getReputationPrefix
        if (reputation >= 100)
            return config.getString("reputation-prefix.honorable");
        if (reputation >= 50)
            return config.getString("reputation-prefix.good");
        if (reputation >= 10)
            return config.getString("reputation-prefix.correct");
        if (reputation >= 0)
            return config.getString("reputation-prefix.neutral");
        if (reputation >= -9)
            return config.getString("reputation-prefix.neutral");
        if (reputation >= -49)
            return config.getString("reputation-prefix.dangerous");
        if (reputation >= -99)
            return config.getString("reputation-prefix.bad");

        return config.getString("reputation-prefix.horrible");
    }

    public String getReputationPrefixe(int reputation) {
        if (reputation >= 100)
            return config.getString("reputation-prefixe.honorable");

        if (reputation >= 50)
            return config.getString("reputation-prefixe.good");

        if (reputation >= 10)
            return config.getString("reputation-prefixe.correct");

        if (reputation >= 0)
            return config.getString("reputation-prefixe.neutral");

        if (reputation >= -9)
            return config.getString("reputation-prefixe.neutral");

        if (reputation >= -49)
            return config.getString("reputation-prefixe.dangerous");

        if (reputation >= -99)
            return config.getString("reputation-prefixe.bad");

        return config.getString("reputation-prefixe.horrible");
    }
}
