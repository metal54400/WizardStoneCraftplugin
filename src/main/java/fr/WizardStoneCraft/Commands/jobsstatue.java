package fr.WizardStoneCraft.Commands;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class jobsstatue implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("jobsstatue") && sender instanceof Player) {
            Player player = (Player) sender;
            sender.sendMessage("\u00a76[Jobs] \u00a7fStatut de vos métiers :");
            for (String job : new String[]{"mineur", "bucheron", "chasseur", "alchimiste", "pêcheur"}) {
                int level = WizardStoneCraft.getJobXp(job + ".level", 0);
                int xp = WizardStoneCraft.getJobXp(job + ".xp", 0);
                sender.sendMessage("\u00a76" + job + " : \u00a7fNiveau " + level + " (" + xp + " XP)");
            }
            return true;
        }
        return false;
    }
}
