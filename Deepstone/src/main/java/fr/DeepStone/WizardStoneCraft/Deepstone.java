package fr.DeepStone.WizardStoneCraft;

import fr.DeepStone.WizardStoneCraft.CommandManager;
import fr.DeepStone.WizardStoneCraft.DataManager;
import fr.DeepStone.WizardStoneCraft.DragonRewardListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import static org.apache.logging.log4j.LogManager.getLogger;

public final class Deepstone extends JavaPlugin {
    private CommandManager commandManager;
    private DataManager dataManager;
    private int taskId = -1;  // ID de la tâche planifiée

    @Override
    public void onEnable() {
        getLogger().info("Deepstone plugin actif !");

        String commandesUrl = getConfig().getString("urls.commandes");
        String clearUrl = getConfig().getString("urls.clear");
        commandManager = new CommandManager(this, commandesUrl, clearUrl);

        getCommand("site-deepstone-boutique").setExecutor(new boutique());

        if (getCommand("traiterboutique") != null) {
            getCommand("traiterboutique").setExecutor((sender, command, label, args) -> {
                if (args.length == 0) {
                    if (commandManager.isEnabled()) {
                        commandManager.traiterCommandesDepuisURL();
                        sender.sendMessage("§7[§e?§7] §aCommandes traitées depuis la boutique distante.");
                    } else {
                        sender.sendMessage("§7[§e!§7] §cLe traitement des commandes est désactivé.");
                    }
                } else {
                    String sub = args[0].toLowerCase();
                    switch (sub) {
                        case "stop":
                            commandManager.setEnabled(false);
                            cancelTask();
                            sender.sendMessage("§7[§e!§7] §cTraitement des commandes désactivé.");
                            break;
                        case "start":
                            commandManager.setEnabled(true);
                            startTask();
                            sender.sendMessage("§7[§a!§7] §aTraitement des commandes activé.");
                            break;
                        default:
                            sender.sendMessage("§7[§e?§7] §cUsage : /traiterboutique [start|stop]");
                    }
                }
                return true;
            });
        }

        // Démarre la tâche périodique si enabled
        if (commandManager.isEnabled()) {
            startTask();
        }

        saveDefaultConfig();
        saveResource("data.yml", false);
        Bukkit.getPluginManager().registerEvents(new DragonRewardListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PhantomDropListener(), this);

        dataManager = new DataManager(this);
    }

    private void startTask() {
        if (taskId == -1) {
            taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(this, commandManager::traiterCommandesDepuisURL, 0L, 200L).getTaskId();
        }
    }

    private void cancelTask() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    @Override
    public void onDisable() {
        cancelTask();
        super.onDisable();
    }
}
