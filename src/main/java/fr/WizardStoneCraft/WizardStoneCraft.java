package fr.WizardStoneCraft;



import com.earth2me.essentials.commands.EssentialsCommand;
import fr.WizardStoneCraft.Commands.*;
import fr.WizardStoneCraft.PlaceHolderApi.PlaceHolderApi;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
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
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static fr.WizardStoneCraft.WizardStoneCraft.repspawnnpc.isPassive;
import static org.bukkit.Bukkit.getPlayer;
import static org.bukkit.Bukkit.getPlayerUniqueId;


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
    private int pointsKills;
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
    private FileConfiguration topluckConfig;


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
        loadMessages();
        loadMessages();
        loadConfig();
        loadMessagese();
        loadMessagesConfig();
        loadTopLuckConfig();
        startMessageTask();
        config = getConfig();
        dropChance = config.getDouble("drop-chance", 50.0) / 100.0;
        PlaceHolderApi.checkPlaceholderAPI();
        WizardStoneCraft plugins = this;
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        setupTeams();
        updateDailyDeals();
        maxCraftsPerDay = getConfig().getInt("craft_limit", 50);
        craftRadius = config.getInt("craft_radius", 5);
        atmAmount = config.getInt("atm_amount", 100);
        atmMessage = config.getString("atm_message", "Vous avez retiré %amount% pièces depuis l'ATM !");
        disableThorns = getConfig().getBoolean("disable_thorns.enabled", true);
        onlyPvp = getConfig().getBoolean("disable_thorns.only_pvp", true);
        mobsAffected = getConfig().getBoolean("disable_thorns.mobs_affected", false);


        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("repadd").setExecutor(new ManageRepCommand());
        getCommand("Claimoffspawnmob").setExecutor(new claimoffmobspawn());
        getCommand("reptop").setExecutor(new ReptopCommand());
        getCommand("rep").setExecutor(new ReputationCommand());
        getCommand("rephighlight").setExecutor(new RepHighlightCommand());
        getCommand("rephelp").setExecutor(new RepHelpCommand());
        getCommand("repreload").setExecutor(new RepReloadCommand(this));
        getCommand("Broadcast").setExecutor(new Broadcast());
        getCommand("tabreload").setExecutor(new UpdateTablistCommand(this));
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
        //tab updater
        new TablistUpdater(this).runTaskTimer(this, 10, 10);

         // Vérifie bien que la valeur est chargée
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




public class repspawnnpc implements CommandExecutor{
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("repspawnnpc")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                // Vérifier si le joueur a la permission d'exécuter cette commande
                if (player.hasPermission("wizardstonecraft.repspawnnpc")) {
                    // Invoquer le NPC "Pacificateur" à la position du joueur
                    spawnPacificateur(player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "§7[§e?§7]§a Le Pacificateur a été invoqué à votre position §7[§c!§7]");
                } else {
                    player.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous n'avez pas la permission d'exécuter cette commande. §7[§c!§7]");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "§7[§e?§7]§e Cette commande ne peut être exécutée que par un joueur. §7[§c!§7]");
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

    public class CookFoodCommand implements CommandExecutor {

        private static final int REQUIRED_REPUTATION = 10;
        private static final Map<Material, Material> COOKABLE_FOODS = new HashMap<>();

        static {
            COOKABLE_FOODS.put(Material.LEGACY_RAW_BEEF, Material.COOKED_BEEF);
            COOKABLE_FOODS.put(Material.LEGACY_RAW_CHICKEN, Material.COOKED_CHICKEN);
            COOKABLE_FOODS.put(Material.PORKCHOP, Material.COOKED_PORKCHOP);
            COOKABLE_FOODS.put(Material.SALMON, Material.COOKED_SALMON);
            COOKABLE_FOODS.put(Material.COD, Material.COOKED_COD);
            COOKABLE_FOODS.put(Material.RABBIT, Material.COOKED_RABBIT);
            COOKABLE_FOODS.put(Material.MUTTON, Material.COOKED_MUTTON);
            COOKABLE_FOODS.put(Material.POTATO, Material.POTATOES);
        }

        static {
            COOKABLE_FOODS.put(Material.BEEF, Material.COOKED_BEEF);
            COOKABLE_FOODS.put(Material.CHICKEN, Material.COOKED_CHICKEN);
            COOKABLE_FOODS.put(Material.PORKCHOP, Material.COOKED_PORKCHOP);
            COOKABLE_FOODS.put(Material.MUTTON, Material.COOKED_MUTTON);
            COOKABLE_FOODS.put(Material.RABBIT, Material.COOKED_RABBIT);
            COOKABLE_FOODS.put(Material.SALMON, Material.COOKED_SALMON);
            COOKABLE_FOODS.put(Material.COD, Material.COOKED_COD);
            COOKABLE_FOODS.put(Material.POTATO, Material.POTATOES);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Seuls les joueurs peuvent exécuter cette commande !");
                return true;
            }

            Player player = (Player) sender;

            // Vérifie la réputation du joueur (remplace getReputation avec ta propre méthode)
            int reputation = getReputation(player);
            if (reputation < 10) {
                player.sendMessage(ChatColor.RED + "§7[§e?§7]§2 Vous devez avoir au moins 10 de réputation pour cuire votre nourriture !");
                return true;
            }

            // Vérifie et cuit la nourriture
            boolean foundFood = false;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && COOKABLE_FOODS.containsKey(item.getType())) {
                    Material cooked = COOKABLE_FOODS.get(item.getType());
                    item.setType(cooked);
                    foundFood = true;
                }
            }

