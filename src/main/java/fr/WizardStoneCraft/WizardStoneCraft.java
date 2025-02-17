package fr.WizardStoneCraft;



import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.database.BetterTeamsDatabase;
import com.booksaw.betterTeams.integrations.placeholder.TeamPlaceholders;
import fr.WizardStoneCraft.Commands.*;
import fr.WizardStoneCraft.PlaceHolderApi.PlaceHolderApi;

import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


import java.io.*;
import java.time.Instant;
import java.util.*;


import static org.bukkit.Bukkit.getOfflinePlayerIfCached;
import static org.bukkit.Bukkit.getPlayer;


public class WizardStoneCraft extends JavaPlugin implements TabExecutor,Listener {

    private File jobsFile;
    private FileConfiguration jobsConfig;
    private final Set<UUID> refundedPlayers = new HashSet<>(); // Liste des joueurs remboursés
    private File bannedPlayersFile;
    private double dropChance;
    private FileConfiguration bannedPlayersConfig;
    private Map<UUID, Long> bannedPlayers;
    public static final Map<UUID, Integer> reputation = new HashMap<>();
    private final Map<UUID, Map<UUID, Long>> killHistory = new HashMap<>();
    private final Map<Player, Integer> playerReputations = new HashMap<>(); // Stocke la réputation des joueurs
    private final Map<Player, Player> selectedPlayers = new HashMap<>();
    List<String> lore = new ArrayList<>(); // Crée une liste vide
    public int MIN_REP;
    public  int MAX_REP;
    private int pointsKill;
    private int pointsJoin;
    private FileConfiguration messages;
    private String tabPrefix;
    private String chatPrefix;
    public FileConfiguration config;
    private LuckPerms luckPerms;
    private static Economy econ = null;








