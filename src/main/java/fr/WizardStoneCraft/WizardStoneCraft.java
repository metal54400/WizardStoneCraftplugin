package fr.WizardStoneCraft;



import fr.WizardStoneCraft.Commands.*;
import fr.WizardStoneCraft.Commands.Anticheat.Topluck;
import fr.WizardStoneCraft.Commands.Claim.*;
import fr.WizardStoneCraft.Commands.Reputation.*;
import fr.WizardStoneCraft.PlaceHolderApi.PlaceHolderApi;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;


import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static fr.WizardStoneCraft.Commands.Reputation.repspawnnpc.isPassive;

import static org.bukkit.Bukkit.getPlayer;


public class WizardStoneCraft extends JavaPlugin implements TabExecutor,Listener {

    private final HashMap<UUID, Integer> clickCounts = new HashMap<>();

    private final Map<UUID, Long> raidCooldown = new HashMap<>();
    public final HashSet<Long> disabledClaims = new HashSet<Long>();
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
    public Map<UUID, Long> mutedPlayers = new HashMap<>();
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
    private int pointsKills;
    private int pointsKill;
    private int pointsJoin;
    private FileConfiguration messages;
    private String tabPrefix;
    private String chatPrefix;
    public FileConfiguration config;
    private LuckPerms luckPerms;
    public GriefPrevention griefPrevention;
    private TabAPI tab;
    private static Economy econ = null;
    private static WizardStoneCraft instance;
    private static final Random random = new Random();
    private final List<Material> rareItems = Arrays.asList(
            Material.DIAMOND, Material.NETHERITE_INGOT, Material.ENCHANTED_GOLDEN_APPLE,
            Material.BEACON, Material.DRAGON_EGG, Material.ELYTRA
    );
    public List<MerchantRecipe> dailyDeals = new ArrayList<>();
    public LocalDate lastUpdate = LocalDate.now();
    private final List<String> blockedCommands = Arrays.asList("/tpa", "/tpahere", "/tpno", "/sethome", "/team sethome","/tpaccept","tpcancel");
    private final HashMap<UUID, Integer> minedOres = new HashMap<>();
    private final HashMap<UUID, Long> lastClickTime = new HashMap<>();
    private final HashMap<UUID, Double> lastSpeed = new HashMap<>();
    private final HashMap<UUID, Long> lastTeleport = new HashMap<>();
    private final HashMap<UUID, HashMap<UUID, Integer>> killTracking = new HashMap<>();
    private final HashMap<UUID, Integer> flyWarnings = new HashMap<>();
    private final Set<Player> alertEnabledPlayers = new HashSet<>();
    private FileConfiguration menuConfig;
    private final HashMap<UUID, Integer> craftCounter = new HashMap<>();
    private int maxCraftsPerDay;
    private int craftRadius;
    private Economy economy;
    private int atmAmount;
    private String atmMessage;
    private HashMap<UUID, Integer> sellerReputations;
    private boolean thornsDisabled;
    private final HashMap<UUID, BossBar> pvpTimers = new HashMap<>();
    private boolean disableThorns;
    private boolean onlyPvp;
    private boolean mobsAffected;
    private final Set<Material> customDespawnItems = new HashSet<>(Arrays.asList(
            Material.SUGAR_CANE,
            Material.MELON_SLICE,
            Material.PUMPKIN,
            Material.KELP
    ));
    private final HashMap<UUID, Integer> redstoneActivations = new HashMap<>();
    private final HashMap<UUID, Long> lastActivation = new HashMap<>();
    private FileConfiguration messagesConfig;
    private List<String> serverMessages;
    private int messageIndex = 0;
    public FileConfiguration topluckConfig;
    private Map<Player, Long> protectionCooldowns;
    private enum Season {SPRING, SUMMER, AUTUMN, WINTER}
    private Season currentSeason = Season.SPRING;
    private int seasonDuration = 6000; // Durée en ticks (5 minutes = 6000 ticks)
    private HashMap<UUID, Long> infectedPlayers = new HashMap<>();
    private AdvancedForge forge;


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

        if (!Bukkit.getPluginManager().isPluginEnabled("TAB")) {
            getLogger().warning("Le plugin TAB n'est pas actif ! Certaines fonctionnalités seront désactivées.");
        } else {
            getLogger().info("Plugin TAB détecté !");
        }


        Plugin plugin = getServer().getPluginManager().getPlugin("GriefPrevention");
        if (plugin instanceof GriefPrevention) {
            griefPrevention = (GriefPrevention) plugin;
            getLogger().info("GriefPrevention détecté et lié avec succès.");
        } else {
            getLogger().warning("GriefPrevention n'a pas été trouvé ! Certaines fonctionnalités ne fonctionneront pas.");
        }
        if (!setupEconomy()) {
            getLogger().severe("Vault est requis pour l'économie ! Désactivation du plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("§7[§e?§7]§a ReputationPlugin activé !");
        saveDefaultConfig();
        loadConfiguration();
        startSeasonCycle();
        startDiseaseSpread();
        loadMessages();
        loadMessages();
        loadConfig();
        loadMessagese();
        loadMessagesConfig();
        loadTopLuckConfig();
        startMessageTask();
        triggerRandomEvents();
        forge = new AdvancedForge();
        config = getConfig();
        dropChance = config.getDouble("drop-chance", 50.0) / 100.0;
        PlaceHolderApi.checkPlaceholderAPI();
        WizardStoneCraft plugins = this;
        updateDailyDeals();
        maxCraftsPerDay = getConfig().getInt("craft_limit", 50);
        craftRadius = config.getInt("craft_radius", 5);
        atmAmount = config.getInt("atm_amount", 100);
        atmMessage = config.getString("atm_message", "Vous avez retiré %amount% pièces depuis l'ATM !");
        disableThorns = getConfig().getBoolean("disable_thorns.enabled", true);
        onlyPvp = getConfig().getBoolean("disable_thorns.only_pvp", true);
        mobsAffected = getConfig().getBoolean("disable_thorns.mobs_affected", false);

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("repadd").setExecutor(new ManageRepCommand());
        getCommand("repremove").setExecutor(new ManageRepCommand());
        getCommand("Claimoffspawnmob").setExecutor(new claimoffmobspawn());
        getCommand("reptop").setExecutor(new ReptopCommand());
        getCommand("rep").setExecutor(new ReputationCommand());
        getCommand("rephighlight").setExecutor(new RepHighlightCommand());
        getCommand("rephelp").setExecutor(new RepHelpCommand());
        getCommand("repreload").setExecutor(new RepReloadCommand(this));
        getCommand("Broadcast").setExecutor(new Broadcast());
        getCommand("repmenu").setExecutor(new RepGui());
        getCommand("repspawnnpc").setExecutor(new repspawnnpc());
        getCommand("affairenpc").setExecutor(this);
        getCommand("jobsstatue").setExecutor(this);
        getCommand("repunmute").setExecutor(new RepUnmuteCommand(mutedPlayers));
        getCommand("passifset").setExecutor(new PassiveCommand());
        getCommand("passifunset").setExecutor(new PassiveCommand());
        getCommand("repmute").setExecutor(new RepUnmuteCommand(mutedPlayers));
        getCommand("affairenpc").setExecutor(new npcaffaire());
        getCommand("tradeclaim").setExecutor(new TradeClaimCommand());
        getCommand("claimmeteo").setExecutor(new ClaimWeatherControl());
        getCommand("cook").setExecutor(new CookFoodCommand());
        getCommand("claimlist").setExecutor(new Claims());
        getCommand("elytra").setExecutor(new endelytra());
        getCommand("claimrename").setExecutor(new ClaimNaming());
        getCommand("topluck").setExecutor(new Topluck());
        getCommand("givejukebox").setExecutor(new Jukeboxmod());
        getCommand("giveceleste").setExecutor(new GiveCelestialArtifactCommand());
        getCommand("anticheatmenu").setExecutor((sender, command, label, args) -> {
            if (sender instanceof Player) {
                openMenus((Player) sender);
            } else {
                sender.sendMessage(ChatColor.RED + "Seuls les joueurs peuvent utiliser cette commande !");
            }
            return true;
        });
        getLogger().info("Anti-Cheat Menu activé !");



        instance = this;

// Cette méthode s'appelle après le rechargement ou la reconnexion du serveur

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!Bukkit.getPluginManager().isPluginEnabled("TAB")) {
                getLogger().warning("Le plugin TAB n'est toujours pas actif après le délai !");
                return;
            }

            TabAPI tabAPI = TabAPI.getInstance();
            if (tabAPI == null) {
                getLogger().warning("Impossible d'obtenir l'instance de TabAPI !");
                return;
            }

            getLogger().info("TabAPI initialisé avec succès !");

            // Enregistrement du Placeholder
            tabAPI.getPlaceholderManager().registerPlayerPlaceholder("%reputation_prefix%", 1000, (TabPlayer tabPlayer) -> {
                Player player = (Player) tabPlayer.getPlayer();
                if (player == null) return "";

                UUID playerId = player.getUniqueId();
                int rep = reputation.getOrDefault(playerId, loadPlayerReputation(playerId));
                return getReputationStatus(rep);  // Le préfixe basé sur la réputation
            });
        }, 20L);
        // Lors de la mise à jour du Tablist, applique le préfixe à chaque joueur
        Bukkit.getScheduler().runTask(this, () -> {
            TabAPI tabAPI = TabAPI.getInstance();
            for (Player player : Bukkit.getOnlinePlayers()) {
                String prefix = getReputationStatus(reputation.getOrDefault(player.getUniqueId(), loadPlayerReputation(player.getUniqueId())));
                tabAPI.getPlayer(String.valueOf(player)).setTemporaryGroup(prefix); // Applique le préfixe
            }
        });
        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Rafraîchit la tablist pour que le préfixe soit pris en compte
                player.setPlayerListName(player.getName());  // Force la mise à jour du nom
            }
        }, 20L);

