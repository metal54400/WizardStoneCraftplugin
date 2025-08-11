package fr.WizardStoneCraft;






/*import fr.WizardStoneCraft.API.Base_de_donné.MySQL;
import fr.WizardStoneCraft.API.Base_de_donné.MySQLTables;*/
import com.ranull.graves.Graves;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import dev.cwhead.GravesX.GravesXAPI;
import fr.DeepStone.WizardStoneCraft.CommandManager;
import fr.DeepStone.WizardStoneCraft.Deepstone;
import fr.WizardStoneCraft.API.Dependency.DependencyManager;
import fr.WizardStoneCraft.API.Item.QuestItemCheckEvent;
import fr.WizardStoneCraft.API.Scheduler.ReputationScheduler;
import fr.WizardStoneCraft.Commands.*;
import fr.WizardStoneCraft.Commands.Anticheat.AntiCheatMenuCommand;
import fr.WizardStoneCraft.Listener.*;
import fr.WizardStoneCraft.Listener.AntiCheat.AntiCheatListener;
import fr.WizardStoneCraft.Commands.Anticheat.Topluck;
import fr.WizardStoneCraft.Commands.Claim.*;
import fr.WizardStoneCraft.Commands.Gems.GemCommand;

import fr.WizardStoneCraft.Commands.Gems.Shop.GemShopCommand;
import fr.WizardStoneCraft.Listener.Claim.ClaimUnclaimProtectionListener;
import fr.WizardStoneCraft.Listener.Gamemode.Staff.StaffModeListener;
import fr.WizardStoneCraft.Listener.Gamemode.onGameModeChange;
import fr.WizardStoneCraft.Listener.Gems.GemShopListener;
import fr.WizardStoneCraft.Commands.Reputation.*;
import fr.WizardStoneCraft.Commands.ProximityChatCommand;
import fr.WizardStoneCraft.Listener.Claim.ClaimListener;


import fr.WizardStoneCraft.Listener.Gsit.Graves.CustomSitBlocker;
import fr.WizardStoneCraft.Listener.Gsit.Graves.SitOnGraveBlocker;
import fr.WizardStoneCraft.Listener.Vilager.onVillagerTrade;
import fr.WizardStoneCraft.Listener.loot.SpawnerLootListener;
import fr.WizardStoneCraft.Listener.réputation.ChatListener;
import fr.WizardStoneCraft.Listener.réputation.RepuListener;
import fr.WizardStoneCraft.Listener.réputation.RéputationListener;
import fr.WizardStoneCraft.Manager.*;

import fr.WizardStoneCraft.PlaceHolderApi.PlaceHolderApi;


import fr.WizardStoneCraft.PlaceHolderApi.PlaceholdersCommand;
import fr.WizardStoneCraft.jobs.JobCommand;
import fr.WizardStoneCraft.jobs.JobManager;
import me.neznamy.tab.api.TabAPI;
import me.realized.duels.api.DuelsAPI;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BossBar;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.time.LocalDate;
import java.util.*;


import static fr.WizardStoneCraft.API.Item.NBTCleanerListener.cleanItemNBT;
import static fr.WizardStoneCraft.Commands.PassiveCommand.passivePlayers;
import static org.bukkit.Bukkit.getPlayer;