    @Override
    public void onLoad() {
        getLogger().info("§7[§e?§7]§a ReputationPlugin chargé !");
    }

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialisation des API
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getLogger().info("§7[§e?§7]§a LuckPerms API détectée et connectée !");
        } else {
            getLogger().warning("§7[§e?§7]§c LuckPerms API non détectée !");
        }





        getLogger().info("§7[§e?§7]§a ReputationPlugin activé !");
        saveDefaultConfig();
        loadConfiguration();
        loadMessages();
        loadMessages();
        loadConfig();
        loadMessagese();
        config = getConfig();
        dropChance = config.getDouble("drop-chance", 50.0) / 100.0;
        loadBannedPlayersData();
        PlaceHolderApi.checkPlaceholderAPI();
        WizardStoneCraft plugin = this;
        // register des commande

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("repadd").setExecutor(new ManageRepCommand());
        getCommand("reptop").setExecutor(new ReptopCommand());
        getCommand("rep").setExecutor(new ReputationCommand());
        getCommand("rephighlight").setExecutor(new RepHighlightCommand());
        getCommand("help").setExecutor(new RepHelpCommand());
        getCommand("repreload").setExecutor(new RepReloadCommand(this));
        getCommand("Broadcast").setExecutor(new Broadcast());
        getCommand("tabreload").setExecutor(new UpdateTablistCommand(this));
        getCommand("menu").setExecutor(new RepGui());

        //tab updater
        new TablistUpdater(this).runTaskTimer(this, 10, 10);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("jobsstatue") && sender instanceof Player) {
            Player player = (Player) sender;
            sender.sendMessage("\u00a76[Jobs] \u00a7fStatut de vos métiers :");
            for (String job : new String[]{"mineur", "bucheron", "chasseur", "alchimiste", "pêcheur"}) {
                int level = getJobXp(job + ".level", 0);
                int xp = getJobXp(job + ".xp", 0);
                sender.sendMessage("\u00a76" + job + " : \u00a7fNiveau " + level + " (" + xp + " XP)");
            }
            return true;
        }
        return false;
    }


    private void loadConfig() {
        jobsFile = new File(getDataFolder(), "jobs.yml");
        if (!jobsFile.exists()) {
            saveResource("jobs.yml", false);
        }
        jobsConfig = YamlConfiguration.loadConfiguration(jobsFile);
    }

    private int getJobXp(String jobPath, int defaultValue) {
        return jobsConfig.getInt(jobPath, defaultValue);
    }

    private void addXp(Player player, String job, int xp) {
        UUID uuid = player.getUniqueId();
        File playerFile = new File(getDataFolder() + "/jobs/", uuid + "_jobs.dat");
        if (!playerFile.getParentFile().exists()) {
            playerFile.getParentFile().mkdirs();
        }
        YamlConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);

        int currentXp = playerData.getInt(job + ".xp", 0) + xp;
        int currentLevel = playerData.getInt(job + ".level", 0);
        int xpToNextLevel = (int) (100 * Math.pow(1.15, currentLevel)); // XP progressif ajusté

        while (currentXp >= xpToNextLevel && currentLevel < 200) {
            currentXp -= xpToNextLevel;
            currentLevel++;
            player.sendMessage("\u00a76[Jobs] \u00a7fFélicitations ! Vous avez atteint le niveau " + currentLevel + " en " + job + "!");
            giveReward(player, job, currentLevel);
            xpToNextLevel = (int) (100 * Math.pow(1.15, currentLevel));
        }

        playerData.set(job + ".xp", currentXp);
        playerData.set(job + ".level", currentLevel);

        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void giveReward(Player player, String job, int level) {
        List<ItemStack> rewards = new ArrayList<>();
        switch (job) {
            case "mineur":
                rewards.add(new ItemStack(Material.STONE, level));
                if (level == 10) rewards.add(new ItemStack(Material.IRON_INGOT, 5));
                if (level == 25) rewards.add(new ItemStack(Material.GOLD_INGOT, 3));
                if (level == 50) rewards.add(new ItemStack(Material.DIAMOND, 2));
                if (level == 100) rewards.add(new ItemStack(Material.NETHERITE_INGOT, 1));
                break;
            case "bucheron":
                rewards.add(new ItemStack(Material.OAK_LOG, level));
                if (level == 10) rewards.add(new ItemStack(Material.STONE_AXE, 1));
                if (level == 25) rewards.add(new ItemStack(Material.IRON_AXE, 1));
                if (level == 50) rewards.add(new ItemStack(Material.DIAMOND_AXE, 1));
                if (level == 100) rewards.add(new ItemStack(Material.NETHERITE_AXE, 1));
                break;
            case "chasseur":
                rewards.add(new ItemStack(Material.BONE, level));
                if (level == 10) rewards.add(new ItemStack(Material.BOW, 1));
                if (level == 25) rewards.add(new ItemStack(Material.ARROW, 20));
                if (level == 50) rewards.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
                if (level == 100) rewards.add(new ItemStack(Material.NETHERITE_SWORD, 1));
                break;
            case "alchimiste":
                rewards.add(new ItemStack(Material.GLASS_BOTTLE, level));
                if (level == 10) rewards.add(new ItemStack(Material.BREWING_STAND, 1));
                if (level == 25) rewards.add(new ItemStack(Material.BLAZE_POWDER, 5));
                if (level == 50) rewards.add(new ItemStack(Material.GHAST_TEAR, 3));
                if (level == 100) rewards.add(new ItemStack(Material.DRAGON_BREATH, 1));
                break;
            case "pêcheur":
                rewards.add(new ItemStack(Material.COD, level));
                if (level == 10) rewards.add(new ItemStack(Material.FISHING_ROD, 1));
                if (level == 25) rewards.add(new ItemStack(Material.LILY_PAD, 5));
                if (level == 50) rewards.add(new ItemStack(Material.HEART_OF_THE_SEA, 1));
                if (level == 100) rewards.add(new ItemStack(Material.TRIDENT, 1));
                break;
        }
        for (ItemStack reward : rewards) {
            player.getInventory().addItem(reward);
        }
        if (!rewards.isEmpty()) {
            player.sendMessage("\u00a76[Jobs] \u00a7fVous avez reçu une récompense pour votre montée en niveau !");
        }
    }




    @Override
    public void onDisable() {
        getLogger().info("§7[§e?§7]§c ReputationPlugin désactivé !");
    }

    private void loadConfiguration() {
        MIN_REP = getConfig().getInt("minimum-reputation");
        MAX_REP = getConfig().getInt("maximum-reputation");
        pointsKill = getConfig().getInt("points-kill");
        pointsJoin = getConfig().getInt("points-join");
    }

    public void loadMessages() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', messages.getString(key));

    }

    private void sendChatMessage(Player player, String message) {
        player.sendMessage(chatPrefix + " " + message);
    }


    void savePlayerReputation(UUID playerId, int rep) {
        // Définition du fichier de réputation du joueur dans le dossier 'rep/'
        File repFolder = new File(getDataFolder(), "rep");

        // Créer le dossier 'rep/' s'il n'existe pas
        if (!repFolder.exists()) {
            repFolder.mkdirs(); // Crée le dossier 'rep/' si il n'existe pas encore
        }

        File playerFile = new File(repFolder, playerId + ".dat");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(playerFile))) {
            oos.writeObject(rep);
            getLogger().info("Réputation du joueur " + playerId + " sauvegardée avec succès dans " + playerId + ".dat.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    int loadPlayerReputation(UUID playerId) {
        // Définition du dossier 'rep/' où le fichier de réputation est stocké
        File repFolder = new File(getDataFolder(), "rep");

        // Vérifier si le dossier existe
        if (!repFolder.exists()) {
            getLogger().warning("Le dossier 'rep/' est introuvable.");
            return 0; // Réputation par défaut si le dossier est introuvable
        }

        // Fichier de réputation du joueur
        File playerFile = new File(repFolder, playerId + ".dat");

        if (!playerFile.exists()) {
            getLogger().warning("Fichier " + playerId + ".dat introuvable. Initialisation de la réputation par défaut.");
            return 0; // Réputation par défaut si le fichier n'existe pas
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(playerFile))) {
            return (int) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return 0; // Réputation par défaut en cas d'erreur
        }
    }












    private void updateTabList(Player player) {
        UUID playerId = player.getUniqueId();
        int rep = reputation.getOrDefault(playerId, loadPlayerReputation(playerId));
        String prefix = getReputationStatus(rep);
        int weight = getLuckPermsWeight(playerId);
        String tabName = ChatColor.GRAY + prefix + "[" + weight + "] " + " " + ChatColor.RESET + player.getName();
        player.setPlayerListName(tabName);
    }
    private int getLuckPermsWeight(UUID playerId) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) return 0;
        return user.getCachedData().getMetaData().getSuffixes().size();
    }




    public void updateTablist(Player player) {
        // Lire le header et footer depuis le fichier de configuration
        String header = ChatColor.translateAlternateColorCodes('&', config.getString("tablist.header"));
        String footer = ChatColor.translateAlternateColorCodes('&', config.getString("tablist.footer"));

        // Mettre à jour la tablist
        player.setPlayerListHeaderFooter(header,footer);

        // Ajouter un préfixe basé sur la réputation dans le nom de la tablist
        UUID playerId = player.getUniqueId();
        int rep = reputation.getOrDefault(playerId, loadPlayerReputation(playerId));
        String reputationPrefix = getReputationStatus(rep);
        String gradePrefix = getLuckPermsPrefix(player);
        player.setPlayerListName(  reputationPrefix + " "  + gradePrefix + ChatColor.RESET + player.getName());
    }

    /**
     * Méthode pour obtenir le préfixe de réputation d'un joueur.
     *
     * @return Le préfixe coloré de la réputation.
     */
    private String getReputationPrefix() {
        // Exemple fictif. Vous pouvez remplacer par un système réel de réputation.
        int reputation = (int) (Math.random() * 200 - 100); // Réputation aléatoire

        // Lire les préfixes depuis le fichier de configuration
        if (reputation >= 100)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.honorable"));
        if (reputation >= 50)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.good"));
        if (reputation >= 0)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.neutral"));
        if (reputation >= -50)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.dangerous"));
        if (reputation >= -100)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.bad"));
        return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.horrible"));
    }

    private String getColoredReputationPrefix(int reputation) {

        if (reputation >= 100) return getConfig().getString("reputation-status.honorable");
        if (reputation >= 50) return getConfig().getString("reputation-status.good");
        if (reputation >= 0) return getConfig().getString("reputation-status.neutral");
        if (reputation >= -10) return getConfig().getString("reputation-status.dangerous");
        if (reputation >= -50) return getConfig().getString("reputation-status.bad");
        if (reputation >= -100) return getConfig().getString("reputation-prefix.horrible");
        return "";
    }


    public String getReputationStatus(int reputation) {
        if (reputation >= 100) return getConfig().getString("reputation-prefix.honorable");
        if (reputation >= 50) return getConfig().getString("reputation-prefix.good");
        if (reputation >= 0) return getConfig().getString("reputation-prefix.neutral");
        if (reputation >= -10) return getConfig().getString("reputation-prefix.dangerous");
        if (reputation >= -50) return getConfig().getString("reputation-prefix.bad");
        if (reputation >= -100) return getConfig().getString("reputation-prefix.horrible");

        return "";
    }
    public String getReputationPrefixe(int reputation) {
        if (reputation >= 100) return getConfig().getString("reputation-prefixe.honorable");
        if (reputation >= 50) return getConfig().getString("reputation-prefixe.good");
        if (reputation >= 0) return getConfig().getString("reputation-prefixe.neutral");
        if (reputation >= -10) return getConfig().getString("reputation-prefixe.dangerous");
        if (reputation >= -50) return getConfig().getString("reputation-prefixe.bad");
        if (reputation >= -100) return getConfig().getString("reputation-prefixe.horrible");
        return "";
    }
    private String getLuckPermsPrefix(Player player) {
        if (luckPerms == null) return ""; // LuckPerms non configuré

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            CachedMetaData metaData = user.getCachedData().getMetaData();
            String prefix = metaData.getPrefix();
            return prefix != null ? ChatColor.translateAlternateColorCodes('&', prefix) + " " : "";
        }
        return "";
    }


    public class ManageRepCommand implements TabExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!sender.hasPermission("reputation.manage")) {
                sender.sendMessage(getMessage("no_permission"));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(getMessage("usage").replace("%command%", label));
                return true;
            }

            String targetPlayer = args[0];
            Player target = getPlayer(targetPlayer);
            if (target == null) {
                sender.sendMessage(getMessage("player_not_found"));
                return true;
            }
            if (!(sender instanceof Player) && args[0].contains("%player%")) {
                sender.sendMessage(getMessage("console_cannot_use_placeholder"));
                return true;
            }

            // Remplacer le placeholder avec PlaceholderAPI
            if (sender instanceof Player) {
                targetPlayer = PlaceHolderApi.parse((Player) sender, targetPlayer);
            }


            int amount = Integer.parseInt(args[1]);
            UUID targetId = target.getUniqueId();
            int newReputation = label.equals("repadd")
                    ? reputation.getOrDefault(targetId, loadPlayerReputation(targetId)) + amount
                    : reputation.getOrDefault(targetId, loadPlayerReputation(targetId)) - amount;
            newReputation = Math.max(Math.min(newReputation, MAX_REP), MIN_REP);
            reputation.put(targetId, newReputation);
            sender.sendMessage(getMessage("rep_modified")
                    .replace("%player%", target.getName())
                    .replace("%amount%", String.valueOf(amount)));
            savePlayerReputation(targetId, newReputation);
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            return null; // Implémentation pour les suggestions automatiques
        }
    }

    public class ReputationCommand  implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
                return true;
            }

            if (args.length < 1) {
                sender.sendMessage(getMessage("usage").replace("%command%", label));
                return true;
            }

            String targetPlayer = args[0];

            UUID targetId;
            String targetName;

            Player target = getPlayer(targetPlayer);
            if (target == null) {
                sender.sendMessage(getMessage("player_not_found"));
                return true;
            } else {
            OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetPlayer);
            if (!offlineTarget.hasPlayedBefore()) {
                sender.sendMessage(getMessage("player_not_found"));
                return true;
            }
            targetId = offlineTarget.getUniqueId();
            targetName = offlineTarget.getName();
        }
            UUID targetid = target.getUniqueId();

            int rep = reputation.getOrDefault(targetid, loadPlayerReputation(targetid));
            String status = getReputationStatus(rep);
            sender.sendMessage(getMessage("reputation_status")
                    .replace("%player%", target.getName())
                    .replace("%rep%", getReputationPrefixe(rep))
                    .replace("%reputation%", String.valueOf(rep))
                    .replace("%liste%", "" +
                            "\n"+
                            "§dΩ Honorable§F = 100§7\n" +
                            "§aΩ Bonne§f = 50§7 \n" +
                            "§7Ω Neutre§f = 0§7\n" +
                            "§6Ω Dangereux§f = -10§7\n" +
                            "§cΩ Mauvaise§f = -50§7\n" +
                            "§4Ω Horrible§f = -100§7\n"+
                            "\n")
                    .replace("%status%", status));

            return true;
        }


    }

    public class ReptopCommand implements TabExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            sender.sendMessage(getMessage("top_reputations"));

            reputation.entrySet().stream()
                    .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        UUID playerId = entry.getKey();
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
                        int reps = entry.getValue();

                        // Obtenir le préfixe de réputation
                        String prefix = getReputationPrefixe(reps);

                        // Afficher le message avec le préfixe
                        sender.sendMessage( offlinePlayer.getName() + ": " + prefix + "§7 " + reps + " points de Réputation");
                    });

            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            return null; // Implémentation pour les suggestions automatiques
        }
    }


    public class RepHighlightCommand implements TabExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
                return true;
            }

            if (args.length < 1) {
                sender.sendMessage(getMessage("usage").replace("%command%", label));
                return true;
            }

            String targetPlayer = args[0];
            Player target = getPlayer(targetPlayer);
            if (target == null) {
                sender.sendMessage(getMessage("player_not_found"));
                return true;
            }

            UUID targetId = target.getUniqueId();
            int rep = reputation.getOrDefault(targetId, loadPlayerReputation(targetId));
            String status = getReputationStatus(rep);
            return true;
        }


        @Override
        public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
            return List.of();
        }
    }

    static class TablistUpdater extends BukkitRunnable {
        private final WizardStoneCraft plugin;

        public TablistUpdater(WizardStoneCraft plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            // Met à jour la tablist pour chaque joueur connecté
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.updateTablist(player);
            }
        }
    }
    public static class UpdateTablistCommand implements CommandExecutor {

        private final WizardStoneCraft plugin;

        public UpdateTablistCommand(WizardStoneCraft plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!sender.hasPermission("reputation.updatetablist")) {
                sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
                return true;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.updateTablist(player);
            }

            sender.sendMessage(ChatColor.GREEN + "La tablist a été mise à jour pour tous les joueurs en ligne.");
            return true;
        }
    }


    public int getReputation(Player player) {
        return reputation.getOrDefault(player.getUniqueId(), 0);
    }
    public void removeReputation(Player player, int amount) {
        UUID playerId = player.getUniqueId();
        int currentReputation = getReputation(player);

        // Update the reputation and apply the penalty
        int newReputation = Math.max(-120, currentReputation - amount);
        reputation.put(playerId, newReputation);

        // Check if the player needs to be banned
        if (newReputation == -120) {
            // Ban the player for 15 or 16 days
            int banDays = 15 + (Math.random() < 0.5 ? 1 : 0); // Randomly choose 15 or 16 days
            long banDuration = Instant.now().plusSeconds(banDays * 24 * 60 * 60).getEpochSecond();

            // Store the ban information (you should save this in a persistent storage, such as a file or database)
            saveBanData(playerId, banDuration);

            // Kick the player and inform them about the ban duration
            player.kickPlayer(messages.getString("ban_message").replace("{days}", String.valueOf(banDays)));
        }
    }

    private void saveBanData(UUID playerId, long banExpiryTime) {
        // Here you would save the ban data to a file or database
        // Example: save to a simple HashMap or a persistent file
        // For this example, we'll store it in an in-memory HashMap
        bannedPlayers.put(playerId, banExpiryTime);

        // You could save it to a file like so (example, using a YML file for persistence):
         config.set("bannedPlayers." + playerId.toString(), banExpiryTime);
         saveConfig();
    }








