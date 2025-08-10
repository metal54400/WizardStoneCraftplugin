package fr.WizardStoneCraft;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class RestartCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("WizardStoneCraft");

        if (plugin == null) {
            sender.sendMessage(ChatColor.RED + "Plugin introuvable !");
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "Redémarrage du plugin WizardStoneCraft...");

        // Désactive le plugin
        Bukkit.getScheduler().runTask(WizardStoneCraft.getInstance(), () -> {
            Bukkit.getPluginManager().disablePlugin(plugin);

            // Petite pause pour être sûr que tout est bien clean
            Bukkit.getScheduler().runTaskLater(WizardStoneCraft.getInstance(), () -> {
                Bukkit.getPluginManager().enablePlugin(plugin);
                sender.sendMessage(ChatColor.GREEN + "Plugin redémarré !");
            }, 20L); // 1 seconde
        });

        return true;
    }
}