            if (foundFood) {
                player.sendMessage(ChatColor.GREEN + "§7[§e?§7]§a Votre nourriture a été cuite !");
            } else {
                player.sendMessage(ChatColor.YELLOW + "§7[§e?§7]§e Vous n'avez pas de nourriture crue à cuire.");
            }

            return true;
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




public class Topluck implements CommandExecutor{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seuls les joueurs peuvent exécuter cette commande.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("anticheat.topluck")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        if (topluckConfig.getConfigurationSection("players") == null) {
            player.sendMessage("§cAucun joueur n'a encore miné de minerai.");
            return true;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Top Luck");

        for (String playerName : topluckConfig.getConfigurationSection("players").getKeys(false)) {
            int totalMined = topluckConfig.getConfigurationSection("players." + playerName).getValues(false)
                    .values().stream().mapToInt(value -> (int) value).sum();
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = skull.getItemMeta();
            meta.setDisplayName("§6" + playerName + " : " + totalMined + " minerais");
            skull.setItemMeta(meta);
            inv.addItem(skull);
        }

        player.openInventory(inv);
        return true;
    }
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




    public class ClaimWeatherControl implements CommandExecutor{
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Seuls les joueurs peuvent exécuter cette commande.");
                return true;
            }

            Player player = (Player) sender;
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, null);

            if (claim == null) {
                player.sendMessage(ChatColor.RED + "Vous devez être dans un claim pour modifier la météo.");
                return true;
            }

            if (!claim.getOwnerID().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Seul le propriétaire du claim peut modifier la météo.");
                return true;
            }

            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Usage: /claimmeteo <soleil|pluie|naturel>");
                return true;
            }

            World world = player.getWorld();
            switch (args[0].toLowerCase()) {
                case "soleil":
                    world.setStorm(false);
                    world.setThundering(false);
                    player.sendMessage(ChatColor.GREEN + "Météo du claim réglée sur Soleil.");
                    break;
                case "pluie":
                    world.setStorm(true);
                    player.sendMessage(ChatColor.GREEN + "Météo du claim réglée sur Pluie.");
                    break;
                case "naturel":
                    player.sendMessage(ChatColor.GREEN + "Météo du claim rétablie sur celle du monde.");
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Option invalide. Utilisez: soleil, pluie ou naturel.");
                    return true;
            }
            return true;
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
                            "§7- §dΩ Honorable§F = 100§7\n" +
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
                        sender.sendMessage( offlinePlayer.getName() + ": " + prefix + "§7 " + reps + " points de Réputation §7[§c!§7]");
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
                sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande. §7[§c!§7]");
                return true;
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.updateTablist(player);
            }

            sender.sendMessage(ChatColor.GREEN + "La tablist a été mise à jour pour tous les joueurs en ligne. §7[§c!§7]");
            return true;
        }
    }