//event
@EventHandler
public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    if (new Random().nextDouble() < dropChance) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            skull.setItemMeta(meta);
        }
        player.getWorld().dropItemNaturally(player.getLocation(), skull);
    }
}
    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            UUID killerId = killer.getUniqueId();

            // Charger la réputation actuelle
            int pointsKill = reputation.getOrDefault(killerId, loadPlayerReputation(killerId));

            // Appliquer une perte de réputation
            int newRep = Math.max(pointsKill - 2, MIN_REP);

            // Mettre à jour la réputation
            reputation.put(killerId, newRep);
            savePlayerReputation(killerId, newRep);

            // Envoyer un message au joueur
            String message = getMessage("reputation_lost");
            if (message != null) {
                getMessage("reputation_lost");
            } else {
                getMessage("reputation_lost");
            }
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Vérifie si la réputation du joueur est inférieure ou égale à la valeur seuil
        int newRep = reputation.getOrDefault(playerId, loadPlayerReputation(playerId)) + pointsJoin;
        reputation.put(playerId, Math.min(newRep, MAX_REP));
        player.sendMessage(getMessage("reputation_gained"));
        savePlayerReputation(playerId, Math.min(newRep, MAX_REP));
        updateTabList(player);

        // Vérifie si le système de ban automatique est activé dans la configuration
        if (config.getBoolean("autoBanOnLowReputation", true) && newRep <= config.getInt("reputationThreshold", -120)) {
            long banDurationSeconds = config.getInt("banDurationDays", 30) * 24 * 60 * 60; // Durée du ban en secondes
            long banExpiryTime = Instant.now().getEpochSecond() + banDurationSeconds;

            bannedPlayers.put(playerId, banExpiryTime); // Ajoute le joueur à la liste des bannis
            saveBannedPlayersData(); // Sauvegarde des données de ban
            player.kickPlayer(getMessage("ban_message") + newRep);
            return;
        }

        // Vérifie si le joueur est banni
        if (bannedPlayers.containsKey(playerId)) {
            long banExpiryTime = bannedPlayers.get(playerId);
            long currentTime = Instant.now().getEpochSecond();

            // Si le temps actuel est inférieur au temps d'expiration du ban, le joueur est toujours banni
            if (currentTime < banExpiryTime) {
                long remainingTime = banExpiryTime - currentTime;
                long remainingDays = remainingTime / (24 * 60 * 60); // Convertir en jours

                // Informe le joueur du temps restant sur son ban
                player.kickPlayer(getMessage("ban_message") + remainingDays);
                return;
            }
        }
    }

    public void loadBannedPlayersData() {
        bannedPlayersFile = new File(getDataFolder(), "bannedPlayers.yml");
        if (!bannedPlayersFile.exists()) {
            saveResource("bannedPlayers.yml", true);
        }

        bannedPlayersConfig = YamlConfiguration.loadConfiguration(bannedPlayersFile);
        bannedPlayers = new HashMap<>();

        // Charge les données de ban dans la map
        for (String key : bannedPlayersConfig.getKeys(false)) {
            UUID playerId = UUID.fromString(key);
            long banExpiryTime = bannedPlayersConfig.getLong(key);
            bannedPlayers.put(playerId, banExpiryTime);
        }
    }

    public void saveBannedPlayersData() {
        for (Map.Entry<UUID, Long> entry : bannedPlayers.entrySet()) {
            bannedPlayersConfig.set(entry.getKey().toString(), entry.getValue());
        }

        try {
            bannedPlayersConfig.save(bannedPlayersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMessagese() {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }






    /**
     * ✅ Ajoute "Remboursé par <NomDuStaff>" sur les items placés ou déplacés en mode créatif.
     */
    @EventHandler
    public void onCreativeItemMove(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Vérifie si le joueur est en mode créatif et a la permission
        if (player.getGameMode() != GameMode.CREATIVE) return;
        if (!player.hasPermission("moderator") && !player.hasPermission("op")) return;

        // Vérifie si l'option est activée dans la config
        if (config == null || !config.getBoolean("addLoreOnCreative", true)) return;

        // Récupère l'item déplacé
        ItemStack cursorItem = event.getCursor();   // Item tenu par la souris
        ItemStack currentItem = event.getCurrentItem(); // Item dans l'inventaire

        if (cursorItem != null) applyRefundLore(player, cursorItem);
        if (currentItem != null) applyRefundLore(player, currentItem);
    }

    /**
     * ✅ Applique la mention "Remboursé par <NomDuStaff>" sur l'item en remplaçant le lore existant.
     */
    private void applyRefundLore(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        if (meta == null) return; // Impossible de modifier cet item

        String staffName = player.getName();
        String loreMessage = getMessage("loreitem") + staffName;

        // Remplace le lore existant par un seul message
        List<String> lore = new ArrayList<>();
        lore.add(loreMessage);
        meta.setLore(lore);
        item.setItemMeta(meta);

        Bukkit.getLogger().info("[ItemsLog] Un item a été remboursé par " + staffName);
    }


    public void addRefundedPlayer(Player player) {
        refundedPlayers.add(player.getUniqueId());
    }

    // Enlever un joueur de la liste des remboursés (si nécessaire)
    public void removeRefundedPlayer(Player player) {
        refundedPlayers.remove(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        int rep = reputation.getOrDefault(playerId, loadPlayerReputation(playerId));
        String prefix = getReputationStatus(rep);
        String gradePrefix = getLuckPermsPrefix(player);
        event.setFormat(prefix + " " +  ChatColor.RESET + "<%1$s> %2$s");
        Player players = event.getPlayer();

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {


        if (event.getView().getTitle().equals("RepGui")) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Vérifie si le joueur est un staff
        if (!player.hasPermission("moderator") && !player.hasPermission("op")) {
            return;
        }

        GameMode newGameMode = event.getNewGameMode();
        File survivalFile = new File(getDataFolder()+ "/inv/", uuid + "_Survival.dat");
        File creativeFile = new File(getDataFolder()+ "/inv/", uuid + "_Creative.dat");
        File adventureFile = new File(getDataFolder()+ "/inv/", uuid + "_Adventure.dat");
        File spectatorFile = new File(getDataFolder()+ "/inv/", uuid + "_Spectator.dat");

        if (newGameMode == GameMode.CREATIVE) {
            saveInventory(player, survivalFile);
            saveInventory(player, adventureFile); // Sauvegarde l'inventaire de survie
            loadInventory(player, creativeFile); // Charge l'inventaire créatif
            Bukkit.getLogger().info("[Inventaire] " + player.getName() + " passe en Créatif (inventaire chargé).");

        } else if (newGameMode == GameMode.SURVIVAL) {
            saveInventory(player, creativeFile);  // Sauvegarde l'inventaire créatif
            loadInventory(player, survivalFile);
            saveInventory(player, adventureFile);
            saveInventory(player, spectatorFile);// Charge l'inventaire de survie
            Bukkit.getLogger().info("[Inventaire] " + player.getName() + " passe en Survie (inventaire restauré).");
        }if (newGameMode == GameMode.ADVENTURE){
            saveInventory(player, survivalFile);
            saveInventory(player, creativeFile); // Sauvegarde l'inventaire de survie
            loadInventory(player, adventureFile);
            saveInventory(player, spectatorFile);// Charge l'inventaire créatif
            Bukkit.getLogger().info("[Inventaire] " + player.getName() + " passe en Adventure (inventaire chargé).");

        }
        if (newGameMode == GameMode.SPECTATOR){
            saveInventory(player, survivalFile);
            saveInventory(player, creativeFile); // Sauvegarde l'inventaire de survie
            loadInventory(player, spectatorFile);
            saveInventory(player, adventureFile);// Charge l'inventaire créatif
            Bukkit.getLogger().info("[Inventaire] " + player.getName() + " passe en Specrator (inventaire chargé).");

        }
    }

    private void saveInventory(Player player, File file) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("inventory", player.getInventory().getContents());

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadInventory(Player player, File file) {
        if (!file.exists()) {
            player.getInventory().clear(); // Vide l'inventaire si aucun fichier trouvé
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<?> items = config.getList("inventory");
        if (items != null) {
            player.getInventory().setContents(items.toArray(new ItemStack[0]));
        }
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        if (blockType.toString().contains("ORE")) {
            int xp = getJobXp("jobs.mineur.xp_per_block", 10);
            addXp(player, "mineur", xp);
            player.sendMessage("\u00a76[Jobs] \u00a7f+" + xp + " XP pour avoir miné du minerai !");
        } else if (blockType.toString().contains("LOG")) {
            int xp = getJobXp("jobs.bucheron.xp_per_block", 10);
            addXp(player, "bucheron", xp);
            player.sendMessage("\u00a76[Jobs] \u00a7f+" + xp + " XP pour avoir coupé du bois !");
        }
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player != null) {
            int xp = getJobXp("jobs.chasseur.xp_per_mob", 20);
            addXp(player, "chasseur", xp);
            player.sendMessage("\u00a76[Jobs] \u00a7f+" + xp + " XP pour avoir tué une créature !");
        }
    }

    @EventHandler
    public void onBrewPotion(BrewEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().equals(event.getContents())) {
                int xp = getJobXp("jobs.alchimiste.xp_per_potion", 15);
                addXp(player, "alchimiste", xp);
                player.sendMessage("\u00a76[Jobs] \u00a7f+" + xp + " XP pour avoir fabriqué une potion !");
            }
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            int xp = getJobXp("jobs.pêcheur.xp_per_fish", 15);
            addXp(player, "pêcheur", xp);
            player.sendMessage("\u00a76[Jobs] \u00a7f+" + xp + " XP pour avoir pêché un poisson !");
        }
    }
}







