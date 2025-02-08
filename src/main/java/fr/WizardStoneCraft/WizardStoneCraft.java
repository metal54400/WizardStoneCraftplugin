package fr.WizardStoneCraft;



import fr.WizardStoneCraft.Commands.*;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.io.*;
import java.time.Instant;
import java.util.*;

import static org.bukkit.Bukkit.getPlayer;


public class WizardStoneCraft extends JavaPlugin implements TabExecutor,Listener {

    private File bannedPlayersFile;
    private FileConfiguration bannedPlayersConfig;
    private Map<UUID, Long> bannedPlayers;
    public final Map<UUID, Integer> reputation = new HashMap<>();
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
    private FileConfiguration config;
    private LuckPerms luckPerms;
    private static Economy econ = null;






    @Override
    public void onLoad() {
        getLogger().info("§7[§e?§7]§a ReputationPlugin chargé !");
    }

    @Override
    public void onEnable() {
        getLogger().info("§7[§e?§7]§a ReputationPlugin activé !");
        saveDefaultConfig();
        loadConfiguration();
        loadMessages();
        config = getConfig();
        loadBannedPlayersData();
        WizardStoneCraft plugin = this;







        // Initialisation des API
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getLogger().info("§7[§e?§7]§a LuckPerms API détectée et connectée !");
        } else {
            getLogger().warning("§7[§e?§7]§c LuckPerms API non détectée !");
        }


        new TablistUpdater(this).runTaskTimer(this, 10, 10);

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("repadd").setExecutor(new ManageRepCommand());
        getCommand("reptop").setExecutor(new ReptopCommand());
        getCommand("rep").setExecutor(new ReputationCommand());
        getCommand("rephighlight").setExecutor(new RepHighlightCommand());
        getCommand("help").setExecutor(new RepHelpCommand());
        getCommand("repreload").setExecutor(new RepReloadCommand(this));
        getCommand("Broadcast").setExecutor(new Broadcast());
        getCommand("tabreload").setExecutor(new UpdateTablistCommand(this));
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
        File playerFile = new File(getDataFolder(), playerId + ".dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(playerFile))) {
            oos.writeObject(rep);
            getLogger().info("Réputation du joueur " + playerId + " sauvegardée avec succès dans " + playerId + ".dat.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int loadPlayerReputation(UUID playerId) {
        File playerFile = new File(getDataFolder(), playerId + ".dat");
        if (!playerFile.exists()) {
            getLogger().warning("Fichier " + playerId + ".dat introuvable. Initialisation de la réputation par défaut.");
            return 0; // Default reputation
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(playerFile))) {
            return (int) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return 0; // Default reputation in case of error
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            UUID killerId = killer.getUniqueId();
            UUID victimId = victim.getUniqueId();
            long currentTime = System.currentTimeMillis();

            // Initialiser l'historique du tueur si inexistant
            killHistory.putIfAbsent(killerId, new HashMap<>());
            Map<UUID, Long> killerVictimHistory = killHistory.get(killerId);

            // Charger la réputation actuelle
            pointsKill = reputation.getOrDefault(killerId, loadPlayerReputation(killerId));
            int newRep = pointsKill; // Variable pour la nouvelle réputation



            // Vérifier si le tueur a déjà tué la victime en 24h
            if (killerVictimHistory.containsKey(victimId)) {
                long lastKillTime = killerVictimHistory.get(victimId);
                if (currentTime - lastKillTime <= 24 * 60 * 60 * 1000) { // Moins de 24h
                    newRep = Math.max(newRep - 2, MIN_REP); // Appliquer une pénalité
                    killer.sendMessage(getMessage("reputation_lost24"));

                }
            }

            // Mettre à jour l'historique AVANT d'ajouter les points normaux
            killerVictimHistory.put(victimId, currentTime);

            // Mettre à jour la réputation
            reputation.put(killerId, newRep);
            savePlayerReputation(killerId, newRep);
            killer.sendMessage(getMessage("reputation_updated").replace("%reputation%", String.valueOf(newRep)));
        }
    }









    private void updateTabList(Player player) {
        UUID playerId = player.getUniqueId();
        int rep = reputation.getOrDefault(playerId, loadPlayerReputation(playerId));
        String prefix = getReputationStatus(rep);
        String tabName = prefix + " " + ChatColor.RESET + player.getName();
        player.setPlayerListName(tabName);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        int rep = reputation.getOrDefault(playerId, loadPlayerReputation(playerId));
        String prefix = getReputationStatus(rep);
        String gradePrefix = getLuckPermsPrefix(player);
        event.setFormat(prefix + " " + gradePrefix + " " + ChatColor.RESET + "<%1$s> %2$s");
        Player players = event.getPlayer();

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
        player.setPlayerListName(  reputationPrefix + " "  + gradePrefix + " " + ChatColor.RESET + player.getName());
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
        if (reputation >= -50) return getConfig().getString("reputation-status.dangerous");
        if (reputation >= -100) return getConfig().getString("reputation-status.bad");
        return getConfig().getString("reputation-prefix.horrible");
    }


