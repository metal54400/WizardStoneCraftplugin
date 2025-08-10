package fr.WizardStoneCraft.Commands;

import fr.WizardStoneCraft.WizardStoneCraft;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ClearLaggCommand implements CommandExecutor {

    private final WizardStoneCraft plugin;

    public ClearLaggCommand(WizardStoneCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Utilisation: /clearlagg <lagg|clear>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "lagg":
                double tps = getTPS();
                ChatColor color = (tps >= 18.0) ? ChatColor.GREEN : (tps >= 15.0 ? ChatColor.YELLOW : ChatColor.RED);
                sender.sendMessage(color + "[ClearLagg] TPS actuel: " + String.format("%.2f", tps));
                break;

            case "clear":
                plugin.clearEntities();
                sender.sendMessage(ChatColor.GREEN + "[ClearLagg] Suppression des entités exécutée !");
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Commande inconnue. Utilisation: /clearlagg <lagg|clear>");
                break;
        }

        return true;
    }

    // Méthode pour récupérer les TPS (Tick Per Second)
    private double getTPS() {
        try {
            // Reflection pour accéder à MinecraftServer.getServer().recentTps[0]
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            double[] recentTps = (double[]) server.getClass().getField("recentTps").get(server);
            return recentTps[0];
        } catch (Exception e) {
            return -1; // Erreur
        }
    }
}
