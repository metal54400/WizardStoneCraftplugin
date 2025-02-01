package fr.WizardStoneCraft;



import com.earth2me.essentials.Essentials;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.*;



public class WizardStoneCraft extends JavaPlugin implements TabExecutor,Listener {


    public final Map<UUID, Integer> reputation = new HashMap<>();
    private final Map<UUID, Map<UUID, Long>> killHistory = new HashMap<>();
    private final Map<Player, Integer> playerReputations = new HashMap<>(); // Stocke la réputation des joueurs
    private final Map<Player, Player> selectedPlayers = new HashMap<>();
    List<String> lore = new ArrayList<>(); // Crée une liste vide
    public int MIN_REP;
    public  int MAX_REP;
    private int pointsKill;
    private int pointsJoin;
    public int DixPoint = 10;
    public int MoinDixPoint = -10;
    private FileConfiguration messages;
    private String tabPrefix;
    private String chatPrefix;
    private FileConfiguration config;
    private LuckPerms luckPerms;
    private Essentials essentials;
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
        WizardStoneCraft plugin = this;







        // Initialisation des API
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getLogger().info("§7[§e?§7]§a LuckPerms API détectée et connectée !");
        } else {
            getLogger().warning("§7[§e?§7]§c LuckPerms API non détectée !");
        }

        RegisteredServiceProvider<Essentials> essentialsRegisteredServiceProvider = Bukkit.getServicesManager().getRegistration(Essentials.class);
        if (provider != null) {
            essentials = essentialsRegisteredServiceProvider.getProvider();
            getLogger().info("§7[§e?§7]§a Essentials API détectée et connectée !");
        } else {
            getLogger().warning("§7[§e?§7]§c Essentials API non détectée !");
        }




        new TablistUpdater(this).runTaskTimer(this, 1000000000, 1000000000);

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("repadd").setExecutor(new ManageRepCommand());
        getCommand("repsubtract").setExecutor(new ManageRepCommand());
        getCommand("reptop").setExecutor(new ReptopCommand());
        getCommand("rep").setExecutor(new ReputationCommand());
        getCommand("rephighlight").setExecutor(new RepHighlightCommand());
        getCommand("help").setExecutor(new RepHelpCommand());
        getCommand("repreload").setExecutor(new RepReloadCommand(this));
        getCommand("Broadcast").setExecutor(new Broadcast());
        getCommand("tabreload").setExecutor(new UpdateTablistCommand(this));
        //getCommand("profile").setExecutor(new ProfileMenu());


    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("repmenu") && sender instanceof Player) {
            Player player = (Player) sender;

            // Vérifie si le joueur est un administrateur
            if (player.hasPermission("reputation.admin")) {
                openPlayerListMenu(player);
                return true;
            } else {
                player.sendMessage("§7[§e?§7] §cVous n'avez pas la permission d'utiliser cette commande.");
            }
        }
        return false;
    }



    /**
     * Ouvre le menu de la liste des joueurs.
     */
    /**
     * Ouvre le menu de la liste des joueurs pour l'administrateur.
     */
    public void openPlayerListMenu(Player admin) {
        // Taille ajustée à un multiple de 9 (par exemple : 54 slots)
        int inventorySize = 54;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, "Liste des joueurs");

        // Ajoute la tête de chaque joueur en ligne
        for (Player target : Bukkit.getOnlinePlayers()) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);

            ItemMeta meta = playerHead.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + target.getName()); // Nom du joueur avec couleur
                playerHead.setItemMeta(meta);
            }

            // Ajoute la tête du joueur dans l'inventaire
            inventory.addItem(playerHead);
        }

        // Ouvre l'inventaire pour l'administrateur
        admin.openInventory(inventory);
    }

    /**
     * Ouvre le menu de modification de réputation pour un joueur cible.
     */
    public void openReputationEditMenu(Player admin, Player target) {
        // Taille ajustée (9 slots suffisent pour ce menu)
        Inventory inventory = Bukkit.createInventory(null, 9, "Modifier Réputation : " + target.getName());

        // Item pour ajouter des points
        ItemStack addReputation = new ItemStack(Material.GREEN_DYE);
        ItemMeta addMeta = addReputation.getItemMeta();
        if (addMeta != null) {
            addMeta.setDisplayName("§aAjouter 10 points"); // Nom personnalisé
            addReputation.setItemMeta(addMeta);
        }

        // Item pour retirer des points
        ItemStack removeReputation = new ItemStack(Material.RED_DYE);
        ItemMeta removeMeta = removeReputation.getItemMeta();
        if (removeMeta != null) {
            removeMeta.setDisplayName("§cRetirer 10 points"); // Nom personnalisé
            removeReputation.setItemMeta(removeMeta);
        }

        // Item pour afficher la réputation actuelle
        ItemStack currentReputation = new ItemStack(Material.PAPER);
        ItemMeta currentMeta = currentReputation.getItemMeta();
        if (currentMeta != null) {
            int reputation = playerReputations.getOrDefault(target, 0); // Récupère la réputation ou 0 par défaut
            currentMeta.setDisplayName("§aRéputation actuelle : " + reputation); // Affiche la réputation
            currentReputation.setItemMeta(currentMeta);
        }

        // Ajoute les items dans le menu
        inventory.setItem(3, addReputation);      // Slot 3 : Ajouter des points
        inventory.setItem(5, removeReputation);  // Slot 5 : Retirer des points
        inventory.setItem(4, currentReputation); // Slot 4 : Réputation actuelle

        // Ouvre le menu pour l'administrateur
        admin.openInventory(inventory);

        // Stocke le joueur cible dans la map `selectedPlayers`
        selectedPlayers.put(admin, target);
    }

    @Override
    public void onDisable() {
        getLogger().info("§7[§e?§7]§c ReputationPlugin désactivé !");
    }

    private void loadConfiguration() {
        MIN_REP = getConfig().getInt("minimum-reputation");
        MAX_REP = getConfig().getInt("maximum-reputation");
        pointsKill = getConfig().getInt("points-kill");
        DixPoint = getConfig().getInt("dixpoint");
        MoinDixPoint = getConfig().getInt("Moindixpoint");
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
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GREEN + "Menu des Profils")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            SkullMeta skullMeta = (SkullMeta) clickedItem.getItemMeta();

            if (skullMeta == null || skullMeta.getOwningPlayer() == null) {
                return;
            }

            Player target = Bukkit.getPlayer(skullMeta.getOwningPlayer().getName());

            if (target != null) {
                player.sendMessage(ChatColor.AQUA + "Statistiques de " + ChatColor.YELLOW + target.getName() + ":");
                player.sendMessage(ChatColor.GREEN + "- Argent: " + ChatColor.GOLD + econ /* Remplace par la vraie valeur */);
                player.sendMessage(ChatColor.GREEN + "- Succès: " + ChatColor.GOLD + "10/50" /* Remplace par la vraie valeur */);
                player.sendMessage(ChatColor.GREEN + "- Réputation: " + ChatColor.GOLD + reputation /* Remplace par la vraie valeur */);
            }
        }
    }
    @EventHandler
    public void onInventoryRepClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Vérifie si un item est cliqué
        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) {
            return;
        }

        // Gestion du menu de la liste des joueurs
        if (title.equals("Liste des joueurs")) {
            event.setCancelled(true); // Empêche de prendre des objets

            if (event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                // Récupère le nom du joueur affiché
                String targetName = event.getCurrentItem().getItemMeta().getDisplayName();
                if (targetName.length() > 2) {
                    targetName = targetName.substring(2); // Retire le préfixe "§e"
                } else {
                    player.sendMessage("§7[§e?§7]§c Nom de joueur invalide.");
                    return;
                }

                // Récupère le joueur correspondant
                Player target = Bukkit.getPlayerExact(targetName);
                if (target != null) {
                    openReputationEditMenu(player, target); // Ouvre le menu de modification pour ce joueur
                } else {
                    player.sendMessage("§7[§e?§7]§c Le joueur " + targetName + " n'est pas en ligne.");
                }
            }
        }

        // Gestion du menu de modification de réputation
        else if (title.startsWith("§7[§e?§7]§a Modifier Réputation")) {
            event.setCancelled(true); // Empêche de prendre des objets

            // Vérifie si un joueur est sélectionné
            if (!selectedPlayers.containsKey(player)) {
                player.sendMessage("§7[§e?§7]§c Erreur : aucun joueur sélectionné.");
                return;
            }

            // Récupère le joueur cible
            Player target = selectedPlayers.get(player);
            if (target != null) {
                switch (event.getCurrentItem().getType()) {
                    case GREEN_DYE -> {
                        // Commande pour ajouter 10 points
                        boolean success = player.performCommand("repadd " + target.getName() + DixPoint );
                        if (success) {
                            player.sendMessage("§7[§e?§7]§a 10 points de réputation ajoutés à " + target.getName() + ".");
                        } else {
                            player.sendMessage("§7[§e?§7]§c Échec de l'exécution de la commande.");
                        }
                        openReputationEditMenu(player, target);
                        event.setCancelled(true); // Rafraîchit le menu
                    }
                    case RED_DYE -> {

                        // Commande pour retirer 10 points
                        boolean success = player.performCommand("repadd " + target.getName() + MoinDixPoint );
                        if (success) {
                            player.sendMessage("§7[§e?§7]§a 10 points de réputation retirés à " + target.getName() + ".");
                        } else {
                            player.sendMessage("§7[§e?§7]§c Échec de l'exécution de la commande.");
                        }
                        openReputationEditMenu(player, target); // Rafraîchit le menu
                        event.setCancelled(true);
                    }
                    case PAPER -> {
                        // Affiche la réputation actuelle
                        int reputation = playerReputations.getOrDefault(target, 0);
                        player.sendMessage("§7[§e?§7] La réputation actuelle de " + target.getName() + " est : §a" + reputation + ".");
                        event.setCancelled(true);
                    }
                    default -> {
                        // Cas d'objet inattendu
                        player.sendMessage("§7[§e?§7]§c Action non reconnue.");
                    }
                }
            } else {
                player.sendMessage("§7[§e?§7]§c Erreur : aucun joueur sélectionné.");
            }
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
                        // Récupère le lore existant (ou crée un nouveau s'il est vide)
                        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

                        // Ajoute le nom du joueur au lore
                        lore.add("§7Rembourcé par " + event.getWhoClicked().getName());
                        meta.setLore(lore); // Met à jour le lore de l'objet
                        item.setItemMeta(meta); // Applique les modifications à l'objet
                    }
                }
            }
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

            // Initialiser l'historique du tueur s'il n'existe pas encore
            killHistory.putIfAbsent(killerId, new HashMap<>());
            Map<UUID, Long> killerVictimHistory = killHistory.get(killerId);

            // Vérifier si le tueur a déjà tué la victime au cours des 24 dernières heures
            if (killerVictimHistory.containsKey(victimId)) {
                long lastKillTime = killerVictimHistory.get(victimId);
                if (currentTime - lastKillTime <= 24 * 60 * 60 * 1000) { // 24 heures en millisecondes
                    // Appliquer une pénalité de réputation
                    int currentRep = reputation.getOrDefault(killerId, loadPlayerReputation(killerId));
                    int newRep = Math.max(currentRep - 1, MIN_REP); // Réduire de 1, sans descendre sous MIN_REP
                    reputation.put(killerId, newRep);
                    savePlayerReputation(killerId, newRep);
                    killer.sendMessage(getMessage("reputation_lost24"));
                }
            }

            // Mettre à jour l'historique des kills
            killerVictimHistory.put(victimId, currentTime);

            // Récompenser le tueur avec les points de kill normaux
            int newRep = reputation.getOrDefault(killerId, loadPlayerReputation(killerId)) + pointsKill;
            reputation.put(killerId, Math.max(newRep, MIN_REP));
            killer.sendMessage(getMessage("reputation_lost"));

            savePlayerReputation(killerId, Math.max(newRep, MIN_REP));
            updateTablist(killer);
        }
    }







    private void updateTabList(Player player) {
        UUID playerId = player.getUniqueId();
        int rep = reputation.getOrDefault(playerId, loadPlayerReputation(playerId));
        String prefix = getColoredReputationPrefix(rep);
        String tabName = prefix + " " + ChatColor.RESET + player.getName();
        player.setPlayerListName(tabName);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        int rep = reputation.getOrDefault(playerId, loadPlayerReputation(playerId));
        String prefix = getColoredReputationPrefix(rep);
        String gradePrefix = getLuckPermsPrefix(player);
        event.setFormat(prefix + " " + gradePrefix + " " + ChatColor.RESET + "<%1$s> %2$s");
        Player players = event.getPlayer();

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
    }

    public void updateTablist(Player player) {
        // Lire le header et footer depuis le fichier de configuration
        String header = ChatColor.translateAlternateColorCodes('&', config.getString("tablist.header", "&aBienvenue sur le serveur"));
        String footer = ChatColor.translateAlternateColorCodes('&', config.getString("tablist.footer", "&6Amusez-vous bien !"));

        // Mettre à jour la tablist
        player.setPlayerListHeaderFooter(header,footer);

        // Ajouter un préfixe basé sur la réputation dans le nom de la tablist
        String reputationPrefix = getReputationPrefix(player);
        String gradePrefix = getLuckPermsPrefix(player);
        player.setPlayerListName(  reputationPrefix + " "  + gradePrefix + " " + ChatColor.RESET + player.getName());
    }

    /**
     * Méthode pour obtenir le préfixe de réputation d'un joueur.
     *
     * @param player Le joueur.
     * @return Le préfixe coloré de la réputation.
     */
    private String getReputationPrefix(Player player) {
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
        return getConfig().getString("reputation-status.horrible");
    }


    private String getReputationStatus(int reputation) {
        if (reputation >= 100) return getConfig().getString("reputation-prefix.honorable");
        if (reputation >= 50) return getConfig().getString("reputation-prefix.good");
        if (reputation >= 0) return getConfig().getString("reputation-prefix.neutral");
        if (reputation >= -50) return getConfig().getString("reputation-prefix.dangerous");
        if (reputation >= -100) return getConfig().getString("reputation-prefix.bad");
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
            Player target = Bukkit.getPlayer(targetPlayer);
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

    public class ReputationCommand implements TabExecutor {
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
            Player target = Bukkit.getPlayer(targetPlayer);
            if (target == null) {
                sender.sendMessage(getMessage("player_not_found"));
                return true;
            }

            UUID targetId = target.getUniqueId();
            int rep = reputation.getOrDefault(targetId, loadPlayerReputation(targetId));
            String status = getReputationStatus(rep);
            sender.sendMessage(getMessage("reputation_status")
                    .replace("%player%", target.getName())
                    .replace("%reputation%", String.valueOf(rep))
                    .replace("%status%", status));
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            return null; // Implémentation pour les suggestions automatiques
        }
    }

    public class ReptopCommand implements TabExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            sender.sendMessage(getMessage("top_reputations"));
            reputation.entrySet().stream()
                    .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> sender.sendMessage("" + "§7[§e?§7]" + " " + ChatColor.GRAY + Bukkit.getOfflinePlayer(entry.getKey()).getName() + ": " + entry.getValue() +   " points" ));

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
            Player target = Bukkit.getPlayer(targetPlayer);
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
}