    public String getReputationStatus(int reputation) {
        if (reputation >= 100) return getConfig().getString("reputation-prefix.honorable");
        if (reputation >= 50) return getConfig().getString("reputation-prefix.good");
        if (reputation >= 0) return getConfig().getString("reputation-prefix.neutral");
        if (reputation >= -50) return getConfig().getString("reputation-prefix.dangerous");
        if (reputation >= -100) return getConfig().getString("reputation-prefix.bad");
        return getConfig().getString("reputation-prefix.horrible");
    }
    public String getReputationPrefixe(int reputation) {
        if (reputation >= 100) return getConfig().getString("reputation-prefixe.honorable");
        if (reputation >= 50) return getConfig().getString("reputation-prefixe.good");
        if (reputation >= 0) return getConfig().getString("reputation-prefixe.neutral");
        if (reputation >= -50) return getConfig().getString("reputation-prefixe.dangerous");
        if (reputation >= -100) return getConfig().getString("reputation-prefixe.bad");
        return getConfig().getString("reputation-status.horrible");
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
            Player target = getPlayer(targetPlayer);
            if (target == null) {
                sender.sendMessage(getMessage("player_not_found"));
                return true;
            }

            UUID targetId = target.getUniqueId();
            int rep = reputation.getOrDefault(targetId, loadPlayerReputation(targetId));
            String status = getReputationStatus(rep);
            sender.sendMessage(getMessage("reputation_status")
                    .replace("%player%", target.getName())
                    .replace("%rep%", getReputationPrefixe(rep))
                    .replace("%reputation%", String.valueOf(rep))
                    .replace("%liste%", "" +
                            "\n"+
                            "§dΩ Honorable\n" +
                            "§aΩ Bonne\n" +
                            "§7Ω Neutre\n" +
                            "§6Ω Dangereux\n" +
                            "§cΩ Mauvaise\n" +
                            "§4Ω Horrible\n"+
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
                    .limit(10)
                    .forEach(entry -> {
                        UUID playerId = entry.getKey();
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
                        int reps = entry.getValue();

                        // Obtenir le préfixe de réputation
                        String prefix = getReputationPrefixe(reps);

                        // Afficher le message avec le préfixe
                        sender.sendMessage( offlinePlayer.getName() + ": " + prefix + getMessage("color:gris") + "" + reps + " points de Réputation");
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
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        int newRep = reputation.getOrDefault(playerId, loadPlayerReputation(playerId)) + pointsJoin;
        reputation.put(playerId, Math.min(newRep, MAX_REP));
        player.sendMessage(getMessage("reputation_gained"));
        savePlayerReputation(playerId, Math.min(newRep, MAX_REP));
        updateTabList(player);

        // Check if the player is banned
        if (bannedPlayers.containsKey(playerId)) {
            long banExpiryTime = bannedPlayers.get(playerId);
            long currentTime = Instant.now().getEpochSecond();

            // If the current time is less than the ban expiry time, the player is still banned
            if (currentTime < banExpiryTime) {
                long remainingTime = banExpiryTime - currentTime;
                long remainingDays = remainingTime / (24 * 60 * 60); // Convert to days

                // Inform the player how many days are left on their ban
                player.kickPlayer("You are banned for " + remainingDays + " more days due to your reputation.");
                return;
            }
        }
    }

    public void loadBannedPlayersData() {
        bannedPlayersFile = new File(getDataFolder(), "bannedPlayers.yml");
        if (!bannedPlayersFile.exists()) {
            saveResource("bannedPlayers.yml", false);
        }

        bannedPlayersConfig = YamlConfiguration.loadConfiguration(bannedPlayersFile);
        bannedPlayers = new HashMap<>();

        // Load the ban data into the map
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

    @EventHandler
    public void onCreativeItemMove(InventoryClickEvent event) {
        // Vérifie que le joueur est en mode créatif
        if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
            // Vérifie si le joueur a un grade spécifique
            if (event.getWhoClicked().hasPermission("moderator") || event.getWhoClicked().hasPermission("op")) {
                ItemStack item = event.getCursor(); // Récupère l'objet déplacé
                if (item != null) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        // Vérifie si l'ajout du lore est activé dans la configuration
                        if (config.getBoolean("addLoreOnCreative", true)) { // Par défaut, c'est activé
                            // Vérifie si le lore existe déjà
                            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

                            // Si le lore est vide, on l'ajoute
                            if (lore.isEmpty()) {
                                // Ajoute le nom du joueur au lore
                                lore.add(getMessage("loreitem") + event.getWhoClicked().getName());
                                meta.setLore(lore); // Met à jour le lore de l'objet
                                item.setItemMeta(meta); // Applique les modifications à l'objet
                            }
                        }
                    }
                }
            }
        }
    }
}