// 1 seconde de délai
    }


    public static WizardStoneCraft getInstance() {
        return instance;
    }
    public void openMenus(Player player) {
        Inventory menu = Bukkit.createInventory(null, 9, ChatColor.DARK_RED + "⚠ Menu Anti-Cheat ⚠");

        // Item pour activer/désactiver les alertes
        ItemStack toggleItem = new ItemStack(alertEnabledPlayers.contains(player) ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta meta = toggleItem.getItemMeta();
        meta.setDisplayName(alertEnabledPlayers.contains(player) ? ChatColor.GREEN + "✅ Alertes activées" : ChatColor.RED + "❌ Alertes désactivées");
        toggleItem.setItemMeta(meta);

        menu.setItem(4, toggleItem); // Met l'item au centre du menu
        player.openInventory(menu);
    }




    private void loadTopLuckConfig() {
        File topluckFile = new File(getDataFolder(), "TopLuck.yml");
        if (!topluckFile.exists()) {
            saveResource("TopLuck.yml", false);
        }
        topluckConfig = YamlConfiguration.loadConfiguration(topluckFile);
    }


        public void loadMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "ServerMessage.yml");
        if (!messagesFile.exists()) {
            saveResource("ServerMessage.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        serverMessages = messagesConfig.getStringList("messages");
    }

    private void startMessageTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (serverMessages.isEmpty()) return;
            Bukkit.broadcastMessage(serverMessages.get(messageIndex));
            messageIndex = (messageIndex + 1) % serverMessages.size();
        }, 0L, 300L); // Toutes les 15 secondes (300 ticks)
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






    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
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
            player.sendMessage("\u00a76[Jobs] \u00a7fFélicitations §7[§c!§7] Vous avez atteint le niveau " + currentLevel + " en " + job + "§7[§c!§7]");
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
            player.sendMessage("\u00a76[Jobs] \u00a7fVous avez reçu une récompense pour votre montée en niveau §7[§c!§7]");
        }
    }





    @Override
    public void onDisable() {
        getLogger().info("§7[§e?§7]§c ReputationPlugin désactivé !");
    }

    private void loadConfiguration() {
        MIN_REP = getConfig().getInt("minimum-reputation");
        MAX_REP = getConfig().getInt("maximum-reputation");
        pointsKills = getConfig().getInt("points-kill");
        pointsJoin = getConfig().getInt("points-join");
    }

    public void loadMessages() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String key) {
        return ChatColor.translateAlternateColorCodes('&', messages.getString(key));
    }

    private String sendChatMessage(Player player, String message) {
        player.sendMessage(chatPrefix + " " + message);
        return message;
    }


    public int getReputation(Player player ) {

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







    public void savePlayerReputation(UUID playerId, int rep) {
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



@EventHandler
public void onPlayerRightClick(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    ItemStack item = player.getInventory().getItemInMainHand();

    // Vérifier si le joueur tient un bâton et fait un clic droit
    if (item.getType() == Material.STICK && event.getAction().toString().contains("RIGHT_CLICK")) {
        Location loc = player.getLocation();
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);

        if (claim == null) {
            player.sendMessage(ChatColor.RED + "§7(§c!§7)§c Ce terrain n'est pas claim.");
            return;
        }

        String owner = claim.getOwnerName();
        player.sendMessage(ChatColor.GREEN + "§7(§e!§7) Ce terrain appartient à : " + ChatColor.GOLD + owner);

        // Récupérer les coins du claim
        Location min = claim.getLesserBoundaryCorner();
        Location max = claim.getGreaterBoundaryCorner();
        World world = min.getWorld();

        // Vérifier si le monde est valide
        if (world == null) {
            player.sendMessage(ChatColor.RED + "§7(§c!§7)§c Erreur : Impossible de récupérer le monde.");
            return;
        }

        // Utilisation d'un HashSet pour éviter les doublons et améliorer la performance
        Set<Block> placedBlocks = new HashSet<>();

        // Placer les bordures avec laine jaune et torches
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            addBlockIfValid(world, x, min.getBlockY(), min.getBlockZ(), Material.YELLOW_WOOL, placedBlocks);
            addBlockIfValid(world, x, min.getBlockY(), max.getBlockZ(), Material.YELLOW_WOOL, placedBlocks);
        }
        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
            addBlockIfValid(world, min.getBlockX(), min.getBlockY(), z, Material.YELLOW_WOOL, placedBlocks);
            addBlockIfValid(world, max.getBlockX(), min.getBlockY(), z, Material.YELLOW_WOOL, placedBlocks);
        }

        // Ajouter glowstone aux coins du claim
        addBlockIfValid(world, min.getBlockX(), min.getBlockY(), min.getBlockZ(), Material.GLOWSTONE, placedBlocks);
        addBlockIfValid(world, max.getBlockX(), min.getBlockY(), max.getBlockZ(), Material.GLOWSTONE, placedBlocks);
        addBlockIfValid(world, min.getBlockX(), min.getBlockY(), max.getBlockZ(), Material.GLOWSTONE, placedBlocks);
        addBlockIfValid(world, max.getBlockX(), min.getBlockY(), min.getBlockZ(), Material.GLOWSTONE, placedBlocks);

        // Ajouter des torches à intervalles réguliers sur les bords
        for (int x = min.getBlockX(); x <= max.getBlockX(); x += 5) {
            addBlockIfValid(world, x, min.getBlockY() + 1, min.getBlockZ(), Material.TORCH, placedBlocks);
            addBlockIfValid(world, x, min.getBlockY() + 1, max.getBlockZ(), Material.TORCH, placedBlocks);
        }
        for (int z = min.getBlockZ(); z <= max.getBlockZ(); z += 5) {
            addBlockIfValid(world, min.getBlockX(), min.getBlockY() + 1, z, Material.TORCH, placedBlocks);
            addBlockIfValid(world, max.getBlockX(), min.getBlockY() + 1, z, Material.TORCH, placedBlocks);
        }

        // Supprimer les blocs après 10 secondes
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Block block : placedBlocks) {
                    if (block.getType() != Material.AIR) {
                        block.setType(Material.AIR);
                    }
                }
            }
        }.runTaskLater(WizardStoneCraft.getInstance(), 200L);
    }
}

    // Méthode pour placer un bloc temporaire seulement si l'emplacement est libre
    private void addBlockIfValid(World world, int x, int y, int z, Material material, Set<Block> placedBlocks) {
        Block block = world.getBlockAt(x, y, z);
        if (block.getType() == Material.AIR) {
            block.setType(material);
            placedBlocks.add(block);
        }
    }

