package simpleRep;



import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;



public class ReputationPlugin extends JavaPlugin implements TabExecutor,Listener {
    final Map<UUID, Integer> reputation = new HashMap<>();
    private final Map<Player, Integer> playerReputations = new HashMap<>(); // Stocke la réputation des joueurs
    private final Map<Player, Player> selectedPlayers = new HashMap<>(); // Stocke quel joueur est sélectionné par quel admin
    public int MIN_REP;
    public  int MAX_REP;
    private int pointsKill;
    private int pointsJoin;
    private FileConfiguration messages;
    private String tabPrefix;
    private String chatPrefix;
    private FileConfiguration config;
    private LuckPerms luckPerms;

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
        ReputationPlugin plugin = this;





        // Initialisation de LuckPerms API
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getLogger().info("§7[§e?§7]§a LuckPerms API détectée et connectée !");
        } else {
            getLogger().warning("§7[§e?§7]§c LuckPerms API non détectée !");
        }



        new TablistUpdater(this).runTaskTimer(this, 1000000000, 1000000000);

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("repadd").setExecutor(new ManageRepCommand());
        getCommand("repsubtract").setExecutor(new ManageRepCommand());
        getCommand("reptop").setExecutor(new ReptopCommand());
        getCommand("rep").setExecutor(new ReputationCommand());
        getCommand("rephighlight").setExecutor(new RepHighlightCommand());
        getCommand("rephelp").setExecutor(new RepHelpCommand());
        getCommand("repreload").setExecutor(new RepReloadCommand(this));



        // Créer une instance de RepMenu et l'enregistrer en tant qu'écouteur d'événements
        this.getCommand("tabreload").setExecutor(new UpdateTablistCommand(this));

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
    public void openPlayerListMenu(Player admin) {
        Inventory inventory = Bukkit.createInventory(null, 27, "Liste des joueurs");

        // Ajoute la tête de chaque joueur en ligne
        for (Player target : Bukkit.getOnlinePlayers()) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);

            ItemMeta meta = playerHead.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§e" + target.getName());
                playerHead.setItemMeta(meta);
            }
            inventory.addItem(playerHead);
        }

        admin.openInventory(inventory);
    }

    /**
     * Ouvre le menu de modification de réputation.
     */
    public void openReputationEditMenu(Player admin, Player target) {
        Inventory inventory = Bukkit.createInventory(null, 9, "Modifier Réputation : " + target.getName());

        // Item pour ajouter des points
        ItemStack addReputation = new ItemStack(Material.GREEN_DYE);
        ItemMeta addMeta = addReputation.getItemMeta();
        if (addMeta != null) {
            addMeta.setDisplayName("§7[§e?§7]§a Ajouter 10 points");
            addReputation.setItemMeta(addMeta);
        }

        // Item pour retirer des points
        ItemStack removeReputation = new ItemStack(Material.RED_DYE);
        ItemMeta removeMeta = removeReputation.getItemMeta();
        if (removeMeta != null) {
            removeMeta.setDisplayName("§7[§e?§7]§c Retirer 10 points");
            removeReputation.setItemMeta(removeMeta);
        }

        // Item pour afficher la réputation actuelle
        ItemStack currentReputation = new ItemStack(Material.PAPER);
        ItemMeta currentMeta = currentReputation.getItemMeta();
        if (currentMeta != null) {
            int reputation = playerReputations.getOrDefault(target, 0);
            currentMeta.setDisplayName("§7[§e?§7]§a Réputation actuelle : " + reputation);
            currentReputation.setItemMeta(currentMeta);
        }

        inventory.setItem(3, addReputation);
        inventory.setItem(5, removeReputation);
        inventory.setItem(4, currentReputation);

        admin.openInventory(inventory);
        selectedPlayers.put(admin, target); // Stocke quel joueur est sélectionné
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

    void loadMessages() {
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
            int newRep = reputation.getOrDefault(killerId, loadPlayerReputation(killerId)) + pointsKill;
            reputation.put(killerId, Math.max(newRep, MIN_REP));
            killer.sendMessage(getMessage("reputation_lost"));
            savePlayerReputation(killerId, Math.max(newRep, MIN_REP));
            updateTablist(killer);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Gestion du menu de la liste des joueurs
        if (title.equals("Liste des joueurs")) {
            event.setCancelled(true);

            if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.PLAYER_HEAD) {
                String targetName = event.getCurrentItem().getItemMeta().getDisplayName().substring(2); // Retire le préfixe "§e"
                Player target = Bukkit.getPlayerExact(targetName);

                if (target != null) {
                    openReputationEditMenu(player, target); // Ouvre le menu de modification pour ce joueur
                } else {
                    player.sendMessage("§cLe joueur " + targetName + " n'est pas en ligne.");
                }
            }
        }

        // Gestion du menu de modification de réputation
        else if (title.startsWith("Modifier Réputation")) {
            event.setCancelled(true);

            if (event.getCurrentItem() != null) {
                Player target = selectedPlayers.get(player); // Récupère le joueur sélectionné

                if (target != null) {
                    switch (event.getCurrentItem().getType()) {
                        case GREEN_DYE -> {
                            // Exécute la commande /repadd pour ajouter 10 points
                            String command = "repadd " + target.getName();
                            player.performCommand(command); // Le joueur exécute la commande
                            player.sendMessage("§aCommande exécutée : " + command);
                            openReputationEditMenu(player, target); // Rafraîchit le menu
                        }
                        case RED_DYE -> {
                            // Exécute la commande /repsubtract pour retirer 10 points
                            String command = "repsubtract " + target.getName();
                            player.performCommand(command); // Le joueur exécute la commande
                            player.sendMessage("§cCommande exécutée : " + command);
                            openReputationEditMenu(player, target); // Rafraîchit le menu
                        }
                        case PAPER -> {
                            // Affiche la réputation actuelle (clic inutile)
                            player.sendMessage("§eLa réputation actuelle de " + target.getName() + " est : " +
                                    playerReputations.getOrDefault(target, 0));
                        }
                    }
                } else {
                    player.sendMessage("§cErreur : aucun joueur sélectionné.");
                }
            }
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
        private final ReputationPlugin plugin;

        public TablistUpdater(ReputationPlugin plugin) {
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

        private final ReputationPlugin plugin;

        public UpdateTablistCommand(ReputationPlugin plugin) {
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


