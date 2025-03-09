package fr.WizardStoneCraft;



import fr.WizardStoneCraft.Commands.*;
import fr.WizardStoneCraft.PlaceHolderApi.PlaceHolderApi;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.raid.RaidEvent;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;


import static org.bukkit.Bukkit.getPlayer;


public class WizardStoneCraft extends JavaPlugin implements TabExecutor,Listener {

    private final Map<UUID, Long> raidCooldown = new HashMap<>();
    private final long ONE_HOUR = 3600000L;
    private final Map<UUID, Long> bookTradeCooldown = new HashMap<>();
    private final HashMap<UUID, String> playerStatus = new HashMap<>();
    private final HashMap<UUID, Long> combatLog = new HashMap<>();
    private static final long COMBAT_TIME = 15000; // 15 sec
    private File jobsFile;
    private final int REQUIRED_REP = 80;
    private final HashMap<UUID, Long> protectedPlayers = new HashMap<>();
    private final long PROTECTION_TIME = 90 * 1000;
    private final HashMap<UUID, Long> elytraCooldown = new HashMap<>();
    private final long COOLDOWN_TIME = 4000; // 4 secondes en millisecondes
    private static FileConfiguration jobsConfig;
    private final Set<UUID> refundedPlayers = new HashSet<>(); // Liste des joueurs remboursés
    private Map<UUID, Long> mutedPlayers = new HashMap<>();
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
    private GriefPrevention griefPrevention;
    private static Economy econ = null;
    private Scoreboard scoreboard;
    private static WizardStoneCraft instance;
    private final Random random = new Random();
    private final List<Material> rareItems = Arrays.asList(
            Material.DIAMOND, Material.NETHERITE_INGOT, Material.ENCHANTED_GOLDEN_APPLE,
            Material.BEACON, Material.DRAGON_EGG, Material.ELYTRA
    );
    public List<MerchantRecipe> dailyDeals = new ArrayList<>();
    public LocalDate lastUpdate = LocalDate.now();


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

        RegisteredServiceProvider<GriefPrevention> providers = Bukkit.getServicesManager().getRegistration(GriefPrevention.class);
        if (providers != null) {
            griefPrevention = providers.getProvider();
            getLogger().info("§7[§e?§7]§a GriefPrevention API détectée et connectée !");
        } else {
            getLogger().warning("§7[§e?§7]§c GriefPrevention API non détectée !");
        }


        getLogger().info("§7[§e?§7]§a ReputationPlugin activé !");
        saveDefaultConfig();
        loadConfiguration();
        updateVillagersTradingState();
        loadMessages();
        loadMessages();
        loadConfig();
        loadMessagese();
        config = getConfig();
        dropChance = config.getDouble("drop-chance", 50.0) / 100.0;
        PlaceHolderApi.checkPlaceholderAPI();
        WizardStoneCraft plugin = this;
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        setupTeams();
        updateDailyDeals();

        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("repadd").setExecutor(new ManageRepCommand());
        getCommand("reptop").setExecutor(new ReptopCommand());
        getCommand("rep").setExecutor(new ReputationCommand());
        getCommand("rephighlight").setExecutor(new RepHighlightCommand());
        getCommand("rephelp").setExecutor(new RepHelpCommand());
        getCommand("repreload").setExecutor(new RepReloadCommand(this));
        getCommand("Broadcast").setExecutor(new Broadcast());
        getCommand("tabreload").setExecutor(new UpdateTablistCommand(this));
        getCommand("repmenu").setExecutor(new RepGui());
        getCommand("repspawnnpc").setExecutor(this);
        getCommand("affairenpc").setExecutor(this);
        getCommand("jobsstatue").setExecutor(this);
        getCommand("repunmute").setExecutor(new RepUnmuteCommand(mutedPlayers));
        getCommand("passifset").setExecutor(new PassiveCommand());
        getCommand("passifunset").setExecutor(new PassiveCommand());
        this.getCommand("tpa").setExecutor(new TeleportCommands());
        this.getCommand("tpyes").setExecutor(new TeleportCommands());
        this.getCommand("tpno").setExecutor(new TeleportCommands());
        getCommand("repmute").setExecutor(new RepUnmuteCommand(mutedPlayers));
        getCommand("affairenpc").setExecutor(new npcaffaire());