@EventHandler
public void onPlayerDeaths(PlayerDeathEvent event) {
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

    //reputation
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Vérifie si la réputation du joueur est inférieure ou égale à la valeur seuil
        int newRep = reputation.getOrDefault(playerId, loadPlayerReputation(playerId)) + pointsJoin;
        reputation.put(playerId, Math.min(newRep, MAX_REP));
        player.sendMessage(getMessage("reputation_gained"));
        savePlayerReputation(playerId, Math.min(newRep, MAX_REP));
        if (isPassive(player)) {
            player.setDisplayName(ChatColor.GREEN + player.getName());
        } else {
            player.setDisplayName(ChatColor.AQUA + player.getName()); // Cyan pour joueurs protégés
        }
        protectedPlayers.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage("§7[§e?§7] §aVous êtes protégé pendant 90 secondes après votre connexion §7[§c!§7]");

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
    public void onPlayerDeathsssssssss(PlayerDeathEvent event) {
        Player victim = (Player) event.getEntity();
        ;
        Player killer = victim.getKiller();

        if (killer == null || killer == victim) return; // Pas de suicide ou de mort sans tueur

        UUID killerUUID = killer.getUniqueId();
        UUID victimUUID = victim.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Initialiser l'historique du tueur si inexistant
        killHistory.putIfAbsent(killerUUID, new HashMap<>());

        HashMap<UUID, Long> killerRecords = (HashMap<UUID, Long>) killHistory.get(killerUUID);

        // Vérifie si le tueur a déjà tué cette victime
        if (killerRecords.containsKey(victimUUID)) {
            long lastKillTime = killerRecords.get(victimUUID);

            // Vérifie si c'était dans les dernières 48 heures
            if (currentTime - lastKillTime < 172800000L) { // 48h en millisecondes
                killer.sendMessage(net.md_5.bungee.api.ChatColor.RED + "⚠ Focus de kill détecté ! Vous perdez 20 points de réputation.");
                victim.sendMessage(net.md_5.bungee.api.ChatColor.YELLOW + "🚨 " + killer.getName() + " vous a tué en moins de 48h. Un modérateur peut être alerté.");

                // Appliquer la pénalité de réputation
                applyReputationPenalty(killer, 20);

                // Notifier les modérateurs
                for (Player admin : Bukkit.getOnlinePlayers()) {
                    if (admin.hasPermission("wizardstonecraft.moderator")) {
                        admin.sendMessage(net.md_5.bungee.api.ChatColor.RED + "⚠ " + killer.getName() + " a tué " + victim.getName() + " en moins de 48h !");
                        admin.sendMessage(net.md_5.bungee.api.ChatColor.RED + "➡ Une intervention peut être nécessaire.");
                    }
                }

                return;
            }
        }

        // Mettre à jour l'historique du tueur
        killerRecords.put(victimUUID, currentTime);
    }

    private void applyReputationPenalty(Player player, int amount) {
        // Exemple : Ajout d'une méthode pour gérer la réputation du joueur
        WizardStoneCraft.getInstance().getReputation(player);
        WizardStoneCraft.getInstance().removeReputation(player, amount);
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            UUID killerId = killer.getUniqueId();

            // Charger la réputation actuelle
            pointsKill = reputation.getOrDefault(killerId, loadPlayerReputation(killerId));


            // Appliquer une perte de réputation (minimum 0)
            int newRep = Math.max(pointsKills, 0);  // Réduction de la réputation avec un minimum de 0

            // Mettre à jour la réputation
            reputation.put(killerId, newRep);
            savePlayerReputation(killerId, newRep);

            // Envoyer un message au joueur
            String message = getMessage("reputation_lost");
            if (message != null) {
                killer.sendMessage(ChatColor.RED + message.replace("%points%", String.valueOf(newRep)));
            } else {
                killer.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous avez perdu de la réputation §7[§c!§7] Nouvelle réputation : " + newRep);
            }
            killer.sendMessage(ChatColor.RED + "§7[§e?§7]§c Votre Réputation est de " + newRep + "§7[§c!§7]");
        }
        //nullllll
    }

    //reputation



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
     * ✅ Ajoute "Remboursé par NomDuStaff" sur les items placés ou déplacés en mode créatif.
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
    public void onInventoryClickssss(InventoryClickEvent event) {


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
                        + ChatColor.RED + "Ce n'est pas l'heure d'échanger avec les villageois §7[§c!§7] "
                        + ChatColor.GRAY + "Les heures d'échange sont de §d8h §7à §b16h.");
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
            player.sendMessage("§7[§e?§7] Vous avez Changé d'inventaire §7[§c!§7]");
        }
        if (newGameMode == GameMode.SPECTATOR){
            saveInventory(player, creativeFile); // Sauvegarde l'inventaire de survie
            loadInventory(player, spectatorFile);
            // Charge l'inventaire créatif
            Bukkit.getLogger().info("[Inventaire] " + player.getName() + " passe en Specrator (inventaire chargé).");
            player.sendMessage("§7[§e?§7] Vous avez Changé d'inventaire §7[§c!§7]");
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
            player.sendMessage("\u00a76[Jobs] \u00a7f+" + xp + " XP pour avoir miné du minerai §7[§c!§7]");
        } else if (blockType.toString().contains("LOG")) {
            int xp = getJobXp("jobs.bucheron.xp_per_block", 10);
            addXp(player, "bucheron", xp);
            player.sendMessage("\u00a76[Jobs] \u00a7f+" + xp + " XP pour avoir coupé du bois §7[§c!§7]");
        }
    }

    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player != null) {
            int xp = getJobXp("jobs.chasseur.xp_per_mob", 20);
            addXp(player, "chasseur", xp);
            player.sendMessage("\u00a76[Jobs] \u00a7f+" + xp + " XP pour avoir tué une créature §7[§c!§7]");
        }
    }

    @EventHandler
    public void onBrewPotion(BrewEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().equals(event.getContents())) {
                int xp = getJobXp("jobs.alchimiste.xp_per_potion", 15);
                addXp(player, "alchimiste", xp);
                player.sendMessage("\u00a76[Jobs] \u00a7f+" + xp + " XP pour avoir fabriqué une potion §7[§c!§7]");
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
                        player.sendMessage("§7[§e?§7] §cAttendez avant d'utiliser une autre fusée §7[§c!§7]");
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
            player.sendMessage("\u00a76[Jobs] \u00a7f+" + xp + " XP pour avoir pêché un poisson §7[§c!§7]");
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
        player.sendMessage("§7[§e?§7] §aVous êtes protégé pendant 90 secondes après votre téléportation §7[§c!§7]");
    }

    @EventHandler
    public void onPlayerDamagess(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player target = (Player) event.getEntity();
        UUID targetId = target.getUniqueId();

        if (protectedPlayers.containsKey(targetId)) {
            long protectionStart = protectedPlayers.get(targetId);
            if (System.currentTimeMillis() - protectionStart < PROTECTION_TIME) {
                event.setCancelled(true);
                event.getDamager().sendMessage("§7[§e?§7] §cCe joueur est actuellement protégé §7[§c!§7]");
            }
        }
    }




    @EventHandler
    public void onPlayerConsumeMilk(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item.getType() == Material.MILK_BUCKET) {
            // Autoriser la consommation de lait normalement
            player.sendMessage("§7[§e?§7] §aVous avez bu du lait §7[§c!§7]");
        }
    }

    @EventHandler
    public void onPlayerDamages(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            if (isPassive(victim)) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.RED + " §7[§e?§7]§a Ce joueur est en Mode Passif §7[§c!§7] Vous ne pouvez pas l'attaquer.");
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
    public void onMobSpawn(CreatureSpawnEvent event) {
        Location loc = event.getLocation();
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);

        if (claim != null && disabledClaims.contains(claim.getID())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            if (playerStatus.getOrDefault(attacker.getUniqueId(), "NEUTRAL").equals("PASSIVE")) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous ne pouvez pas attaquer en mode passif §7[§c!§7]");
                return;
            }
            ;
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
                player.sendMessage("§7[§e?§7]§c Vous devez attendre avant d'acheter un autre §blivre enchanté §7[§c!§7]");
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
                        shooter.sendMessage("§7[§e?§7] Vous ne pouvez pas lancer des potions oozing ou infested en vous accroupissant.");
                        event.setCancelled(true);
                        return;
                    }
                }

                // Interdire les potions persistantes de faiblesse dans un claim
                if (effectName.contains("weakness") && potionItem.getType() == Material.LINGERING_POTION &&
                        event.getPotion().getShooter() instanceof Player) {

                    Player shooter = (Player) event.getPotion().getShooter();
                    if (isInClaim(shooter.getLocation())) {
                        shooter.sendMessage("§7[§e?§7] Vous ne pouvez pas utiliser de potions persistantes de faiblesse dans un claim.");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

        @EventHandler
        public void onPlayerMovessss(PlayerMoveEvent event) {
            Player player = event.getPlayer();
            World world = player.getWorld();

            if (world.getEnvironment() == World.Environment.NETHER && player.getLocation().getY() >= 127) {
                player.teleport(player.getLocation().subtract(0, 5, 0)); // Redescendre de 5 blocs
                player.sendMessage(ChatColor.RED + "§7[§e?§7]§c L'accès au toit du Nether est interdit !");
            }
        }


    @EventHandler
    public void onElytraUse(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return; // Autoriser en mode créatif et spectateur
        }

        if (isInNewGeneration(loc)) {
            if (player.getInventory().getChestplate() != null &&
                    player.getInventory().getChestplate().getType() == Material.ELYTRA) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "§7[§e?§7]§c Les élytres sont désactivées dans cette zone pour encourager l'exploration !");
            }
        }
    }

    private boolean isInNewGeneration(Location loc) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        return Math.abs(x) > 8000 || Math.abs(z) > 8000;
    }

    @EventHandler
    public void onBlockBreaks(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material block = event.getBlock().getType();
        UUID uuid = player.getUniqueId();

        // Détection X-Ray
        if (block == Material.DIAMOND_ORE || block == Material.EMERALD_ORE || block == Material.ANCIENT_DEBRIS) {
            minedOres.put(uuid, minedOres.getOrDefault(uuid, 0) + 1);

            // Réduction du compteur après 30 secondes
            Bukkit.getScheduler().runTaskLater(this, () ->
                    minedOres.put(uuid, minedOres.get(uuid) - 1), 600L);

            // Vérification du X-Ray
            if (minedOres.get(uuid) > 5) {
                alertAdmins(player, "§7[§e?§7] X-Ray détecté ! (minage suspect de minerais rares)");

                // Bannissement immédiat du joueur
                Bukkit.getScheduler().runTask(this, () -> {
                    player.kickPlayer("§7[§cBanni§7] Vous êtes banni. Raison : Vous avez utilisé X-Ray.");
                    Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(),
                            "§cVous avez été banni pour X-Ray.", null, "Console");
                });
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Détection Fly Hack (Kick après 3 détections)
        if (!player.isOnGround() && player.getGameMode() != GameMode.CREATIVE) {
            if (player.getVelocity().getY() > 0.5) {
                flyWarnings.put(uuid, flyWarnings.getOrDefault(uuid, 0) + 1);
                alertAdmins(player, "§7[§e?§7] Fly hack détecté ! (" + flyWarnings.get(uuid) + "/3)");

                if (flyWarnings.get(uuid) >= 3) {
                    player.kickPlayer(ChatColor.RED + "§7[§e?§7] Fly hack détecté ! Vous avez été expulsé du serveur.");
                    flyWarnings.remove(uuid); // Réinitialiser le compteur après le kick
                }
            }
        }

        // Détection Speed Hack
        double speed = event.getFrom().distance(event.getTo());
        if (speed > 0.7 && lastSpeed.containsKey(uuid) && (speed / lastSpeed.get(uuid)) > 2) {
            alertAdmins(player, "§7[§e?§7] Speed hack détecté !");
        }
        lastSpeed.put(uuid, speed);
    }

    @EventHandler
    public void onPlayerInteractsss(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Détection AutoClicker (CPS)
        lastClickTime.putIfAbsent(uuid, now);
        clickCounts.put(uuid, clickCounts.getOrDefault(uuid, 0) + 1);

        if (now - lastClickTime.get(uuid) >= 1000) { // Vérification chaque seconde
            if (clickCounts.get(uuid) > 10) { // CPS supérieur à 10
                kickPlayer(player, "§7[§4Anticheat§7] Détection AutoClicker (CPS > 10)");
                alertAdmins(player, "§7[§e?§7] AutoClicker détecté ! CPS: " + clickCounts.get(uuid));
            }
            lastClickTime.put(uuid, now);
            clickCounts.put(uuid, 0);
        }
    }

    @EventHandler
    public void onEntityDamagesssss(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            double baseDamage = event.getOriginalDamage(EntityDamageEvent.DamageModifier.BASE); // Dégâts sans enchantements/critiques
            double totalDamage = event.getDamage();

            // Vérifie si les dégâts de base sont supérieurs à 15
            if (baseDamage > 15) {
                // Bannir le joueur avec une raison spécifique
                Bukkit.getScheduler().runTask(this, () -> {
                    Bukkit.getBanList(BanList.Type.NAME).addBan(
                            player.getName(),
                            "§7[§cBanni§7] Vous avez été banni pour KillAura (Dégâts anormaux > 15)",
                            null,
                            "Console"
                    );
                    player.kickPlayer("§7[§cBanni§7] Vous avez été banni pour KillAura.");
                });

                // Avertir les administrateurs
                alertAdmins(player, "§7[§e?§7] KillAura détecté ! Dégâts de base: " + baseDamage + ", Dégâts totaux: " + totalDamage);
            }
        }
    }


    private void kickPlayer(Player player, String reason) {
        Bukkit.getScheduler().runTask(this, () -> player.kickPlayer(reason));
    }

    @EventHandler
    public void onPlayerTeleportss(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        lastTeleport.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            UUID killerUUID = killer.getUniqueId();
            UUID victimUUID = victim.getUniqueId();
            long now = System.currentTimeMillis();

            // Détection TP Kill
            if (lastTeleport.containsKey(killerUUID) && now - lastTeleport.get(killerUUID) < 1000000) {
                alertAdmins(killer, "TP Kill détecté ! (Kill immédiatement après un TP)");
            }

            // Détection Focus (Kill le même joueur 3 fois en 5 minutes)
            killTracking.putIfAbsent(killerUUID, new HashMap<>());
            HashMap<UUID, Integer> victimKills = killTracking.get(killerUUID);
            victimKills.put(victimUUID, victimKills.getOrDefault(victimUUID, 0) + 1);

            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (killTracking.containsKey(killerUUID) && killTracking.get(killerUUID).containsKey(victimUUID)) {
                    killTracking.get(killerUUID).put(victimUUID, killTracking.get(killerUUID).get(victimUUID) - 1);
                }
            }, 6000L); // Retire le kill après 5 minutes

            if (victimKills.get(victimUUID) >= 3) {
                alertAdmins(killer, "Focus détecté ! (A tué " + victim.getName() + " 3 fois en 5 minutes)");
            }
        }
    }

    private void alertAdmins(Player player, String reason) {
        String message = ChatColor.RED + "[AntiCheat] " + ChatColor.YELLOW + player.getName() + " " + ChatColor.RED + "est suspecté de triche ! (" + reason + ")";
        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("anticheat.alerts")) {
                admin.sendMessage(message);
            }
        }
        getLogger().warning(message); // Message dans la console
    }

    // Méthode de vérification de claim via GriefPrevention
    private boolean isInClaim(Location loc) {
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);
        return claim != null;
    }
    @EventHandler
    public void onInventoryClicks(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.DARK_RED + "⚠ Menu Anti-Cheat ⚠")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            if (event.getSlot() == 4) {
                if (alertEnabledPlayers.contains(player)) {
                    alertEnabledPlayers.remove(player);
                    player.sendMessage(ChatColor.RED + "❌ Vous ne recevrez plus les alertes de l'anti-cheat.");
                } else {
                    alertEnabledPlayers.add(player);
                    player.sendMessage(ChatColor.GREEN + "✅ Vous recevrez maintenant les alertes de l'anti-cheat.");
                }
                openMenus(player);
            }
        }
    }

    public void sendAntiCheatAlert(Player suspect, String reason) {
        String message = ChatColor.RED + "[AntiCheat] " + ChatColor.YELLOW + suspect.getName() + ChatColor.RED + " est suspecté de triche ! (" + reason + ")";
        Bukkit.getOnlinePlayers().stream()
                .filter(admin -> admin.hasPermission("anticheat.alerts") && alertEnabledPlayers.contains(admin))
                .forEach(admin -> admin.sendMessage(message));
        for (Player admin : alertEnabledPlayers) {
            admin.sendMessage(message);
        }

        getLogger().warning("[AntiCheat] " + suspect.getName() + " suspecté de triche (" + reason + ")"); // Toujours affiché dans la console
    }


    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase().split(" ")[0]; // Récupère juste la commande

        if (blockedCommands.contains(command)) {
            int reputation = getReputation(event.getPlayer()); // Obtenir la réputation du joueur

            if (reputation <= -50) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "§7[§e?§7] Votre réputation est §ctrop basse §7pour utiliser cette commande §7[§c!§7]");
            }
        }
    }
    @EventHandler
    public void onCommandPreprocesss(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().toLowerCase().split(" ");
        String command = args[0];

        // Vérifie si c'est une commande de téléportation
        if (command.equals("/tpa") || command.equals("/tpahere")) {
            if (args.length < 2) return; // Vérifie s'il y a bien un pseudo après la commande

            Player sender = event.getPlayer();
            Player target = getServer().getPlayer(args[1]); // Récupère le joueur cible

            if (target != null && getReputation(target.getPlayer()) <= -100) {
                event.setCancelled(true);
                sender.sendMessage(ChatColor.RED + "§7[§e?§7] Vous ne pouvez pas vous téléporter à " + target.getName() + " car il a une §cmauvaise réputation §7[§c!§7]");
            }
        }
    }


    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        for (Claim claim : GriefPrevention.instance.dataStore.getClaims()) {
            if (claim.getLesserBoundaryCorner().getWorld().equals(event.getWorld()) && event.toWeatherState()) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onPlayerClickATM(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        Player player = event.getPlayer();

        if (!ChatColor.stripColor(sign.getLine(0)).equalsIgnoreCase("[ATM]")) return;

        economy.depositPlayer(player, atmAmount);
        player.sendMessage(ChatColor.GREEN + atmMessage.replace("%amount%", String.valueOf(atmAmount)));
    }

    @EventHandler
    public void onEnderDragonDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof EnderDragon) {
            EnderDragon dragon = (EnderDragon) event.getEntity();

            // Réduire le volume du son de mort du dragon
            dragon.getWorld().playSound(dragon.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.2f, 1.0f);
        }
    }


    @EventHandler
    public void onBlockForms(BlockFormEvent event) {
        Material type = event.getNewState().getType();
        if (type == Material.SNOW || type == Material.ICE) {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(event.getBlock().getLocation(), false, null);
            if (claim != null) {
                event.setCancelled(true);
            }
        }
    }




    @EventHandler
    public void onChorusTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(event.getTo(), false, null);
            if (claim != null) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "La téléportation avec Chorus Fruit est désactivée dans ce claim.");
            }
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        Material material = item.getItemStack().getType();

        if (customDespawnItems.contains(material)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!item.isDead()) {
                        item.remove(); // Supprime l'item après le temps défini
                    }
                }
            }.runTaskLater(this, 45 * 20L); // 45 secondes (1 tick = 1/20s)
        }
    }

    @EventHandler
    public void onPlayerMoves(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (world.getEnvironment() == World.Environment.NETHER && player.getLocation().getY() >= 127) {
            player.teleport(player.getLocation().subtract(0, 5, 0)); // Redescendre de 5 blocs
            player.sendMessage(ChatColor.RED + "L'accès au toit du Nether est interdit !");
        }
    }

    @EventHandler
    public void onWanderingTraderSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof WanderingTrader) {
            WanderingTrader trader = (WanderingTrader) event.getEntity();
            customizeTrades(trader);
        }
    }

    private void customizeTrades(WanderingTrader trader) {
        List<MerchantRecipe> trades = new ArrayList<>();
        Random random = new Random();

        List<Material> rareItems = List.of(
                Material.DIAMOND_BLOCK, Material.NETHERITE_INGOT, Material.ELYTRA,
                Material.BEACON, Material.TOTEM_OF_UNDYING, Material.ENCHANTED_GOLDEN_APPLE,
                Material.SHULKER_BOX, Material.DRAGON_HEAD, Material.HEART_OF_THE_SEA
        );

        List<Material> commonItems = List.of(
                Material.IRON_INGOT, Material.GOLD_INGOT, Material.EMERALD,
                Material.OBSIDIAN, Material.ENDER_PEARL, Material.LAPIS_LAZULI,
                Material.REDSTONE, Material.GLOWSTONE, Material.QUARTZ
        );

        Collections.shuffle(rareItems, random);
        Collections.shuffle(commonItems, random);

        for (int i = 0; i < 3; i++) { // 3 objets rares
            MerchantRecipe recipe = new MerchantRecipe(new ItemStack(rareItems.get(i)), 10);
            recipe.addIngredient(new ItemStack(Material.EMERALD, random.nextInt(32) + 16));
            trades.add(recipe);
        }

        for (int i = 0; i < 3; i++) { // 3 objets communs
            MerchantRecipe recipe = new MerchantRecipe(new ItemStack(commonItems.get(i), random.nextInt(5) + 1), 10);
            recipe.addIngredient(new ItemStack(Material.EMERALD, random.nextInt(10) + 5));
            trades.add(recipe);
        }

        trader.setRecipes(trades);
    }


    private boolean isNoStuff(Player player) {
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && armor.getType() != Material.AIR) {
                return false; // Le joueur a une armure
            }
        }
        return true; // Le joueur est sans stuff
    }






    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();
        Entity damager = event.getDamager();

        // Vérifie que les deux entités sont des joueurs
        if (damaged.getType() == EntityType.PLAYER && damager.getType() == EntityType.PLAYER) {
            Player victim = (Player) damaged;
            Player attacker = (Player) damager;
            ItemStack weapon = attacker.getInventory().getItemInMainHand();

            startPvPTimer(victim);  // Démarre le timer PvP pour la victime
            startPvPTimer(attacker);  // Démarre le timer PvP pour l'attaquant

            // Vérifie si l'arme est une masse
            if (weapon.getType() == Material.MACE) {
                boolean hasArmor = false;
                for (ItemStack armor : victim.getInventory().getArmorContents()) {
                    if (armor != null && (armor.getType().name().contains("DIAMOND") || armor.getType().name().contains("NETHERITE"))) {
                        hasArmor = true;
                        break;
                    }
                }

                // Réduit les dégâts de 60% si la condition est remplie
                if (hasArmor) {
                    event.setDamage(event.getDamage() * 0.4);
                }
            }
        }
    }

    private void startPvPTimer(Player player) {
        UUID playerId = player.getUniqueId();

        // Si le joueur a déjà une BossBar, on la garde, sinon on en crée une nouvelle
        if (!pvpTimers.containsKey(playerId)) {
            BossBar bossBar = Bukkit.createBossBar("§7[§e?§7]§c Vous êtes en combat §7[§c!§7]", BarColor.RED, BarStyle.SOLID);
            bossBar.addPlayer(player);
            pvpTimers.put(playerId, bossBar);

            // Le timer qui décompte et enlève la BossBar après 15 secondes
            new BukkitRunnable() {
                int timeLeft = 15;
                @Override
                public void run() {
                    if (timeLeft <= 0) {
                        bossBar.removePlayer(player);
                        pvpTimers.remove(playerId);
                        cancel();
                        return;
                    }
                    bossBar.setProgress(timeLeft / 15.0);
                    timeLeft--;
                }
            }.runTaskTimer(this, 0, 20);
        }
    }

    @EventHandler
    public void onInventoryClicksss(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && pvpTimers.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§7[§e?§7]§c Vous ne pouvez pas ouvrir d'inventaire en combat §7[§c!§7]");
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (pvpTimers.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("§7[§e?§7]§c Vous ne pouvez pas exécuter de commande en combat §7[§c!§7]");
        }
    }

    @EventHandler
    public void onPlayerQuitss(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (pvpTimers.containsKey(player.getUniqueId())) {
            pvpTimers.get(player.getUniqueId()).removePlayer(player);
            pvpTimers.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (disableThorns && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // Si l'enchantement est sur l'armure du joueur et qu'il est dans un combat PvP
            if (player.getInventory().getArmorContents() != null) {
                for (var armorPiece : player.getInventory().getArmorContents()) {
                    if (armorPiece.containsEnchantment(Enchantment.THORNS)) {
                        if (onlyPvp && event.getDamager() instanceof Player) {
                            // Empêcher l'enchantement dans les combats PvP
                            event.setDamage(event.getDamage() * 0);  // Annuler les dégâts
                        } else if (!mobsAffected && !(event.getDamager() instanceof Player)) {
                            // Laisser les mobs affectés mais pas les joueurs
                            event.setDamage(event.getDamage());
                        }
                    }
                }
            }
        }
    }





    @EventHandler
    public void onPlayerInteractsssssss(PlayerInteractEvent event) {
        // Vérifier si c'est un clic droit avec un bâton
        if (event.getAction().toString().contains("RIGHT_CLICK") && event.getItem() != null && event.getItem().getType() == Material.STICK) {
            Player player = event.getPlayer();
            // Obtenir le bloc sous le joueur (pour détecter les contours du claim)
            @NotNull Vector playerPosition = player.getLocation().toVector();

            // Trouver le claim à l'endroit du joueur
            Claim claim = griefPrevention.dataStore.getClaimAt(player.getLocation(), true, null);

            if (claim != null) {
                // Afficher les contours du claim
                player.sendMessage(ChatColor.GREEN + "Vous êtes dans le claim: " + claim.getOwnerName());
            } else {
                player.sendMessage(ChatColor.RED + "Vous n'êtes pas dans un claim.");
            }

            // Calculer le nombre de joueurs dans un rayon de 50 blocs
            int playersNearby = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().equals(player.getWorld()) && p.getLocation().distance(player.getLocation()) <= 50) {
                    playersNearby++;
                }
            }

            // Afficher le message avec le nombre de joueurs à proximité
            player.sendMessage(ChatColor.YELLOW + "Il y a " + playersNearby + " joueur(s) autour de vous.");
        }
    }


    @EventHandler
    public void onPlayerUsePortableJukebox(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Vérifie si le joueur clique droit avec un jukebox portatif
        if (item != null && item.hasItemMeta() && item.getItemMeta().getDisplayName().equals("§6Jukebox Portatif")) {
            // Exécuter l'action : jouer une musique ou changer de disque
            Jukeboxmod.playCustomMusic(player);
        }
    }

    // Fonction pour jouer un son ou une musique (ajouter des sons personnalisés si nécessaire)


    // Méthode pour donner un Jukebox Portatif au joueur

    private boolean elytraEnabledInEnd = true;

    public class endelytra implements CommandExecutor{

        @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("elytra")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("activer")) {
                    elytraEnabledInEnd = true;
                    sender.sendMessage("§aLes élytres sont désormais activées dans l'End.");
                } else if (args[0].equalsIgnoreCase("désactiver")) {
                    elytraEnabledInEnd = false;
                    sender.sendMessage("§cLes élytres sont désormais désactivées dans l'End.");
                } else {
                    sender.sendMessage("§cUsage incorrect. Utilisez /elytra <activer|désactiver>");
                }
                return true;
            }
        }
        return false;
    }
    }

    public static class Jukeboxmod implements CommandExecutor {

        private static final HashMap<UUID, Long> cooldowns = new HashMap<>();
        private static final long COOLDOWN_TIME = 50 * 1000; // 50 secondes en millisecondes

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                long currentTime = System.currentTimeMillis();
                UUID uuid = player.getUniqueId();

                // Vérification du cooldown
                if (cooldowns.containsKey(uuid) && (currentTime - cooldowns.get(uuid)) < COOLDOWN_TIME) {
                    long remainingTime = (COOLDOWN_TIME - (currentTime - cooldowns.get(uuid))) / 1000;
                    player.sendMessage("§cVous devez attendre encore " + remainingTime + " secondes avant de changer la musique !");
                    return true;
                }

                playCustomMusic(player);
                cooldowns.put(uuid, currentTime); // Mise à jour du cooldown

                return true;
            }
            sender.sendMessage("Seuls les joueurs peuvent exécuter cette commande.");
            return false;
        }

        private static void playCustomMusic(Player player) {
            String[] discs = {
                    "MUSIC_DISC_13", "MUSIC_DISC_CAT", "MUSIC_DISC_BLOCKS",
                    "MUSIC_DISC_CHIRP", "MUSIC_DISC_FAR", "MUSIC_DISC_MALL",
                    "MUSIC_DISC_MELLOHI", "MUSIC_DISC_STAL", "MUSIC_DISC_STRAD",
                    "MUSIC_DISC_WARD", "MUSIC_DISC_11", "MUSIC_DISC_WAIT", "MUSIC_DISC_PIGSTEP"
            };

            int randomIndex = (int) (Math.random() * discs.length);
            Sound sound = Sound.valueOf(discs[randomIndex]);

            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            player.sendMessage("§aVous écoutez maintenant une musique aléatoire !");
        }

        public static ItemStack createPortableJukebox() {
            ItemStack jukebox = new ItemStack(Material.JUKEBOX);
            ItemMeta meta = jukebox.getItemMeta();
            meta.setDisplayName("§6Jukebox Portatif");
            jukebox.setItemMeta(meta);
            return jukebox;
        }
    }
    @EventHandler
    public void onTridentLightningStrike(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Trident trident && trident.getShooter() instanceof Player player) {
            if (trident.getItemStack().getEnchantmentLevel(Enchantment.CHANNELING) > 0) {
                // Réduire les dégâts de la foudre
                Entity struckEntity = event.getHitEntity();
                if (struckEntity != null && struckEntity instanceof LivingEntity) {
                    double originalDamage = 8.0; // Valeur originale de dégâts de la foudre
                    double reducedDamage = originalDamage * 0.5; // Réduction des dégâts de 50%
                    ((LivingEntity) struckEntity).damage(reducedDamage);
                }
                // Appliquer un cooldown de 10 secondes
                long cooldownTime = System.currentTimeMillis() + 10000; // 10 secondes
                player.setCooldown(trident.getItemStack().getType(), 10000);
            }
        }
    }

    @EventHandler
    public void onShieldBlock(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player && event.getDamager() instanceof Player) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.SHIELD) {
                long lastBlockTime = (long) (player.getLastDamage() - 1000); // Le temps entre les utilisations du bouclier
                if (System.currentTimeMillis() - lastBlockTime < 1000) {
                    event.setCancelled(true); // Annuler l'attaque si trop rapide
                }
            }
        }
    }

    @EventHandler
    public void onBaneOfArthropodsHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && event.getEntity() instanceof LivingEntity) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.containsEnchantment(Enchantment.BANE_OF_ARTHROPODS)) {
                LivingEntity target = (LivingEntity) event.getEntity();
                // Appliquer un ralentissement à la cible
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1)); // 60 ticks de ralentissement
            }
        }
    }



    @EventHandler
    public void onPlayerInteractssss(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.hasItemMeta() && item.getItemMeta().getDisplayName().equals("§6Jukebox Portatif")) {
            Jukeboxmod.playCustomMusic(player);
        }
    }

    // Intercepter l'événement où un joueur essaie d'utiliser des Élytres
    @EventHandler
    public void onPlayerFly(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        // Vérifier si le joueur est dans l'End et si l'utilisation des Élytres est désactivée
        if (player.getWorld().getName().equals("world_end") && !elytraEnabledInEnd) {
            if (player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType() == Material.ELYTRA) {
                event.setCancelled(true);
                player.sendMessage("§cLes élytres sont désactivées dans l'End.");
            }
        }
    }

    @EventHandler
    public void onRedstoneActivate(BlockRedstoneEvent event) {
        if (event.getBlock().getType() != Material.REDSTONE_WIRE) return;
        UUID worldUUID = event.getBlock().getWorld().getUID();
        long currentTime = System.currentTimeMillis();

        // Vérifier la dernière activation
        long lastTime = lastActivation.getOrDefault(worldUUID, 0L);
        if (currentTime - lastTime < 200) { // 200ms = 4 ticks environ
            incrementActivationCount(worldUUID, event);
        } else {
            redstoneActivations.put(worldUUID, 0); // Réinitialiser le compteur
        }
        lastActivation.put(worldUUID, currentTime);
    }

    private void incrementActivationCount(UUID worldUUID, BlockRedstoneEvent event) {
        int count = redstoneActivations.getOrDefault(worldUUID, 0) + 1;
        redstoneActivations.put(worldUUID, count);

        if (count >= 10) { // Limite de 10 activations rapides
            event.setNewCurrent(0); // Désactiver temporairement la redstone
            sendClockWarning();
        }
    }

    private void sendClockWarning() {
        if (messagesConfig.contains("clock-warning")) {
            Bukkit.broadcastMessage(messagesConfig.getString("messages"));
        }
    }

    @EventHandler
    public void onBlockBreakss(BlockBreakEvent event) {
        Material type = event.getBlock().getType();
        Player player = event.getPlayer();
        String playerName = player.getName();

        List<Material> trackedOres = Arrays.asList(Material.DIAMOND_ORE, Material.GOLD_ORE, Material.IRON_ORE,Material.COPPER_ORE, Material.ANCIENT_DEBRIS, Material.EMERALD_ORE,Material.DEEPSLATE_IRON_ORE,Material.DEEPSLATE_COPPER_ORE,Material.DEEPSLATE_COAL_ORE,Material.DEEPSLATE_GOLD_ORE,Material.DEEPSLATE_EMERALD_ORE,Material.LAPIS_ORE,Material.DEEPSLATE_LAPIS_ORE ,Material.DEEPSLATE_DIAMOND_ORE);
        if (trackedOres.contains(type)) {
            int count = topluckConfig.getInt("players." + playerName + "." + type.name(), 0) + 1;
            topluckConfig.set("players." + playerName + "." + type.name(), count);
            saveTopLuckConfig();
        }
    }

    @EventHandler
    public void onPlayerBlock(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.isBlocking()) {
                event.setCancelled(true); // Annule les dégâts
                player.sendMessage(ChatColor.GREEN + "Parade réussie ! Vous contre-attaquez !");
                if (event.getDamager() instanceof Player) {
                    ((Player) event.getDamager()).damage(4.0); // Inflige 2 cœurs de dégâts à l'attaquant
                }
            }
        }
    }

    @EventHandler
    public void onWeatherChangesss(WeatherChangeEvent event) {
        World world = event.getWorld();
        Random random = new Random();
        if (event.toWeatherState()) { // Déclenche un événement météo aléatoire si la pluie commence
            int chance = random.nextInt(100);
            if (chance < 30) { // 30% de chance d'avoir un effet spécial
                if (chance < 15) {
                    Bukkit.broadcastMessage(ChatColor.RED + "Une tempête a renforcé les monstres !");
                    world.setThunderDuration(6000);
                } else {
                    Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "Une pluie acide commence !");
                    for (Player player : world.getPlayers()) {
                        if (!player.isInsideVehicle()) {
                            player.damage(1.0); // Endommage légèrement les joueurs exposés
                        }
                    }
                }
            }
        }

    }




    private void saveTopLuckConfig() {
        try {
            topluckConfig.save(new File(getDataFolder(), "TopLuck.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onEntityDamagesss(EntityDamageByEntityEvent event) {
        // Assurez-vous que l'entité attaquée est bien un joueur ou un autre type d'entité que vous souhaitez gérer
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            ItemStack armorPiece = entity.getEquipment().getHelmet();  // Exemple avec le casque, répétez pour les autres pièces d'armure

            // Vérifiez que l'armorPiece n'est pas null avant de l'utiliser
            if (armorPiece != null && armorPiece.containsEnchantment(Enchantment.PROTECTION)) {
                // Traitement de l'enchantement ou autre logique ici
            }
        }
    }

    public static ItemStack createHeroArmor() {
        ItemStack armor = new ItemStack(Material.GOLDEN_CHESTPLATE);
        ItemMeta meta = armor.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Armure du Héros de la Lumière");
            meta.setLore(Arrays.asList(ChatColor.YELLOW + "Réduit les dégâts de foudre", ChatColor.YELLOW + "Augmente la vitesse en plein jour"));
            meta.addEnchant(Enchantment.PROTECTION, 4, true);
            armor.setItemMeta(meta);
        }
        return armor;
    }

    public static ItemStack createHeroSword() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Épée Divine");
            meta.setLore(Arrays.asList(ChatColor.YELLOW + "Fait reculer les ennemis", ChatColor.YELLOW + "Dégâts accrus contre les mobs du Nether"));
            meta.addEnchant(Enchantment.KNOCKBACK, 2, true);
            meta.addEnchant(Enchantment.SHARPNESS, 5, true);
            sword.setItemMeta(meta);
        }
        return sword;
    }

    public static ItemStack createAssassinArmor() {
        ItemStack armor = new ItemStack(Material.NETHERITE_CHESTPLATE);
        ItemMeta meta = armor.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_RED + "Armure de l'Assassin du Chaos");
            meta.setLore(Arrays.asList(ChatColor.RED + "Annule les dégâts de chute", ChatColor.RED + "Rend furtif en restant immobile"));
            meta.addEnchant(Enchantment.FEATHER_FALLING, 4, true);
            armor.setItemMeta(meta);
        }
        return armor;
    }

    public static ItemStack createAssassinDagger() {
        ItemStack dagger = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = dagger.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_RED + "Dague Empoisonnée");
            meta.setLore(Arrays.asList(ChatColor.RED + "Inflige des dégâts sur la durée", ChatColor.RED + "Ralentit la cible"));
            meta.addEnchant(Enchantment.SHARPNESS, 4, true);
            meta.addEnchant(Enchantment.FEATHER_FALLING, 1, true);
            dagger.setItemMeta(meta);
        }
        return dagger;
    }

    public static ItemStack createJudgmentBlade() {
        ItemStack blade = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = blade.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GRAY + "Lame du Jugement");
            meta.setLore(Arrays.asList(ChatColor.WHITE + "Change de puissance selon la réputation"));
            meta.addEnchant(Enchantment.SHARPNESS, 5, true);
            blade.setItemMeta(meta);
        }
        return blade;
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item != null && item.hasItemMeta()) {
            if (item.getItemMeta().getDisplayName().contains("Lame du Jugement")) {
                int reputation = getReputation(player);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setLore(Arrays.asList(ChatColor.WHITE + "Réputation actuelle: " + reputation));
                    meta.addEnchant(Enchantment.SHARPNESS, Math.max(1, reputation / 20), true);
                    item.setItemMeta(meta);
                }
            }
        }
    }

    private void startSeasonCycle() {
        new BukkitRunnable() {
            @Override
            public void run() {
                switchSeason();
            }
        }.runTaskTimer(this, seasonDuration, seasonDuration);
    }

    private void switchSeason() {
        switch (currentSeason) {
            case SPRING -> currentSeason = Season.SUMMER;
            case SUMMER -> currentSeason = Season.AUTUMN;
            case AUTUMN -> currentSeason = Season.WINTER;
            case WINTER -> currentSeason = Season.SPRING;
        }
        Bukkit.broadcastMessage(ChatColor.AQUA + "§7[§e?§7]§a La saison change ! Nous sommes maintenant en " + ChatColor.BOLD + currentSeason);
        applySeasonEffects();
    }

    private void applySeasonEffects() {
        World world = Bukkit.getWorlds().get(0);
        switch (currentSeason) {
            case SPRING -> world.setStorm(false);
            case SUMMER -> world.setStorm(random.nextBoolean()); // Parfois orageux
            case AUTUMN -> world.setStorm(true);
            case WINTER -> world.setStorm(true);
        }
    }

    private void startDiseaseSpread() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (infectedPlayers.containsKey(player.getUniqueId())) {
                        applyDiseaseEffects(player);
                    } else if (random.nextInt(100) < 5) { // 5% de chance de tomber malade
                        infectPlayer(player);
                    }
                }
            }
        }.runTaskTimer(this, 200, 200);
    }

    private void infectPlayer(Player player) {
        infectedPlayers.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous vous sentez malade... ");
    }

    private void applyDiseaseEffects(Player player) {
        player.sendMessage(ChatColor.DARK_RED + "§7[§e?§7]§c Vous êtes malade ! §aTrouvez un remède...");
        player.setFoodLevel(player.getFoodLevel() - 1); // Affame progressivement
    }

    @EventHandler
    public void onPlayerMovess(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (infectedPlayers.containsKey(player.getUniqueId())) {
            for (Player nearby : player.getNearbyEntities(3, 3, 3).stream().filter(e -> e instanceof Player).map(e -> (Player) e).toList()) {
                if (!infectedPlayers.containsKey(nearby.getUniqueId()) && random.nextInt(100) < 20) {
                    infectPlayer(nearby);
                }
            }
        }
    }

    public void startVolcanicEruption(Location location) {
        // Crée un volcan dans un biome montagne
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                // Créer une explosion de lave
                for (int i = 0; i < 10; i++) {
                    Location eruptionLocation = location.add(new Random().nextInt(10), 0, new Random().nextInt(10));
                    Block block = eruptionLocation.getBlock();
                    block.setType(Material.LAVA);
                    block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 0);
                }
            }
        }, 0, 200); // Exécution toutes les 10 secondes
    }

    // Système de Zones Radioactives
    @EventHandler
    public void onPlayerEnterRadioactiveZone(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();

        if (isInRadioactiveZone(playerLocation)) {
            // Dégâts progressifs
            player.damage(1);
            player.sendMessage(ChatColor.RED + "§7[§e?§7]§a Vous êtes dans une zone radioactive! Restez prudent!");
        }
    }

    private boolean isInRadioactiveZone(Location location) {
        // Définir une zone aléatoire et vérifier si un joueur est dedans
        double x = location.getX();
        double z = location.getZ();
        return (x > 1000 && x < 2000) && (z > 1000 && z < 2000); // Exemple de zone
    }

    // Système d'Attaques de Météores
    public void startMeteorShower() {
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                // Génère un météore aléatoire
                double x = new Random().nextInt(3000) - 1500;
                double z = new Random().nextInt(3000) - 1500;
                Location meteorLocation = new Location(Bukkit.getWorld("world"), x, 100, z);
                meteorShowerImpact(meteorLocation);
            }
        }, 0, 6000); // Tous les 5 minutes
    }

    private void meteorShowerImpact(Location location) {
        // Crée un "impact" de météore
        location.getWorld().createExplosion(location, 4F, false, false);
        location.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, location, 50);

        // Applique des dégâts dans la zone de l'impact
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) < 20) {
                player.damage(5); // Dégâts d'impact
                player.sendMessage(ChatColor.GOLD + "§7[§e?§7]§c Un météore est tombé près de vous!");
            }
        }
    }

    // Déclenchement des événements au hasard
    public void triggerRandomEvents() {
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                int eventChoice = new Random().nextInt(3);
                if (eventChoice == 0) {
                    startVolcanicEruption(new Location(Bukkit.getWorld("world"), new Random().nextInt(1000), 70, new Random().nextInt(1000)));
                } else if (eventChoice == 1) {
                    startMeteorShower();
                }
            }
        }, 0, 12000); // Tous les 10 minutes
    }



         // Tous les 20 secondes (400 ticks)
         public void applyProtection(Player player) {
             long cooldownTime = 30000;  // 30 secondes de protection

             // Si le joueur n'a pas de protection active, on lui applique
             if (!protectionCooldowns.containsKey(player) || protectionCooldowns.get(player) <= System.currentTimeMillis()) {
                 player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 4, true, false));
                 protectionCooldowns.put(player, System.currentTimeMillis() + cooldownTime);

                 player.sendMessage("§7[§e?§7]§a Vous êtes maintenant protégé contre certaines attaques pendant 30 secondes.");
             } else {
                 long remainingTime = (protectionCooldowns.get(player) - System.currentTimeMillis()) / 1000;
                 player.sendMessage("§7[§e?§7]§a Votre protection est encore active pendant " + remainingTime + " secondes.");
             }
         }

    // Gestion de l'activation du sort avec un clic droit
    @EventHandler
    public void onPlayerUseSpell(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction().name().contains("RIGHT_CLICK")) {
            // Vérifier si le joueur a un item spécifique pour activer le sort (ex. un bâton)
            if (player.getInventory().getItemInMainHand().getType().toString().equals("BLAZE_ROD")) {
                applyProtection(player);
            }
        }
    }

    // Annuler les dégâts si le joueur est protégé
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // Si le joueur a une protection active et qu'il reçoit des dégâts spécifiques
            if (protectionCooldowns.containsKey(player) && protectionCooldowns.get(player) > System.currentTimeMillis()) {
                // Bloquer certains types de dégâts spécifiques
                if (event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                        event.getCause() == EntityDamageEvent.DamageCause.LAVA ||
                        event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                        event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {

                    event.setCancelled(true);
                    player.sendMessage("§7[§e?§7]§a Votre protection a bloqué les dégâts.");
                }
            }
        }
    }

    public class AdvancedForge {

    public void openForgeUI(Player player) {
        Inventory forgeInventory = instance.getInstance().getServer().createInventory(null, 9, "Forge Avancée");

        // Ajouter des éléments de forge (exemple : minerais et pierres magiques)
        forgeInventory.setItem(0, new ItemStack(Material.DIAMOND));
        forgeInventory.setItem(1, new ItemStack(Material.GLOWSTONE));
        forgeInventory.setItem(4, new ItemStack(Material.ANVIL)); // Zone centrale pour personnalisation

        // Ouvrir l'interface
        player.openInventory(forgeInventory);
    }

    // Méthode pour personnaliser un équipement
    public void customizeEquipment(Player player, ItemStack item) {
        if (item != null && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            // Exemple : ajouter un enchantement spécifique
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);

            // Exemple : ajouter un effet de potion à l'équipement

            // Exemple : changer le nom de l'objet
            meta.setDisplayName("Épée de Forge Magique");

            // Appliquer les modifications à l'équipement
            item.setItemMeta(meta);

            player.sendMessage("Votre équipement a été personnalisé avec succès !");
        } else {
            player.sendMessage("Cet objet ne peut pas être personnalisé.");
        }
    }

    // Gérer l'événement d'interaction avec l'interface de la forge
    public void handleForgeInteraction(Player player, Inventory inventory) {
        ItemStack item = inventory.getItem(4); // Zone centrale où l'objet est placé

        // Si un objet est placé dans la zone centrale, procéder à la personnalisation
        if (item != null) {
            customizeEquipment(player, item);
        } else {
            player.sendMessage("Placez un équipement dans la forge pour le personnaliser.");
        }
    }}

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction().name().contains("RIGHT_CLICK")) {
            if (player.getInventory().getItemInMainHand().getType().equals(Material.BLAZE_ROD)) {
                // Ouvrir l'interface de forge
                forge.openForgeUI(player);
            }
        }
    }

    // Gérer les interactions dans l'interface de la forge
    @EventHandler
    public void onInventoryClickssssssssss(org.bukkit.event.inventory.InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getContents().equals("Forge Avancée")) {
            Player player = (Player) event.getWhoClicked();

            if (event.getSlot() == 4) { // Si on clique dans la zone centrale
                forge.handleForgeInteraction(player, inventory);
            }
        }
    }

    public class CelestialArtifact {

        private String name;
        private String description;
        private ItemStack artifactItem;
        private CelestialPower power;

        public CelestialArtifact(String name, String description, Material material, CelestialPower power) {
            this.name = name;
            this.description = description;
            this.artifactItem = new ItemStack(material);
            this.power = power;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public ItemStack getArtifactItem() {
            return artifactItem;
        }

        public void activatePower(Player player) {
            power.activate(player);
        }

        // Enum pour les pouvoirs des artefacts célestes
        public enum CelestialPower {
            // Exemple de pouvoirs d'artefacts célestes
            STELLAR_SHIELD {
                @Override
                public void activate(Player player) {
                    player.sendMessage("Un bouclier stellaire vous protège !");
                    // Ajoute un effet de bouclier à un joueur
                    player.setNoDamageTicks(100);  // Bouclier temporaire
                }
            },
            COSMIC_FLIGHT {
                @Override
                public void activate(Player player) {
                    player.sendMessage("Vous avez été imbibé de pouvoirs cosmiques, vous pouvez voler !");
                    // Permet au joueur de voler
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }
            };

            public abstract void activate(Player player);
        }


}

    public class GiveCelestialArtifactCommand implements CommandExecutor {

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                // Création de l'artefact céleste (exemple d'artefact)
                CelestialArtifact artifact = new CelestialArtifact(
                        "Artefact de l'Étoile Filante",  // Nom de l'artefact
                        "Un artefact mystique qui vous confère un bouclier stellaire.",  // Description
                        Material.NETHER_STAR,  // Type d'objet (exemple : une étoile du Nether)
                        CelestialArtifact.CelestialPower.STELLAR_SHIELD  // Pouvoir de l'artefact
                );

                // Donner l'artefact au joueur
                player.getInventory().addItem(artifact.getArtifactItem());
                player.sendMessage("Vous avez reçu l'artefact céleste : " + artifact.getName());
            } else {
                sender.sendMessage("Seuls les joueurs peuvent exécuter cette commande.");
            }
            return true;
        }
        @EventHandler
        public void onEclipse(WeatherChangeEvent event) {
            if (event.toWeatherState()) {
                // Si une éclipse commence, tous les artefacts célestes deviennent plus puissants
                Bukkit.getOnlinePlayers().forEach(player -> {
                    CelestialArtifact artifact = getArtifact(player);
                    if (artifact != null) {
                        artifact.activatePower(player);  // Active le pouvoir de l'artefact pendant l'éclipse
                        player.sendMessage("L'éclipse lunaire renforce votre artefact céleste !");
                    }
                });
            }
        }

        // Méthode pour obtenir un artefact d'un joueur (à implémenter selon ton système)
        private CelestialArtifact getArtifact(Player player) {
            // Implémenter la logique pour récupérer un artefact donné à un joueur
            return null;  // À remplacer par un système réel de stockage d'artefacts
        }
}



        // Protéger les villageois des attaques de mobs
        @EventHandler
        public void onEntityDamagessss(EntityDamageByEntityEvent event) {
            Entity entity = event.getEntity();
            Entity damager = event.getDamager();

            if (entity instanceof Villager && damager instanceof Monster) {
                event.setCancelled(true);  // Annuler l'attaque pour protéger le villageois
            }
        }

        // Récompenser les joueurs qui défendent le village
        @EventHandler
        public void onMonsterKill(EntityDeathEvent event) {
            Entity entity = event.getEntity();
            if (entity instanceof Monster) {
                Player closestPlayer = getClosestPlayer(entity.getLocation());
                if (closestPlayer != null) {
                    closestPlayer.getInventory().addItem(new ItemStack(Material.DIAMOND)); // Récompense
                    closestPlayer.sendMessage("Vous avez protégé un village en tuant un monstre !");
                }
            }
        }

        // Lorsque les joueurs interagissent avec les villageois, créer un système d'alerte
        @EventHandler
        public void onPlayerInteractWithVillager(PlayerInteractEntityEvent event) {
            if (event.getRightClicked() instanceof Villager) {
                Player player = event.getPlayer();
                player.sendMessage("Alerte : Un village est sous menace ! Préparez-vous à défendre !");
                // D'autres actions, comme invoquer des défenseurs ou déclencher des pièges.
            }
        }

        // Méthode pour obtenir le joueur le plus proche d'un monstre
        private Player getClosestPlayer(org.bukkit.Location location) {
            double closestDistance = Double.MAX_VALUE;
            Player closestPlayer = null;
            for (Player player : Bukkit.getOnlinePlayers()) {
                double distance = player.getLocation().distance(location);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPlayer = player;
                }
            }
            return closestPlayer;
        }





        public void generateSurface(World world, Random random, int chunkX, int chunkZ, Block[][] blocks) {
            // Génération du terrain du biome volcanique
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int maxY = world.getMaxHeight();
                    for (int y = 0; y < maxY; y++) {
                        Block block = blocks [x][y];
                        if (y < 20) {
                            block.setType(Material.LAVA);  // Lava surface
                        } else if (y < 30) {
                            block.setType(Material.NETHERRACK);  // Base of the volcano
                        } else {
                            block.setType(Material.STONE);  // Regular ground
                        }
                    }
                }
            }
        }




        // Ajout d'un événement d'éruption de temps en temps
        public void triggerEruption(World world) {
            Random random = new Random();
            int centerX = random.nextInt(1000);
            int centerZ = random.nextInt(1000);

            // Créer une éruption dans une région spécifique
            for (int x = centerX - 10; x < centerX + 10; x++) {
                for (int z = centerZ - 10; z < centerZ + 10; z++) {
                    int y = world.getHighestBlockYAt(x, z);
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(Material.LAVA);
                }
            }
            // Annonce d'éruption
            world.getPlayers().forEach(player -> player.sendMessage("Une éruption volcanique est en cours à " + centerX + ", " + centerZ));
        }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (random.nextInt(1000) < 5) { // 0.5% de chance de générer un temple par chunk
            generateTemple(event.getChunk());
        }

    }



    private void generateTemple(Chunk chunk) {
        World world = chunk.getWorld();
        int x = chunk.getX() * 16 + random.nextInt(16);
        int z = chunk.getZ() * 16 + random.nextInt(16);
        int y = world.getHighestBlockYAt(x, z);

        Location templeLocation = new Location(world, x, y, z);
        world.getBlockAt(templeLocation).setType(Material.STONE_BRICKS);

        Bukkit.getLogger().info("Un Temple Ancien est apparu en " + x + ", " + y + ", " + z);
    }
    public  void placeCelestialArtifact(World world, int x, int y, int z) {
        Block chestBlock = world.getBlockAt(x, y, z);
        chestBlock.setType(Material.CHEST);

        if (chestBlock.getState() instanceof Chest chest) {
            CelestialArtifact artifact = getRandomArtifact();
            chest.getBlockInventory().addItem(artifact.getArtifactItem());
        }
    }

    private  CelestialArtifact getRandomArtifact() {
        CelestialArtifact.CelestialPower power = CelestialArtifact.CelestialPower.values()[random.nextInt(CelestialArtifact.CelestialPower.values().length)];
        return new CelestialArtifact("Artefact Divin", "Un pouvoir mystérieux des anciens dieux.", Material.NETHER_STAR, power);
    }

    public class DivinePowerManager {

        public static void activatePower(Player player, CelestialArtifact artifact) {
            switch (artifact.getName()) {
                case "Œil du Ciel" -> {
                    player.sendMessage("Vous avez reçu la vision des dieux !");
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 6000, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 1));
                }
                case "Flamme Sacrée" -> {
                    player.sendMessage("Votre force embrase vos ennemis !");
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 6000, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 6000, 1));
                }
                case "Sérénité Astrale" -> {
                    player.sendMessage("Vous ressentez l'harmonie cosmique.");
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 6000, 2));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6000, 2));
                }
            }
        }
    }




        public static void spawnBoss(Location location) {
            World world = location.getWorld();
            if (world == null) return;

            Wither boss = (Wither) world.spawnEntity(location, EntityType.WITHER);
            boss.setCustomName("👁️ L’Ombre Éternelle");
            boss.setCustomNameVisible(true);
            boss.setMaxHealth(500);
            boss.setHealth(500);
            boss.setMetadata("LOMBRE_ETERNELLE", new FixedMetadataValue(WizardStoneCraft.getInstance(), true));
            world.strikeLightningEffect(location);

            // Ajoute un effet visuel
            new BukkitRunnable() {
                int time = 0;

                @Override
                public void run() {
                    if (boss.isDead()) {
                        cancel();
                        return;
                    }

                    // Particules d’ombre
                    boss.getWorld().spawnParticle(Particle.SMOKE, boss.getLocation(), 20, 1, 1, 1, 0);
                    boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.0f, 0.5f);

                    // Phase 2 : Plus rapide
                    if (boss.getHealth() < 250 && time % 10 == 0) {
                        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2));
                        boss.getWorld().strikeLightningEffect(boss.getLocation());
                    }

                    // Phase 3 : Éclipse
                    if (boss.getHealth() < 100 && time % 20 == 0) {
                        boss.getWorld().setStorm(true);
                        boss.getWorld().setThundering(true);
                    }

                    time++;
                }
            }.runTaskTimer(WizardStoneCraft.getInstance(), 0L, 20L);
        }

        @EventHandler
        public void onBossHits(EntityDamageByEntityEvent event) {
            if (!(event.getEntity() instanceof Wither boss)) return;
            if (!boss.hasMetadata("LOMBRE_ETERNELLE")) return;

            // Onde d’Ombre (chance de 20%)
            if (random.nextInt(100) < 20) {
                for (Entity entity : boss.getNearbyEntities(5, 5, 5)) {
                    if (entity instanceof Player player) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                        player.damage(5);
                    }
                }
            }
        }

        @EventHandler
        public void onBossTargets(EntityTargetLivingEntityEvent event) {
            if (!(event.getEntity() instanceof Wither boss)) return;
            if (!boss.hasMetadata("LOMBRE_ETERNELLE")) return;

            // Fissure Obscure (chance de 30%)
            if (random.nextInt(100) < 30) {
                Location loc = event.getTarget().getLocation();
                loc.getWorld().spawnEntity(loc, EntityType.WITHER_SKELETON);
                loc.getWorld().spawnParticle(Particle.WITCH, loc, 30, 1, 1, 1, 0.5);
            }
        }

        @EventHandler
        public void onBossDeaths(EntityDeathEvent event) {
            if (!(event.getEntity() instanceof Wither boss)) return;
            if (!boss.hasMetadata("LOMBRE_ETERNELLE")) return;

            boss.getWorld().strikeLightningEffect(boss.getLocation());
            boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
            boss.getWorld().dropItemNaturally(boss.getLocation(), new ItemStack(Material.NETHER_STAR, 1));
        }




    @EventHandler
    public void onBossHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Wither boss)) return;
        if (!boss.hasMetadata("LOMBRE_ETERNELLE")) return;

        // Onde d’Ombre (chance de 20%)
        if (random.nextInt(100) < 20) {
            for (Entity entity : boss.getNearbyEntities(5, 5, 5)) {
                if (entity instanceof Player player) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1));
                    player.damage(5);
                }
            }
        }
    }

    @EventHandler
    public void onBossTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Wither boss)) return;
        if (!boss.hasMetadata("LOMBRE_ETERNELLE")) return;

        // Fissure Obscure (chance de 30%)
        if (random.nextInt(100) < 30) {
            Location loc = event.getTarget().getLocation();
            loc.getWorld().spawnEntity(loc, EntityType.WITHER_SKELETON);
            loc.getWorld().spawnParticle(Particle.WITCH, loc, 30, 1, 1, 1, 0.5);
        }
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Wither boss)) return;
        if (!boss.hasMetadata("LOMBRE_ETERNELLE")) return;

        boss.getWorld().strikeLightningEffect(boss.getLocation());
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
        boss.getWorld().dropItemNaturally(boss.getLocation(), new ItemStack(Material.NETHER_STAR, 1));
    }

}












