package fr.WizardStoneCraft.API.Dependency;

import com.earth2me.essentials.Essentials;
import net.luckperms.api.LuckPerms;

import net.milkbowl.vault.economy.Economy;
import me.clip.placeholderapi.PlaceholderAPI;
import me.neznamy.tab.api.TabAPI;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.logging.Logger;

import me.ryanhamshire.GriefPrevention.GriefPrevention;

public class DependencyManager {
    private Essentials essentials;

    private final Plugin plugin;
    private final Logger logger;

    private LuckPerms luckPerms;
    private Economy economy;
    private GriefPrevention griefPrevention;
    private TabAPI tab;

    public DependencyManager(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void loadDependencies() {
        setupLuckPerms();
        setupEconomy();
        setupGriefPrevention();
        setupTAB();
        setupEssentialsX();
    }

    private void setupEssentialsX() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (plugin instanceof Essentials) {
            this.essentials = (Essentials) plugin;
            logger.info("EssentialsX détecté et intégré avec succès.");
        } else {
            logger.warning("EssentialsX non détecté !");
        }
    }

    private void setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            this.luckPerms = provider.getProvider();
            logger.info("§7[§e?§7]§a LuckPerms API détectée et connectée !");
        } else {
            logger.warning("§7[§e?§7]§c LuckPerms API non détectée !");
        }
    }

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            logger.severe("Vault non trouvé !");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }

        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            this.economy = provider.getProvider();
            logger.info("Système économique via Vault initialisé !");
        } else {
            logger.severe("Impossible d'initialiser Vault !");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    private void setupGriefPrevention() {
        Plugin gp = Bukkit.getPluginManager().getPlugin("GriefPrevention");
        if (gp instanceof GriefPrevention) {
            this.griefPrevention = (GriefPrevention) gp;
            logger.info("GriefPrevention détecté et lié.");
        } else {
            logger.warning("GriefPrevention manquant !");
        }
    }

    private void setupTAB() {
        if (Bukkit.getPluginManager().isPluginEnabled("TAB")) {
            this.tab = TabAPI.getInstance();
            logger.info("Plugin TAB détecté !");
        } else {
            logger.warning("Le plugin TAB n'est pas actif !");
        }
    }

    // Getters
    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public Essentials getEssentials() {
        return essentials;
    }


    public Economy getEconomy() {
        return economy;
    }

    public GriefPrevention getGriefPrevention() {
        return griefPrevention;
    }

    public TabAPI getTabAPI() {
        return tab;
    }
}