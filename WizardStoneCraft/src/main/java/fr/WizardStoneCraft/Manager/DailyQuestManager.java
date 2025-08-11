package fr.WizardStoneCraft.Manager;

import fr.WizardStoneCraft.WizardStoneCraft;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.QuestsAPI;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class DailyQuestManager {

    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final File file;

    private final List<Quest> dailyQuestPool = new ArrayList<>();
    private final Random random = new Random();

    public DailyQuestManager(JavaPlugin plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "dailyquests.yml");
        config = YamlConfiguration.loadConfiguration(file);

        loadFixedDailyQuests();
    }

    private void loadFixedDailyQuests() {
        dailyQuestPool.clear();

        // Liste fixe d'IDs BeautyQuest entre 20 et 100
        List<Integer> questIDs = Arrays.asList(21, 23, 26, 30, 35, 40, 52, 60, 70, 85);

        for (int id : questIDs) {
            Quest quest = QuestsAPI.getAPI().getQuestsManager().getQuest(id);
            if (quest != null) {
                dailyQuestPool.add(quest);
            } else {
                plugin.getLogger().warning("La quête ID " + id + " est introuvable dans BeautyQuests !");
            }
        }

        plugin.getLogger().info("Chargé " + dailyQuestPool.size() + " quêtes répétables pour la liste journalière.");
    }

    public Quest getQuestById(int id) {
        for (Quest q : dailyQuestPool) {
            if (q.getId() == id) return q;
        }
        return null;
    }

    public Quest getDailyQuestForPlayer(String playerName) {
        String today = LocalDate.now().toString();

        if (config.contains(playerName + ".date") && config.getString(playerName + ".date").equals(today)) {
            int questId = config.getInt(playerName + ".quest");
            return getQuestById(questId);
        }

        if (dailyQuestPool.isEmpty()) return null;

        Quest selectedQuest = dailyQuestPool.get(random.nextInt(dailyQuestPool.size()));

        config.set(playerName + ".date", today);
        config.set(playerName + ".quest", selectedQuest.getId());
        config.set(playerName + ".completed", false);
        save();

        return selectedQuest;
    }

    public void completeQuest(Player player) {
        String playerName = player.getName();
        if (!config.contains(playerName + ".date") || config.getBoolean(playerName + ".completed")) return;

        int questId = config.getInt(playerName + ".quest");
        Quest quest = getQuestById(questId);
        if (quest == null) return;

        config.set(playerName + ".completed", true);
        save();

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage("§7[§e?§7] §aQuête quotidienne complétée !");
            player.sendMessage("§e+ 5 points de réputation");
            player.sendMessage("§e+ 10 niveaux d'XP");
            player.sendMessage("§e+ 1500 blocs de claim");

            int moneyReward;
            if (questId >= 20 && questId <= 50) {
                moneyReward = 20 + random.nextInt(31); // 20 à 50
            } else {
                moneyReward = 50 + random.nextInt(51); // 50 à 100
            }
            player.sendMessage("§e+ " + moneyReward + " $");

            // Ajout des récompenses selon ton système (exemple)
            if (plugin instanceof WizardStoneCraft wsc) {
                wsc.getReputationManager().addReputation(player, 5);
            }

            player.giveExpLevels(10);

            GriefPrevention gp = GriefPrevention.instance;
            if (gp != null) {
                PlayerData pd = gp.dataStore.getPlayerData(player.getUniqueId());
                if (pd != null) {
                    pd.setAccruedClaimBlocks(pd.getAccruedClaimBlocks() + 1500);
                    player.sendMessage("§aTu as reçu 1500 blocs de claim !");
                }
            }

            var reg = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            if (reg != null) {
                Economy eco = reg.getProvider();
                if (eco.hasAccount(player)) {
                    eco.depositPlayer(player, moneyReward);
                }
            }
        });
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