//event
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
        player.sendMessage("§7[§e?§7] §aVous êtes protégé pendant 90 secondes après votre connexion §7[§c!§7]");

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
    public class TradeClaimCommand implements CommandExecutor{
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "§7[§e?§7] Seuls les joueurs peuvent utiliser cette commande.");
                return true;
            }

            Player player = (Player) sender;
            PlayerData playerData = griefPrevention.dataStore.getPlayerData(player.getUniqueId());
            int claimBlocks = playerData.getAccruedClaimBlocks();
            int tradeAmount = 1000; // Nombre de blocks échangés

            if (claimBlocks < tradeAmount) {
                player.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous n'avez pas assez de blocks de claim pour cet échange.");
                return true;
            }

            // Retirer les blocks de claim et donner des émeraudes
            playerData.setAccruedClaimBlocks(claimBlocks - tradeAmount);
            player.getInventory().addItem(new ItemStack(Material.EMERALD, 10)); // 10 émeraudes en échange

            player.sendMessage(ChatColor.GREEN + "§7[§e?§7]§a Vous avez échangé " + tradeAmount + " blocks de claim contre 10 émeraudes !");
            return true;
        }

        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();
            World world = player.getWorld();

            if (world.getEnvironment() == World.Environment.NETHER && player.getLocation().getY() >= 127) {
                player.teleport(player.getLocation().subtract(0, 5, 0)); // Redescendre de 5 blocs
                player.sendMessage(ChatColor.RED + "§7[§e?§7]§c L'accès au toit du Nether est interdit !");
            }
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
    public void onEntityDamageSSSSS(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            double damage = event.getDamage();

            // Détection KillAura (Dégâts supérieurs à 9)
            if (damage > 9) {
                kickPlayer(player, "§7[§4Anticheat§7] Détection KillAura (Dégâts > 9)");
                alertAdmins(player, "§7[§e?§7] KillAura détecté ! Dégâts: " + damage);
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
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();

        // Vérifie si le joueur est proche d’un crafter
        if (!isNearCrafter(player)) return;

        // Vérifie si l’item crafté est une masse (ne pas limiter)
        if (event.getRecipe().getResult().getType() == Material.MACE) {
            return;
        }

        int craftsToday = craftCounter.getOrDefault(uuid, 0);
        if (craftsToday >= maxCraftsPerDay) {
            event.setCancelled(true);
            player.sendMessage("§7[§e?§7] §cVous avez atteint votre limite de craft pour aujourd’hui §7[§c!§7]");
            return;
        }

        // Incrémente le compteur
        craftCounter.put(uuid, craftsToday + 1);
    }

    private boolean isNearCrafter(Player player) {
        Location loc = player.getLocation();
        int radius = craftRadius;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = loc.clone().add(x, y, z).getBlock();
                    if (block.getType() == Material.CRAFTER) {
                        return true;
                    }
                }
            }
        }
        return false;
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
    public void onPlayerKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            UUID killerId = killer.getUniqueId();

            // Charger la réputation actuelle
            pointsKill = reputation.getOrDefault(killerId, loadPlayerReputation(killerId));


            // Appliquer une perte de réputation (minimum 0)
            int newRep = Math.max(pointsKills, -1);  // Réduction de la réputation avec un minimum de 0

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
    }
        


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
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

    public static class Jukeboxmod implements CommandExecutor{
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.getInventory().addItem(createPortableJukebox());
                player.sendMessage("§aVous avez reçu un Jukebox Portatif !");
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

    private void saveTopLuckConfig() {
        try {
            topluckConfig.save(new File(getDataFolder(), "TopLuck.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}