public class WizardStoneCraft extends JavaPlugin implements TabExecutor,Listener {
    public int teleportProtectionSeconds;
    public int combatDurationSeconds;
    public int reputationLossAttackPassive;
    public int reputationLossPassiveAttacker;
    public boolean logPassiveAttacks;
    private final Set<UUID> pvpModePlayers = new HashSet<>();
    private static final Logger log = LoggerFactory.getLogger(WizardStoneCraft.class);
    private final Map<UUID, Long> protectionCooldowns = new HashMap<>();
    public final HashMap<UUID, Integer> clickCounts = new HashMap<>();
    public Map<UUID, Map<UUID, Integer>> killTracking = new HashMap<>();
    public Map<UUID, List<Long>> killTimestamps = new HashMap<>();
    private DailyQuestManager dailyQuestManager;
    private final HashMap<UUID, Location> mobLocations = new HashMap<>();
    private final Map<UUID, Long> raidCooldown = new HashMap<>();
    public final HashSet<Long> disabledClaims = new HashSet<Long>();
    private final long ONE_HOUR = 3600000L;
    public final Map<UUID, Long> bookTradeCooldown = new HashMap<>();
    private final HashMap<UUID, String> playerStatus = new HashMap<>();
    public final HashMap<UUID, Long> combatLog = new HashMap<>();
    public static final long COMBAT_TIME = 15000; // 15 sec
    private File jobsFile;
    private final int REQUIRED_REP = 80;
    public final HashMap<UUID, Long> protectedPlayers = new HashMap<>();
    private final long PROTECTION_TIME = 90 * 1000;
    private final HashMap<UUID, Long> elytraCooldown = new HashMap<>();
    private final long COOLDOWN_TIME = 4000; // 4 secondes en millisecondes
    private static FileConfiguration jobsConfig;
    private final Set<UUID> refundedPlayers = new HashSet<>(); // Liste des joueurs remboursés
    public Map<UUID, Long> mutedPlayers = new HashMap<>();
    public double dropChance;
    private FileConfiguration bannedPlayersConfig;
    private Map<UUID, Long> bannedPlayers;
    public static final Map<UUID, Integer> reputation = new HashMap<>();
    public final Map<UUID, Map<UUID, Long>> killHistory = new HashMap<>();
    private final Map<Player, Integer> playerReputations = new HashMap<>(); // Stocke la réputation des joueurs
    private final Map<Player, Player> selectedPlayers = new HashMap<>();
    List<String> lore = new ArrayList<>(); // Crée une liste vide
    public int MIN_REP;
    public  int MAX_REP;
    public int pointsKills;
    public GemShopManager shopManager;
    public int pointsJoin;
    private FileConfiguration messages;
    private String tabPrefix;
    private String chatPrefix;
    public FileConfiguration config;
    private LuckPerms luckPerms;
    public GriefPrevention griefPrevention;
    private ReputationManager reputationManager;
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
    public final List<String> blockedCommands = Arrays.asList("/tpa", "/tpahere", "/tpno", "/sethome", "/team sethome","/tpaccept","tpcancel");
    public final HashMap<UUID, Integer> minedOres = new HashMap<>();
    public final HashMap<UUID, Long> lastClickTime = new HashMap<>();
    private final HashMap<UUID, Double> lastSpeed = new HashMap<>();
    public final HashMap<UUID, Long> lastTeleport = new HashMap<>();
    public final HashMap<UUID, Integer> flyWarnings = new HashMap<>();

    public final Set<Player> alertEnabledPlayers = new HashSet<>();
    private FileConfiguration menuConfig;
    private final HashMap<UUID, Integer> craftCounter = new HashMap<>();
    private final Map<UUID, Location> lastDeathLocations = new HashMap<>();
    private final String TOTEM_KEY = "totem_revanche";
    private int maxCraftsPerDay;
    private int craftRadius;
    public Economy economy;;
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
    private  GravesXAPI gravesXAPI;

    private final Map<UUID, Set<UUID>> bossAttackers = new HashMap<>();
    private final HashMap<UUID, Integer> redstoneActivations = new HashMap<>();
    private final HashMap<UUID, Long> lastActivation = new HashMap<>();
    private FileConfiguration messagesConfig;
    private List<String> serverMessages;
    private int messageIndex = 0;
    private File topluckConfigFile;
    public FileConfiguration topluckConfig;
    public CommandManager commandManager;
    public GemManager gemManager;
    public boolean antiCheatEnabled;
    private DependencyManager dependencyManager;
    private StaffModeManager staffModeManager;

    private JobManager jobManager;


    public static WizardStoneCraft getInstance() {
        return instance;
    }


    // Durée en ticks (60 Minecraft jours = 1,440,000 ticks)

