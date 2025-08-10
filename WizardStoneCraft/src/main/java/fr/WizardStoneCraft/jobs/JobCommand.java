package fr.WizardStoneCraft.jobs;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JobCommand implements CommandExecutor {

    private final JobManager jobManager;

    public JobCommand(WizardStoneCraft plugin) {
        this.jobManager = new JobManager(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Seuls les joueurs peuvent choisir un métier.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.YELLOW + "Utilisation: /job <metier>");
            player.sendMessage(ChatColor.YELLOW + "Métiers disponibles: FARMER, MINER, BLACKSMITH, COOK, HUNTER");
            return true;
        }

        try {
            JobType job = JobType.valueOf(args[0].toUpperCase());
            jobManager.setJob(player, job);
            player.sendMessage(ChatColor.GREEN + "Tu as choisi le métier: " + job.name());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Métier invalide. Métiers disponibles: FARMER, MINER, BLACKSMITH, COOK, HUNTER");
        }

        return true;
    }
}