        instance = this;
        //tab updater
        new TablistUpdater(this).runTaskTimer(this, 10, 10);
    }
    public static WizardStoneCraft getInstance() {
        return instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("repspawnnpc")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                // Vérifier si le joueur a la permission d'exécuter cette commande
                if (player.hasPermission("wizardstonecraft.repspawnnpc")) {
                    // Invoquer le NPC "Pacificateur" à la position du joueur
                    spawnPacificateur(player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "§7[§e?§7]§a Le Pacificateur a été invoqué à votre position !");
                } else {
                    player.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous n'avez pas la permission d'exécuter cette commande.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "§7[§e?§7]§e Cette commande ne peut être exécutée que par un joueur.");
            }
            return true;
        }
        return true;
    }

    private void spawnPacificateur(Location location) {
        World world = location.getWorld();
        if (world != null) {
            // Supprimer les anciens NPC "Pacificateur"
            world.getEntitiesByClass(Villager.class).stream()
                    .filter(v -> v.getCustomName() != null && v.getCustomName().equals("Pacificateur"))
                    .forEach(Villager::remove);
            // Invoquer un nouveau NPC "Pacificateur"
            Villager pacificateur = world.spawn(location, Villager.class);
            pacificateur.setCustomName("§7[§e?§7]§a Pacificateur");
            pacificateur.setCustomNameVisible(true);
            pacificateur.setAI(false);
        }
    }
    public static boolean isPassive(Player player) {
        return PassiveCommand.passivePlayers.getOrDefault(player.getUniqueId(), false);
    }

    private void updateDailyDeals() {
        if (!LocalDate.now().isEqual(lastUpdate)) {
            lastUpdate = LocalDate.now();
            dailyDeals.clear();
            for (int i = 0; i < 3; i++) {
                ItemStack sellItem = new ItemStack(rareItems.get(random.nextInt(rareItems.size())), 1);
                ItemStack cost = new ItemStack(Material.EMERALD, random.nextInt(32) + 16);
                MerchantRecipe trade = new MerchantRecipe(sellItem, 10);
                trade.addIngredient(cost);
                dailyDeals.add(trade);
            }
        }
    }






    private void loadConfig() {
        jobsFile = new File(getDataFolder(), "jobs.yml");
        if (!jobsFile.exists()) {
            saveResource("jobs.yml", false);
        }
        jobsConfig = YamlConfiguration.loadConfiguration(jobsFile);
    }

    public static int getJobXp(String jobPath, int defaultValue) {
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


    public int getReputation(Player player) {
        return loadPlayerReputation(player.getUniqueId());
    }

    public void removeReputation(Player player, int amount) {
        UUID playerId = player.getUniqueId();
        int currentReputation = getReputation(player);

        // Mise à jour de la réputation
        int newReputation = Math.max(-120, currentReputation - amount);
        savePlayerReputation(playerId, newReputation);

        // Vérification du bannissement

    }





    void savePlayerReputation(UUID playerId, int rep) {
        File repFolder = new File(getDataFolder(), "rep");
        if (!repFolder.exists()) {
            repFolder.mkdirs();
        }
        File playerFile = new File(repFolder, playerId + ".dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(playerFile))) {
            oos.writeObject(rep);
            getLogger().info("Réputation du joueur " + playerId + " sauvegardée avec succès.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int loadPlayerReputation(UUID playerId) {
        File playerFile = new File(WizardStoneCraft.getInstance().getDataFolder(), "rep/" + playerId + ".dat");

        if (!playerFile.exists()) {
            return 0;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(playerFile))) {
            return (int) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }






    private void setupTeams() {
        createTeam("PROTECTED", ChatColor.BLUE);
        createTeam("COMBAT", ChatColor.RED);
        createTeam("PASSIVE", ChatColor.GREEN);
        createTeam("NEUTRAL", ChatColor.WHITE);
    }
    private void createTeam(String name, ChatColor color) {
        Team team = scoreboard.getTeam(name);
        if (team == null) {
            team = scoreboard.registerNewTeam(name);
        }
        team.setColor(color);
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
        if (reputation >= 10)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.correct"));
        if (reputation >= 0)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.neutral"));
        if (reputation >= -50)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.dangerous"));
        if (reputation >= -100)
            return ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.bad"));
        if (reputation >= -120) ChatColor.translateAlternateColorCodes('&', config.getString("reputation-prefix.horrible"));
        return "";
    }

    private String getColoredReputationPrefix(int reputation) {

        if (reputation >= 100) return getConfig().getString("reputation-status.honorable");
        if (reputation >= 50) return getConfig().getString("reputation-status.good");
        if (reputation >= 10) return getConfig().getString("reputation-status.correct");
        if (reputation >= 0) return getConfig().getString("reputation-status.neutral");
        if (reputation >= -10) return getConfig().getString("reputation-status.dangerous");
        if (reputation >= -100) return getConfig().getString("reputation-status.bad");
        if (reputation >= -120) return getConfig().getString("reputation-prefix.horrible");
        return "";
    }


    public String getReputationStatus(int reputation) {
        if (reputation >= 100) return getConfig().getString("reputation-prefix.honorable");
        if (reputation >= 50) return getConfig().getString("reputation-prefix.good");
        if (reputation >= 10) return getConfig().getString("reputation-prefix.correct");
        if (reputation >= 0) return getConfig().getString("reputation-prefix.neutral");
        if (reputation >= -10) return getConfig().getString("reputation-prefix.dangerous");
        if (reputation >= -50) return getConfig().getString("reputation-prefix.bad");
        if (reputation >= -120) return getConfig().getString("reputation-prefix.horrible");

        return "";
    }
    public String getReputationPrefixe(int reputation) {
        if (reputation >= 100) return getConfig().getString("reputation-prefixe.honorable");
        if (reputation >= 50) return getConfig().getString("reputation-prefixe.good");
        if (reputation >= 10) return getConfig().getString("reputation-prefixe.correct");
        if (reputation >= 0) return getConfig().getString("reputation-prefixe.neutral");
        if (reputation >= -10) return getConfig().getString("reputation-prefixe.dangerous");
        if (reputation >= -50) return getConfig().getString("reputation-prefixe.bad");
        if (reputation >= -120) return getConfig().getString("reputation-prefixe.horrible");
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

            if (newReputation <= -120) {
                // Auto-ban de 15 jours
                long banDuration = System.currentTimeMillis() + (15L * 24 * 60 * 60 * 1000);
                Bukkit.getScheduler().runTask((Plugin) this, () -> {
                    target.kickPlayer(getMessage("auto_ban_message"));
                    Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), getMessage("ban_reason"), new Date(banDuration), "System");
                });
            } else if (newReputation <= -100) {
                // Auto-mute de 5 jours
                mutePlayer(targetId, 5);
                sender.sendMessage(getMessage("auto_mute_message").replace("%player%", target.getName()));
            }
            return true;
        }



        public void mutePlayer(UUID playerId, int days) {
            mutedPlayers.put(playerId, System.currentTimeMillis() + (days * 24L * 60 * 60 * 1000));
        }



        private void checkMutedPlayers() {
            long currentTime = System.currentTimeMillis();
            mutedPlayers.entrySet().removeIf(entry -> entry.getValue() < currentTime);
        }

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
            return List.of();
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
                            "- §dΩ Honorable§F = 100§7\n" +
                            "- §2Ω Bonne§f = 50§7 \n" +
                            "- §aΩ correct§f = 10§7\n" +
                            "- §7Ω Neutre§f = 0§7\n" +
                            "- §6Ω Dangereux§f = -10§7\n" +
                            "- §cΩ Mauvaise§f = -50§7\n" +
                            "- §4Ω Horrible§f = -100§7\n"+
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

//event
@EventHandler
public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getHand() == EquipmentSlot.HAND) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item.getType() == Material.STICK) {
            Claim claim = griefPrevention.dataStore.getClaimAt(event.getPlayer().getLocation(), true, null);
            if (claim == null) {
                event.getPlayer().sendMessage(ChatColor.RED + "Vous n'êtes pas dans un claim.");
                return;
            }
            drawClaimBorder(event.getPlayer(), claim);
            event.getPlayer().sendMessage(ChatColor.GREEN + "Les limites du claim sont affichées !");
        }
    }
}

    private void drawClaimBorder(org.bukkit.entity.Player player, Claim claim) {
        int minX = claim.getLesserBoundaryCorner().getBlockX();
        int minZ = claim.getLesserBoundaryCorner().getBlockZ();
        int maxX = claim.getGreaterBoundaryCorner().getBlockX();
        int maxZ = claim.getGreaterBoundaryCorner().getBlockZ();
        int y = player.getLocation().getBlockY(); // Hauteur de la ligne

        for (int x = minX; x <= maxX; x++) {
            Location loc1 = new Location(player.getWorld(), x, y, minZ);
            Location loc2 = new Location(player.getWorld(), x, y, maxZ);
            player.sendBlockChange(loc1, Material.YELLOW_WOOL.createBlockData());
            player.sendBlockChange(loc2, Material.YELLOW_WOOL.createBlockData());
        }
        for (int z = minZ; z <= maxZ; z++) {
            Location loc3 = new Location(player.getWorld(), minX, y, z);
            Location loc4 = new Location(player.getWorld(), maxX, y, z);
            player.sendBlockChange(loc3, Material.YELLOW_WOOL.createBlockData());
            player.sendBlockChange(loc4, Material.YELLOW_WOOL.createBlockData());
        }
    }

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

            // Appliquer une perte de réputation (minimum 0)
            int newRep = Math.max(pointsKill - 2, 0);

            // Mettre à jour la réputation
            reputation.put(killerId, newRep);
            savePlayerReputation(killerId, newRep);

            // Envoyer un message au joueur
            String message = getMessage("reputation_lost");
            if (message != null) {
                killer.sendMessage(ChatColor.RED + message.replace("%points%", String.valueOf(newRep)));
            } else {
                killer.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous avez perdu de la réputation ! Nouvelle réputation : " + newRep);
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
        if (isPassive(player)) {
            player.setDisplayName(ChatColor.GREEN + player.getName());
        } else {
            player.setDisplayName(ChatColor.AQUA + player.getName()); // Cyan pour joueurs protégés
        }
        protectedPlayers.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage("§7[§e?§7] §aVous êtes protégé pendant 90 secondes après votre connexion !");

    }



    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {}



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
    public void onVillagerTrade(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // Vérifie si l'entité est un villageois
        if (holder instanceof Villager) {
            Player player = (Player) event.getPlayer();
            long time = player.getWorld().getTime(); // Temps du monde (0 = 06:00, 6000 = 12:00)

            // Plage horaire autorisée (8h à 16h in-game, soit 2000 à 12000 ticks)
            if (time < 2000 || time > 12000) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.YELLOW + "?" + ChatColor.GRAY + "] "
                        + ChatColor.RED + "Ce n'est pas l'heure d'échanger avec les villageois ! "
                        + ChatColor.GRAY + "Les heures d'échange sont de 8h à 16h.");
            }
        }
    }
    @EventHandler
    public void onClockUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.CLOCK) {
            long time = player.getWorld().getTime();
            int hours = (int) ((time / 1000 + 6) % 24); // Convertir ticks en heures
            player.sendMessage(ChatColor.GRAY + "Heure actuelle en jeu : " + ChatColor.YELLOW + hours + "h");
        }
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Material item = player.getInventory().getItemInMainHand().getType();

        // Vérifier si le joueur fait un clic droit avec une montre ou une boussole
        if (event.getAction().toString().contains("RIGHT_CLICK") &&
                (item == Material.CLOCK || item == Material.COMPASS)) {

            // Récupérer l'heure en jeu
            long time = player.getWorld().getTime();
            int hours = (int) ((time / 1000 + 6) % 24);
            int minutes = (int) ((time % 1000) * 60 / 1000);

            // Récupérer l'heure réelle (IRL)
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            dateFormat.setTimeZone(TimeZone.getDefault()); // Fuseau horaire du serveur
            String realTime = dateFormat.format(new Date());

            // Message de base avec l'heure
            String message = ChatColor.GRAY + "In-game: " + ChatColor.AQUA + String.format("%02d:%02d", hours, minutes) +
                    ChatColor.GRAY + " | IRL: " + ChatColor.LIGHT_PURPLE + realTime;

            // Si c'est une boussole, ajouter les coordonnées
            if (item == Material.COMPASS) {
                Location loc = player.getLocation();
                message += ChatColor.GRAY + " | X: " + ChatColor.YELLOW + loc.getBlockX() +
                        ChatColor.GRAY + " Y: " + ChatColor.YELLOW + loc.getBlockY() +
                        ChatColor.GRAY + " Z: " + ChatColor.YELLOW + loc.getBlockZ();
            }

            // Envoyer le message au joueur
            player.sendMessage(message);
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
            player.sendMessage("§7[§e?§7] Vous avez Changé d'inventaire");
        } else if (newGameMode == GameMode.SURVIVAL) {
            saveInventory(player, creativeFile);  // Sauvegarde l'inventaire créatif
            loadInventory(player, survivalFile);// Charge l'inventaire de survie
            Bukkit.getLogger().info("[Inventaire] " + player.getName() + " passe en Survie (inventaire restauré).");
            player.sendMessage("§7[§e?§7] Vous avez Changé d'inventaire");
        }
        if (newGameMode == GameMode.ADVENTURE){
            saveInventory(player, creativeFile); // Sauvegarde l'inventaire de survie
            loadInventory(player, adventureFile); // Charge l'inventaire créatif
            Bukkit.getLogger().info("[Inventaire] " + player.getName() + " passe en Adventure (inventaire chargé).");
            player.sendMessage("§7[§e?§7] Vous avez Changé d'inventaire");
        }
        if (newGameMode == GameMode.SPECTATOR){
            saveInventory(player, creativeFile); // Sauvegarde l'inventaire de survie
            loadInventory(player, spectatorFile);
            // Charge l'inventaire créatif
            Bukkit.getLogger().info("[Inventaire] " + player.getName() + " passe en Specrator (inventaire chargé).");
            player.sendMessage("§7[§e?§7] Vous avez Changé d'inventaire");
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
    public void onElytraBoost(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getItem() != null && event.getItem().getType() == Material.FIREWORK_ROCKET) {
            if (player.isGliding()) { // Vérifie si le joueur est en vol avec l'elytra
                UUID playerId = player.getUniqueId();
                long currentTime = System.currentTimeMillis();

                if (elytraCooldown.containsKey(playerId)) {
                    long lastUse = elytraCooldown.get(playerId);
                    if (currentTime - lastUse < COOLDOWN_TIME) {
                        event.setCancelled(true);
                        player.sendMessage("§7[§e?§7] §cAttendez avant d'utiliser une autre fusée !");
                        return;
                    }
                }
                elytraCooldown.put(playerId, currentTime);
            }
        }
    }

    @EventHandler
    public void onElytraFly(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        if (player.isGliding() && player.getGameMode() != GameMode.CREATIVE) {
            player.setVelocity(player.getVelocity().multiply(0.7)); // Réduction de vitesse de 30%
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
    @EventHandler
    public void onMaceHit(EntityDamageByEntityEvent event) {
        // Vérifie si l'attaquant est un joueur
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();

        // Vérifie si la cible est un joueur
        if (!(event.getEntity() instanceof Player)) return;
        Player target = (Player) event.getEntity();

        // Vérifie si l'attaquant utilise une Masse
        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon.getType() != Material.MACE) return; // Remplace `WOODEN_AXE` par le bon item si nécessaire

        // Vérifie si la cible porte au moins une pièce d'armure en Diamant ou Netherite
        if (!hasDiamondOrNetheriteArmor(target)) return;

        // Réduction des dégâts de 60%
        double originalDamage = event.getDamage();
        double reducedDamage = originalDamage * 0.4; // 40% des dégâts d'origine
        event.setDamage(reducedDamage);

        // Empêcher le one-shot
        double targetHealth = target.getHealth();
        double maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (reducedDamage >= targetHealth) {
            event.setDamage(targetHealth - 1); // Laisse au moins 1 HP pour éviter le one-shot
        }
    }

    private boolean hasDiamondOrNetheriteArmor(Player player) {
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null) {
                Material type = armor.getType();
                if (type == Material.DIAMOND_HELMET || type == Material.DIAMOND_CHESTPLATE ||
                        type == Material.DIAMOND_LEGGINGS || type == Material.DIAMOND_BOOTS ||
                        type == Material.NETHERITE_HELMET || type == Material.NETHERITE_CHESTPLATE ||
                        type == Material.NETHERITE_LEGGINGS || type == Material.NETHERITE_BOOTS) {
                    return true;
                }
            }
        }
        return false;
    }



    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        protectedPlayers.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage("§7[§e?§7] §aVous êtes protégé pendant 90 secondes après votre téléportation !");
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player target = (Player) event.getEntity();
        UUID targetId = target.getUniqueId();

        if (protectedPlayers.containsKey(targetId)) {
            long protectionStart = protectedPlayers.get(targetId);
            if (System.currentTimeMillis() - protectionStart < PROTECTION_TIME) {
                event.setCancelled(true);
                event.getDamager().sendMessage("§7[§e?§7] §cCe joueur est actuellement protégé !");
            }
        }
    }





    @EventHandler
    public void onTimeSkip(TimeSkipEvent event) {
        updateVillagersTradingState();
    }


    private void updateVillagersTradingState() {
        long time = Bukkit.getWorld("world").getTime();
        boolean tradingAllowed = time >= 2000 && time <= 12000;
        for (Villager villager : Bukkit.getWorld("world").getEntitiesByClass(Villager.class)) {
            villager.setAI(tradingAllowed);
        }
    }

    @EventHandler
    public void onPlayerChats(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Vérifie si le joueur est mute
        if (mutedPlayers.containsKey(playerId)) {
            long muteExpiration = mutedPlayers.get(playerId);
            if (muteExpiration > currentTime) {
                player.sendMessage(getMessage("chat_muted"));
                event.setCancelled(true);
            } else {
                unmutePlayer(playerId); // Supprime le mute si expiré
            }
        }
    }
    public void unmutePlayer(UUID playerId) {
        mutedPlayers.remove(playerId);
    }


    @EventHandler
    public void onPlayerConsumeMilk(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item.getType() == Material.MILK_BUCKET) {
            // Autoriser la consommation de lait normalement
            player.sendMessage("§7[§e?§7] §aVous avez bu du lait !");
        }
    }

    @EventHandler
    public void onPlayerDamages(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            if (isPassive(victim)) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.RED + " §7[§e?§7]§a Ce joueur est en Mode Passif §c! Vous ne pouvez pas l'attaquer.");
            }
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof Villager) {
            Villager villager = (Villager) event.getRightClicked();
            if ("§7[§e?§7]§a Pacificateur".equals(villager.getCustomName())) {
                Player player = event.getPlayer();
                int reputation = getReputation(player); // Implémentez cette méthode pour obtenir la réputation du joueur
                if (reputation >= REQUIRED_REP) {
                    PassiveCommand.setPassive(player, true);
                    player.sendMessage(ChatColor.RED +"§7[§e?§7] §aVous avez activer le Mode Passif." );
                } else {
                    player.sendMessage(ChatColor.RED + "§7[§e?§7] §cVous n'avez pas assez de réputation pour activer le Mode Passif.");
                }
            }
        }
    }
    @EventHandler
    public void onPlayerJoins(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        setPseudoColor(player, "PROTECTED");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && playerStatus.get(player.getUniqueId()).equals("PROTECTED")) {
                    setPseudoColor(player, "NEUTRAL");
                }
            }
        }.runTaskLater(this, 200L); // 10 sec
    }

    @EventHandler
    public void onPlayerTeleports(PlayerTeleportEvent event) {
        setPseudoColor(event.getPlayer(), "PROTECTED");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getPlayer().isOnline() && playerStatus.get(event.getPlayer().getUniqueId()).equals("PROTECTED")) {
                    setPseudoColor(event.getPlayer(), "NEUTRAL");
                }
            }
        }.runTaskLater(this, 200L); // 10 sec
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            if (playerStatus.getOrDefault(attacker.getUniqueId(), "NEUTRAL").equals("PASSIVE")) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous ne pouvez pas attaquer en mode passif !");
                return;
            }

            setPseudoColor(attacker, "COMBAT");
            setPseudoColor(victim, "COMBAT");
            combatLog.put(attacker.getUniqueId(), System.currentTimeMillis());
            combatLog.put(victim.getUniqueId(), System.currentTimeMillis());
        }
    }
    @EventHandler
    public void onTrade(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory().getHolder() instanceof Villager villager)) return;

        MerchantRecipe trade = villager.getRecipe(event.getSlot());
        Material result = trade.getResult().getType();
        UUID playerUUID = player.getUniqueId();

        // Limitation des livres enchantés (1 par heure)
        if (result == Material.ENCHANTED_BOOK) {
            long lastTrade = bookTradeCooldown.getOrDefault(playerUUID, 0L);
            if (System.currentTimeMillis() - lastTrade < 3600000) {
                event.setCancelled(true);
                player.sendMessage("\u00a7cVous devez attendre avant d'acheter un autre livre enchanté !");
                return;
            }
            bookTradeCooldown.put(playerUUID, System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onVillagerTradeChange(VillagerAcquireTradeEvent event) {
        MerchantRecipe trade = event.getRecipe();
        Material result = trade.getResult().getType();

        // Appliquer les limitations sur certains échanges
        if (result == Material.DIAMOND_CHESTPLATE || result == Material.DIAMOND_PICKAXE) {
            trade.setMaxUses(1); // 1 trade par refill
        } else if (result == Material.COAL || result == Material.STICK) {
            trade.setMaxUses(5); // 5 trades par refill
        }
    }

    @EventHandler
    public void onPlayerQuits(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (combatLog.containsKey(player.getUniqueId()) && System.currentTimeMillis() - combatLog.get(player.getUniqueId()) <= COMBAT_TIME) {
            Bukkit.getServer().broadcastMessage(ChatColor.RED + player.getName() + "§7[§e?§7]§c s'est déconnecté en combat et a été sanctionné !");
            player.setHealth(0.0);
        }
    }







    public void setPseudoColor(Player player, String status) {
        Team team = scoreboard.getTeam(status);
        if (team != null) {
            team.addEntry(player.getName());
        }
        playerStatus.put(player.getUniqueId(), status);
    }
    @EventHandler
    public void onRaidWaveSpawn(RaidSpawnWaveEvent event) {
        // On ne vérifie le cooldown qu'au début du raid (première vague)
        if (event.getPatrolLeader().getWave() == 1) {
            Raid raid = event.getRaid();
            long now = System.currentTimeMillis();
            Iterator<UUID> it = raid.getHeroes().iterator();
            while (it.hasNext()) {
                UUID player = it.next();
                Player player1 = getPlayer(player);
                if (raidCooldown.containsKey(player)) {
                    long lastRaid = raidCooldown.get(player);
                    if (now - lastRaid < ONE_HOUR) {
                        long minutesRestantes = (ONE_HOUR - (now - lastRaid)) / 60000;
                        player1.sendMessage("§7[§e?§7] Vous devez attendre encore " + minutesRestantes + " minutes avant de participer à un nouveau raid.");
                        // On retire le joueur de la liste des participants du raid
                        it.remove();
                        continue;
                    }
                }
                // Mise à jour du cooldown pour le joueur
                raidCooldown.put(player, now);
            }
        }
    }

    @EventHandler
    public void onPotionSplashs(PotionSplashEvent event) {
        ThrownPotion thrownPotion = event.getPotion();
        ItemStack potionItem = thrownPotion.getItem();
        if (potionItem.hasItemMeta() && potionItem.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta) potionItem.getItemMeta();
            // Vérifier les effets personnalisés
            for (PotionEffect effect : meta.getCustomEffects()) {
                String effectName = effect.getType().getName();
                if (effectName.equalsIgnoreCase("oozing") || effectName.equalsIgnoreCase("infested")) {
                    // Annule l’événement pour empêcher l’application des effets
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        ItemStack potionItem = event.getPotion().getItem();

        if (potionItem.hasItemMeta() && potionItem.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = (PotionMeta) potionItem.getItemMeta();
            for (PotionEffect effect : meta.getCustomEffects()) {
                String effectName = effect.getType().getName().toLowerCase();

                // Interdire le lancer de potions "oozing" ou "infested" en s'accroupissant
                if ((effectName.contains("oozing") || effectName.contains("infested")) &&
                        event.getPotion().getShooter() instanceof Player) {

                    Player shooter = (Player) event.getPotion().getShooter();
                    if (shooter.isSneaking()) {
                        shooter.sendMessage("Vous ne pouvez pas lancer des potions oozing ou infested en vous accroupissant.");
                        event.setCancelled(true);
                        return;
                    }
                }

                // Interdire les potions persistantes de faiblesse dans un claim
                if (effectName.contains("weakness") && potionItem.getType() == Material.LINGERING_POTION &&
                        event.getPotion().getShooter() instanceof Player) {

                    Player shooter = (Player) event.getPotion().getShooter();
                    if (isInClaim(shooter.getLocation())) {
                        shooter.sendMessage("Vous ne pouvez pas utiliser de potions persistantes de faiblesse dans un claim.");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    // Interdire le lancement via distributeur pour les potions "oozing" ou "infested"
    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        ItemStack item = event.getItem();
        if ((item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION) &&
                item.hasItemMeta() && item.getItemMeta() instanceof PotionMeta) {

            PotionMeta meta = (PotionMeta) item.getItemMeta();
            for (PotionEffect effect : meta.getCustomEffects()) {
                String effectName = effect.getType().getName().toLowerCase();
                if (effectName.contains("oozing") || effectName.contains("infested")) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    // Méthode de vérification de claim via GriefPrevention
    private boolean isInClaim(Location loc) {
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);
        return claim != null;
    }
}