    @Override
    public void onLoad() {
        getLogger().info("§7[§e?§7]§a WizardStoneCraft à été chargé !");
    }


    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }


        this.gemManager = new GemManager(this);
        this.shopManager = new GemShopManager(this);
        this.dailyQuestManager = new DailyQuestManager(this);
        this.reputationManager = new ReputationManager(this);
        this.dependencyManager = new DependencyManager(this);
        this.staffModeManager = new StaffModeManager(this);

        dependencyManager.loadDependencies();
        saveDefaultConfig();
        new ReputationScheduler(this).startWeeklyTask();
        loadConfiguration();
        loadMessages();
        startNametagUpdater();
        loadMessages();
        loadMessagese();
        loadMessagesConfig();
        loadTopLuckConfig();
        loadJoinRewards();
        startMessageTask();
        startClearLaggTask();
        PlaceHolderApi.checkPlaceholderAPI();

        config = getConfig();
        dropChance = config.getDouble("drop-chance", 50.0) / 100.0;
        maxCraftsPerDay = getConfig().getInt("craft_limit", 50);
        craftRadius = config.getInt("craft_radius", 5);
        atmAmount = config.getInt("atm_amount", 100);
        atmMessage = config.getString("atm_message", "Vous avez retiré %amount% pièces depuis l'ATM !");
        disableThorns = getConfig().getBoolean("disable_thorns.enabled", true);
        onlyPvp = getConfig().getBoolean("disable_thorns.only_pvp", true);
        mobsAffected = getConfig().getBoolean("disable_thorns.mobs_affected", false);
        antiCheatEnabled = getConfig().getBoolean("anticheat-enabled", false);
        teleportProtectionSeconds = getConfig().getInt("teleport_protection_seconds", 90);
        combatDurationSeconds = getConfig().getInt("combat_duration_seconds", 15);
        reputationLossAttackPassive = getConfig().getInt("reputation_loss_attack_passive", 20);
        reputationLossPassiveAttacker = getConfig().getInt("reputation_loss_passive_attacker", 1);
        logPassiveAttacks = getConfig().getBoolean("log_passive_attacks", true);

        instance = this;

        new PlaceHolderApi().register();

        DuelsAPI duelsAPI = new DuelsAPI();
        Graves WizardStoneCraft = (Graves) Bukkit.getPluginManager().getPlugin("GravesX");

        if (WizardStoneCraft == null || !WizardStoneCraft.isEnabled()) {
            getLogger().severe("GravesX plugin not found or not enabled!");
            // Arrête l'initialisation ici si nécessaire
            return;
        }

        //Listener
        PassiveModeGUI passiveGUI = new PassiveModeGUI();

        Bukkit.getPluginManager().registerEvents(new SpawnerLootListener(),this);
        Bukkit.getPluginManager().registerEvents(new BundleManager(this),this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(this, dependencyManager), this);
        Bukkit.getPluginManager().registerEvents(passiveGUI, this);
        Bukkit.getPluginManager().registerEvents(new RéputationListener(), this);
        Bukkit.getPluginManager().registerEvents(new AntiCheatListener(), this);
        Bukkit.getPluginManager().registerEvents(new ClaimListener(), this);
        Bukkit.getPluginManager().registerEvents(new ClaimListener(), this);
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new GemShopListener(gemManager, shopManager), this);
        Bukkit.getPluginManager().registerEvents(new Topluck(this), this);
        Bukkit.getPluginManager().registerEvents(new ClaimUnclaimProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CreativeItemLoreListener(), this);
        Bukkit.getPluginManager().registerEvents(new VoicechatMuteListener(voicechatServerApi), this);
        Bukkit.getPluginManager().registerEvents(new PvpListener(this,gravesXAPI),this);
        Bukkit.getPluginManager().registerEvents(new CustomSitBlocker(),this);
        Bukkit.getPluginManager().registerEvents(new SitOnGraveBlocker(), this);
        Bukkit.getPluginManager().registerEvents(new CombatLogListener(duelsAPI, combatLog), this);
        Bukkit.getPluginManager().registerEvents(new onPlayerInteractAtItemFrame(),this);
        Bukkit.getPluginManager().registerEvents(new élytra(),this);
        Bukkit.getPluginManager().registerEvents(new onGameModeChange(),this);
        Bukkit.getPluginManager().registerEvents(new onVillagerTrade(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(),this);
        Bukkit.getPluginManager().registerEvents(new RepuListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ShulkerShellDropListener() ,this);
        Bukkit.getPluginManager().registerEvents(new StaffModeListener(staffModeManager,this),this);

        //Register Commande
        getCommand("clearlagg").setExecutor(new ClearLaggCommand(this));
        getCommand("placeholders").setExecutor(new PlaceholdersCommand());
        getCommand("job").setExecutor(new JobCommand(this));
        getCommand("t").setExecutor(new ProximityChatCommand());
        getCommand("gemshop").setExecutor(new GemShopCommand(shopManager));
        getCommand("gems").setExecutor(new GemCommand(gemManager, economy));
        getCommand("gemsreconvert").setExecutor(new GemCommand(gemManager, economy));
        getCommand("gemadd").setExecutor(new GemCommand(gemManager, economy));
        getCommand("gemstop").setExecutor(new GemCommand(gemManager, economy));
        getCommand("repadd").setExecutor(new ManageRepCommand());
        getCommand("repremove").setExecutor(new ManageRepCommand());
        getCommand("Claimoffspawnmob").setExecutor(new claimoffmobspawn());
        getCommand("reptop").setExecutor(new ReptopCommand());
        getCommand("rep").setExecutor(new ReputationCommand());
        getCommand("rephelp").setExecutor(new RepHelpCommand());
        getCommand("repreload").setExecutor(new RepReloadCommand(this));
        getCommand("Broadcast").setExecutor(new Broadcast());
        getCommand("repspawnnpc").setExecutor(new repspawnnpc());
        getCommand("repunmute").setExecutor(new RepMuteUnmuteCommand(mutedPlayers));
        getCommand("passifset").setExecutor(new PassiveCommand());
        getCommand("passifunset").setExecutor(new PassiveCommand());
        getCommand("repmute").setExecutor(new RepMuteUnmuteCommand(mutedPlayers));
        getCommand("tradeclaim").setExecutor(new TradeClaimCommand());
        getCommand("claimmeteo").setExecutor(new ClaimWeatherControl());
        getCommand("cook").setExecutor(new CookFoodCommand());
        getCommand("claimlist").setExecutor(new Claims());
        getCommand("elytra").setExecutor(new endelytra());
        getCommand("claimrename").setExecutor(new ClaimNaming());
        getCommand("topluck").setExecutor(new Topluck(this));
        getCommand("givejukebox").setExecutor(new Jukeboxmod());
        getCommand("testrep").setExecutor(new testrep());
        getCommand("claimgive").setExecutor(new ClaimTransferPlugin());
        getCommand("menueggcustom").setExecutor(new egg());
        getCommand("restartplugin").setExecutor(new RestartCommand());
        getCommand("passifgui").setExecutor(new PassiveModeCommand(passiveGUI));
        getCommand("anticheatmenu").setExecutor(new AntiCheatMenuCommand());
        getCommand("staffmode").setExecutor(new StaffModeCommand(staffModeManager));
        getCommand("listops").setExecutor(new ListOpsCommand());

        getLogger().info("Anti-Cheat Menu activé !");
        getLogger().info("§7[§e?§7]§a WizardStoneCraft est activé !");




    }

    public boolean checkItem(Player player, ItemStack item) {
        // Ici ta logique pour définir si l'item est valide ou non
        boolean valid = true; // Par défaut true, à adapter selon ta logique

        QuestItemCheckEvent event = new QuestItemCheckEvent(player, item, valid);
        Bukkit.getPluginManager().callEvent(event);

        item = event.getItem();
        valid = event.isValid();

        return valid;
    }

    @Override
    public void onDisable() {
        getLogger().info("§7[§e?§7]§c WizardStoneCraft est désactivé !");
    }

    public ReputationManager getReputationManager() {
        return reputationManager;
    }

    public JobManager getJobManager() {
        return jobManager;
    }
    public void startNametagUpdater() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PvpListener.updateNametag(player); // Appelle ta fonction existante
            }
        }, 0L, 20L * 30); // 20 ticks * 30 = toutes les 30 secondes
    }
    @EventHandler
    public void onQuestItemCheck(QuestItemCheckEvent event) {
        ItemStack cleaned = cleanItemNBT(event.getItem());
        event.setItem(cleaned);
    }


    private void loadTopLuckConfig() {
        File topluckFile = new File(getDataFolder(), "TopLuck.yml");
        if (!topluckFile.exists()) {
            saveResource("TopLuck.yml", false);
        }
        topluckConfig = YamlConfiguration.loadConfiguration(topluckFile);
    }

    private final Map<UUID, Long> lastJoinReward = new HashMap<>();

    public long getLastJoinReward(UUID uuid) {
        return joinRewardConfig.getLong("rewards." + uuid.toString(), 0L);
    }

    public void setLastJoinReward(UUID uuid, long timestamp) {
        joinRewardConfig.set("rewards." + uuid.toString(), timestamp);
        saveJoinRewards();
    }

    public Set<UUID> getPassivePlayers() {
        return passivePlayers.keySet();
    }

    private File joinRewardFile;
    private FileConfiguration joinRewardConfig;

    public void loadJoinRewards() {
        joinRewardFile = new File(getDataFolder(), "join_rewards.yml");
        if (!joinRewardFile.exists()) {
            saveResource("join_rewards.yml", false);
        }

        joinRewardConfig = YamlConfiguration.loadConfiguration(joinRewardFile);
    }

    public void saveJoinRewards() {
        try {
            joinRewardConfig.save(joinRewardFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void saveTopluckConfig() {
        if (topluckConfig == null || topluckConfigFile == null) return;
        try {
            topluckConfig.save(topluckConfigFile);
        } catch (IOException e) {
            getLogger().severe("Impossible de sauvegarder topluck.yml !");
            e.printStackTrace();
        }
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

    public String getLuckPermsPrefix(Player player) {
        if (luckPerms == null) return ""; // LuckPerms non configuré

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            CachedMetaData metaData = user.getCachedData().getMetaData();
            String prefix = metaData.getPrefix();
            return prefix != null ? ChatColor.translateAlternateColorCodes('&', prefix) + " " : "";
        }
        return "";
    }

    public void loadMessagese() {
        File file = new File(getDataFolder(), "messages.yml");
        if (!file.exists()) {
            saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(file);
    }

    private boolean elytraEnabledInEnd = true;

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
    public void onMobSpawn(CreatureSpawnEvent event) {
        Location loc = event.getLocation();
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);

        if (claim != null && disabledClaims.contains(claim.getID())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRaidWaveSpawn(RaidSpawnWaveEvent event) {
        // Vérifie seulement à la première vague du raid
        if (event.getPatrolLeader().getWave() != 1) return;

        Raid raid = event.getRaid();
        long now = System.currentTimeMillis();

        Iterator<UUID> it = raid.getHeroes().iterator();
        while (it.hasNext()) {
            UUID uuid = it.next();
            Player player = getPlayer(uuid);

            if (player == null) continue; // le joueur est peut-être déconnecté

            if (raidCooldown.containsKey(uuid)) {
                long lastRaid = raidCooldown.get(uuid);

                if (now - lastRaid < ONE_HOUR) {
                    long minutesRestantes = (ONE_HOUR - (now - lastRaid)) / 60000;
                    player.sendMessage("§7[§e✖§7] Vous devez attendre encore §c" + minutesRestantes + " minute(s) §7avant de pouvoir refaire un raid.");

                    it.remove(); // supprime le joueur du raid
                    continue;
                }
            }

            // ✅ Mise à jour du cooldown
            raidCooldown.put(uuid, now);
        }
    }


    @EventHandler
    public void onPlayerMovesss(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        // Ignore les mouvements sans changement de position verticale
        if (event.getFrom().getBlockY() == event.getTo().getBlockY()) return;

        if (world.getEnvironment() == World.Environment.NETHER && player.getLocation().getY() >= 127) {
            // Vérifie si le joueur n’a pas la permission spéciale
            if (!player.hasPermission("WizardStoneCraft.toitnether")) {
                Location safeLocation = player.getLocation().clone().subtract(0, 5, 0);

                player.teleport(safeLocation);
                player.sendMessage(ChatColor.RED + "§7[§e?§7]§c L'accès au toit du Nether est interdit !");
            }
        }
    }

    public void banPlayer(Player player, String reason) {
        Bukkit.getScheduler().runTask(this, () -> {
            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), reason, null, "Console");
            player.kickPlayer(ChatColor.RED + "[Banni] " + reason);
        });
    }

    public void kickPlayer(Player player, String reason) {
        Bukkit.getScheduler().runTask(this, () -> player.kickPlayer(reason));
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();

        if (!(entity.getType() == EntityType.WITHER || entity.getType() == EntityType.ENDER_DRAGON || entity.getType() == EntityType.WARDEN)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        UUID bossId = entity.getUniqueId();

        bossAttackers.putIfAbsent(bossId, new HashSet<>());
        bossAttackers.get(bossId).add(player.getUniqueId());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        EntityType type = entity.getType();

        double totalReward = 0;
        switch (type) {
            case WITHER:
                totalReward = 500;
                break;
            case ENDER_DRAGON:
                totalReward = 100;
                break;
            case WARDEN:
                totalReward = 1000;
                break;
            default:
                return;
        }

        Set<UUID> participants = bossAttackers.remove(entity.getUniqueId());
        if (participants == null || participants.isEmpty()) return;

        double share = totalReward / participants.size();

        for (UUID playerId : participants) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                economy.depositPlayer(player, share);
                player.sendMessage("§aTu as reçu " + share + "€ pour avoir aidé à tuer un " + type.name() + " !");
            }
        }
    }

    @EventHandler
    public void onEnderDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon)) return;

        EnderDragon dragon = (EnderDragon) event.getEntity();
        Player killer = dragon.getKiller();

        if (killer == null) return;

        // Jouer le son à volume réduit dans l'End
        World endWorld = dragon.getWorld();
        endWorld.playSound(dragon.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.2f, 1.0f);

        // Rejouer le son dans l'Overworld et le Nether
        World overworld = Bukkit.getWorld("world");
        World nether = Bukkit.getWorld("world_nether");

        if (overworld != null) {
            overworld.playSound(overworld.getSpawnLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.2f, 1.0f);
        }

        if (nether != null) {
            nether.playSound(nether.getSpawnLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.2f, 1.0f);
        }

        // Téléporter le joueur (exemple : vers le spawn de l'Overworld)
        killer.sendMessage(ChatColor.GREEN + "§7[§e?§7] ce joueur" + "§a"+ killer.getName() + "§7as vaincu le §cdragon§7 !");
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
    public void onPlayerUsePortableJukebox(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Vérifie si le joueur clique droit avec un jukebox portatif
        if (item != null && item.hasItemMeta() && item.getItemMeta().getDisplayName().equals("§6Jukebox Portatif")) {
            // Exécuter l'action : jouer une musique ou changer de disque
            Jukeboxmod.playCustomMusic(player);
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

        private static final String[] discs = {
                "music_disc.13",
                "music_disc.cat",
                "music_disc.blocks",
                "music_disc.chirp",
                "music_disc.far",
                "music_disc.mall",
                "music_disc.mellohi",
                "music_disc.stal",
                "music_disc.strad",
                "music_disc.ward",
                "music_disc.11",
                "music_disc.wait",
                "music_disc.pigstep",
                "music_disc.otherside",
                "music_disc.5",
                "music_disc.relic",
                "music_disc.creator",
                "music_disc.creator_music_box",
                "music_disc.precipice"
        };

        private static void playCustomMusic(Player player) {

            int randomIndex = (int) (Math.random() * discs.length);
            String key = discs[randomIndex];

            Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(key));

            if (sound != null) {
                player.playSound(player.getLocation(), sound, SoundCategory.RECORDS, 1.0f, 1.0f);
                player.sendMessage("§aVous écoutez maintenant une musique aléatoire !");
            } else {
                player.sendMessage("§cImpossible de jouer ce disque : " + key);
            }
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

        if (count >= 600) { // Limite de 10 activations rapides
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

    private void startClearLaggTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                broadcastWarning(60); // Avertissement 60s
                Bukkit.getScheduler().runTaskLater(getInstance(), () -> broadcastWarning(30), 30 * 20L); // 30s
                Bukkit.getScheduler().runTaskLater(getInstance(), () -> clearEntities(), 60 * 20L); // Nettoyage
            }
        }.runTaskTimer(this, 0, 3600 * 20L); // Toutes les 1 heure
    }

    private void broadcastWarning(int seconds) {
        String message = ChatColor.RED + "§7[§e?§7] [ClearLagg]§c Suppression des entités dans " + seconds + " secondes !";
        Bukkit.broadcastMessage(message);
    }

    public void clearEntities() {
        int removed = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Item || entity instanceof LivingEntity) {
                    if (entity instanceof Player || isProtectedEntity(entity)) continue;
                    entity.remove();
                    removed++;
                }
            }
        }
        Bukkit.broadcastMessage(ChatColor.GREEN + "[ClearLagg] " + removed + " entités supprimées dans tous les mondes !");
    }

    private boolean isProtectedEntity(Entity entity) {
        return entity.getType() == EntityType.VILLAGER ||
                entity.getType() == EntityType.ARMOR_STAND ||

                // Tous les types de bateaux simples
                entity.getType() == EntityType.OAK_BOAT ||
                entity.getType() == EntityType.SPRUCE_BOAT ||
                entity.getType() == EntityType.BIRCH_BOAT ||
                entity.getType() == EntityType.JUNGLE_BOAT ||
                entity.getType() == EntityType.ACACIA_BOAT ||
                entity.getType() == EntityType.DARK_OAK_BOAT ||
                entity.getType() == EntityType.MANGROVE_BOAT ||
                entity.getType() == EntityType.BAMBOO_RAFT ||
                entity.getType() == EntityType.CHERRY_BOAT ||
                entity.getType() == EntityType.PALE_OAK_BOAT ||

                // Tous les types de bateaux avec coffre
                entity.getType() == EntityType.OAK_CHEST_BOAT ||
                entity.getType() == EntityType.SPRUCE_CHEST_BOAT ||
                entity.getType() == EntityType.BIRCH_CHEST_BOAT ||
                entity.getType() == EntityType.JUNGLE_CHEST_BOAT ||
                entity.getType() == EntityType.ACACIA_CHEST_BOAT ||
                entity.getType() == EntityType.DARK_OAK_CHEST_BOAT ||
                entity.getType() == EntityType.MANGROVE_CHEST_BOAT ||
                entity.getType() == EntityType.BAMBOO_CHEST_RAFT ||
                entity.getType() == EntityType.CHERRY_CHEST_BOAT ||
                entity.getType() == EntityType.PALE_OAK_CHEST_BOAT ||

                // Minecarts
                entity.getType() == EntityType.MINECART ||

                // Protection spéciale pour entité nommée
                (entity instanceof LivingEntity && entity.getCustomName() != null);
    }

    @EventHandler
    public void onCreeperSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Creeper)) return;
        Creeper creeper = (Creeper) event.getEntity();

        double chance = random.nextDouble();
        if (chance < 0.2) {
            creeper.getPersistentDataContainer().set(new NamespacedKey(this, "variant"), PersistentDataType.STRING, "frozen");
            creeper.setCustomName("§bCreeper Gelé");
        } else if (chance < 0.4) {
            creeper.getPersistentDataContainer().set(new NamespacedKey(this, "variant"), PersistentDataType.STRING, "poison");
            creeper.setCustomName("§2Creeper Empoisonné");
        } else if (chance < 0.6) {
            creeper.getPersistentDataContainer().set(new NamespacedKey(this, "variant"), PersistentDataType.STRING, "electric");
            creeper.setCustomName("§eCreeper Électrique");
        }
    }

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Creeper)) return;
        Creeper creeper = (Creeper) event.getEntity();
        String variant = creeper.getPersistentDataContainer().get(new NamespacedKey(this, "variant"), PersistentDataType.STRING);

        if (variant == null) return;

        switch (variant) {
            case "frozen":
                for (Entity e : event.getEntity().getNearbyEntities(4, 4, 4)) {
                    if (e instanceof Player p)
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 2));
                }
                break;
            case "poison":
                for (Entity e : event.getEntity().getNearbyEntities(4, 4, 4)) {
                    if (e instanceof Player p)
                        p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                }
                break;
            case "electric":
                for (Entity e : event.getEntity().getNearbyEntities(4, 4, 4)) {
                    e.getWorld().strikeLightningEffect(e.getLocation());
                }
                break;
        }
    }

    public void addCustomSpawnEggs(Player player) {
        // Créer les œufs personnalisés
        ItemStack frozenEgg = new ItemStack(Material.CREEPER_SPAWN_EGG);
        ItemMeta frozenMeta = frozenEgg.getItemMeta();
        frozenMeta.setDisplayName("§bŒuf de Creeper Gelé");
        frozenEgg.setItemMeta(frozenMeta);

        ItemStack poisonEgg = new ItemStack(Material.CREEPER_SPAWN_EGG);
        ItemMeta poisonMeta = poisonEgg.getItemMeta();
        poisonMeta.setDisplayName("§2Œuf de Creeper Empoisonné");
        poisonEgg.setItemMeta(poisonMeta);

        ItemStack electricEgg = new ItemStack(Material.CREEPER_SPAWN_EGG);
        ItemMeta electricMeta = electricEgg.getItemMeta();
        electricMeta.setDisplayName("§eŒuf de Creeper Électrique");
        electricEgg.setItemMeta(electricMeta);

        ItemStack RodeurEgg = new ItemStack(Material.HUSK_SPAWN_EGG);
        ItemMeta RodeurEggMeta = RodeurEgg.getItemMeta();
        electricMeta.setDisplayName("§eŒuf de Rodeur");
        electricEgg.setItemMeta(RodeurEggMeta);

        // Ajouter les œufs à l'inventaire créatif du joueur
        player.getInventory().addItem(frozenEgg, poisonEgg, electricEgg,RodeurEgg);
    }

    public void openCustomEggsMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 54, "§aŒufs Spéciaux");

        menu.setItem(0, createCustomEgg("§bŒuf de Creeper Gelé", "frozen_creeper"));
        menu.setItem(1, createCustomEgg("§2Œuf de Creeper Empoisonné", "poison_creeper"));
        menu.setItem(2, createCustomEgg("§eŒuf de Creeper Électrique", "electric_creeper"));
        menu.setItem(3, createCustomEggs("§eŒuf de Rodeur", "Rodeur"));

        player.openInventory(menu);
    }

    private ItemStack createCustomEgg(String name, String tag) {
        ItemStack egg = new ItemStack(Material.CREEPER_SPAWN_EGG);
        ItemMeta meta = egg.getItemMeta();
        meta.setDisplayName(name);

        // Tag custom via PersistentDataContainer
        NamespacedKey key = new NamespacedKey(this, "customEgg");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, tag);

        egg.setItemMeta(meta);
        return egg;
    }

    private ItemStack createCustomEggs(String name, String tag) {
        ItemStack egg = new ItemStack(Material.HUSK_SPAWN_EGG);
        ItemMeta meta = egg.getItemMeta();
        meta.setDisplayName(name);

        // Tag custom via PersistentDataContainer
        NamespacedKey key = new NamespacedKey(this, "customEgg");
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, tag);

        egg.setItemMeta(meta);
        return egg;
    }

    public class egg implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (!(sender instanceof Player player)) return true;

            openCustomEggsMenu(player);
            return true;
         }

    }

    private VoicechatServerApi voicechatServerApi;



    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.HUSK) {
            LivingEntity entity = (LivingEntity) event.getEntity();

            // Vérifie si c'est un Husk et change ses caractéristiques
            if (Math.random() < 0.1) { // 10% de chance pour un husk d'être un Rodeur
                // Nommer le husk "Rodeur"
                entity.setCustomName("Rodeur");
                entity.setCustomNameVisible(true);

                // Augmenter la vitesse
                entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1)); // Vitesse de niveau 1

                // Augmenter les dégâts
                entity.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(12.0); // Dégâts augmentés à 12

                // Augmenter la santé (facultatif)
                entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(60.0); // Santé à 60

                // Ajouter une armure ou un équipement spécial
                ItemStack helmet = new ItemStack(Material.IRON_HELMET);
                ItemMeta meta = helmet.getItemMeta();
                meta.setDisplayName("Casque du Rodeur");
                helmet.setItemMeta(meta);
                entity.getEquipment().setHelmet(helmet);
            }
        }
    }

    @EventHandler
    public void onEntityDamagesssss(EntityDamageEvent event) {
        // Si le Rodeur est blessé, lui appliquer des effets spéciaux
        if (event.getEntity() instanceof Husk) {
            Husk husk = (Husk) event.getEntity();
            if (husk.getCustomName() != null && husk.getCustomName().equals("Rodeur")) {
                // Appliquer un effet de régénération lorsqu'il est blessé (facultatif)
                husk.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
            }
        }
    }
}
















