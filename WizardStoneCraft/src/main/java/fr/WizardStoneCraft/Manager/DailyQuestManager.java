package fr.WizardStoneCraft.Manager;

import fr.WizardStoneCraft.WizardStoneCraft;
import fr.WizardStoneCraft.data.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class DailyQuestManager implements CommandExecutor {

    private final File file;
    private final FileConfiguration config;
    private final Map<String, Quest> quests = new HashMap<>();
    private final JavaPlugin plugin;

    public DailyQuestManager(JavaPlugin plugin) {
        this.plugin = plugin;

        file = new File(plugin.getDataFolder(), "dailyquests.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);

        loadQuests();
    }

    private void loadQuests() {
        quests.put("recolte_cultures", new Quest("Récolte pour le village", "Récolte %amount% cultures variées.", QuestType.FARMING, 64));
        quests.put("plante_arbres", new Quest("Forêt renaissante", "Plante %amount% arbres près du spawn.", QuestType.PLANTING, 10));
        quests.put("miner_ressources", new Quest("Commande express du forgeron", "Mine 64 fer, 32 or, 16 diamants.", QuestType.MINING_COMPLEX, 1));
        quests.put("preparer_patissier", new Quest("Pâtisseries pour le banquet", "Prépare %amount% pâtisseries pour le cuisinier.", QuestType.COOKING, 5));
        quests.put("deguster_biomes", new Quest("Goûteur de légende", "Mange un aliment de chaque biome.", QuestType.EATING_BIOMES, 1));
        quests.put("tuer_trinite", new Quest("Trinité du chaos", "Tue Warden, Enderdragon et Wither sans mourir.", QuestType.BOSS_TRIO, 1));
        quests.put("tuer_wither_specifique", new Quest("Exécution rituelle", "Tue un Wither avec une épée en or et armure en cuir.", QuestType.SPECIFIC_BOSS, 1));
        quests.put("duel_kits", new Quest("Duel d’élite", "Gagne %amount% duels avec un kit donné.", QuestType.PVP_DUEL, 3));
        quests.put("zombie_villager", new Quest("Purification", "Tue un zombie villageois.", QuestType.VILLAGERZOMBIE, 1));
        quests.put("vaincre_breeze", new Quest("Souffle apaisé", "Tue un Breeze sans prendre de dégâts.", QuestType.BREZZE, 1));
    }

    public Quest getDailyQuest(String playerName) {
        String today = LocalDate.now().toString();
        if (config.contains(playerName + ".date") && config.getString(playerName + ".date").equals(today)) {
            String questKey = config.getString(playerName + ".quest");
            return quests.getOrDefault(questKey, null);
        } else {
            List<String> keys = new ArrayList<>(quests.keySet());
            Collections.shuffle(keys);
            String selectedQuest = keys.get(0);
            config.set(playerName + ".date", today);
            config.set(playerName + ".quest", selectedQuest);
            config.set(playerName + ".completed", false);
            save();
            return quests.get(selectedQuest);
        }
    }

    public void setQuestForPlayer(String playerName, String questKey) {
        if (quests.containsKey(questKey)) {
            config.set(playerName + ".date", LocalDate.now().toString());
            config.set(playerName + ".quest", questKey);
            config.set(playerName + ".completed", false);
            save();
        }
    }

    public void completeQuest(Player player) {
        String playerName = player.getName();
        String today = LocalDate.now().toString();

        if (!today.equals(config.getString(playerName + ".date"))) return;
        if (config.getBoolean(playerName + ".completed")) return;

        config.set(playerName + ".completed", true);
        save();

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage("§7[§e?§7] §aQuête quotidienne complétée ! Tu gagnes §e5 gems §aet §e1 point de réputation.");
            // GemManager and reputation update (assuming they exist and are public)
            if (plugin instanceof WizardStoneCraft wsc) {
                wsc.gemManager.addGems(playerName, 5);
            }
        });
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Quest {
        private final String name;
        private final String description;
        private final QuestType type;
        private final int amount;
        private final Set<EntityType> targetEntities;
        private final Set<Material> targetMaterials;

        public Quest(String name, String description, QuestType type, int amount) {
            this(name, description, type, amount, Set.of(), Set.of());
        }

        public Quest(String name, String description, QuestType type, int amount,
                     Set<EntityType> targetEntities, Set<Material> targetMaterials) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.amount = amount;
            this.targetEntities = targetEntities;
            this.targetMaterials = targetMaterials;
        }

        public String getName() { return name; }

        public String getDescription() { return description.replace("%amount%", String.valueOf(amount)); }

        public QuestType getType() { return type; }

        public int getAmount() { return amount; }

        public boolean isTarget(EntityType entityType) {
            return targetEntities.isEmpty() || targetEntities.contains(entityType);
        }

        public boolean isTarget(Material material) {
            return targetMaterials.isEmpty() || targetMaterials.contains(material);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§7[§e?§7] §cSeuls les joueurs peuvent exécuter cette commande.");
            return true;
        }

        if (label.equalsIgnoreCase("dailyquest")) {
            Quest quest = getDailyQuest(player.getName());
            if (quest == null) {
                player.sendMessage("§7[§e?§7] §cAucune quête disponible.");
                return true;
            }

            boolean completed = config.getBoolean(player.getName() + ".completed");
            if (completed) {
                player.sendMessage("§7[§e?§7] §aTu as déjà complété ta quête quotidienne aujourd'hui !");
            } else {
                player.sendMessage("§7[§e?§7] §6[Quête Quotidienne] §e" + quest.getName());
                player.sendMessage("§7" + quest.getDescription());
            }
            return true;

        } else if (label.equalsIgnoreCase("dailyquestfinish")) {
            if (!player.hasPermission("WizardStoneCraft.admin")) {
                player.sendMessage("§cTu n'as pas la permission d'exécuter cette commande.");
                return true;
            }
            completeQuest(player);
            player.sendMessage("§aTu as forcé la fin de la quête quotidienne (mode admin).");
            return true;

        } else if (label.equalsIgnoreCase("dailyquestselection")) {
            if (!player.hasPermission("WizardStoneCraft.admin")) {
                player.sendMessage("§cTu n'as pas la permission d'exécuter cette commande.");
                return true;
            }
            if (args.length != 1 || !quests.containsKey(args[0])) {
                player.sendMessage("§cUtilisation: /dailyquestselection <clé_de_quête>");
                player.sendMessage("§7Quêtes disponibles: " + String.join(", ", quests.keySet()));
                return true;
            }
            setQuestForPlayer(player.getName(), args[0]);
            player.sendMessage("§aTu as défini la quête quotidienne sur: §e" + args[0]);
            return true;
        }

        return false;
    }
}
