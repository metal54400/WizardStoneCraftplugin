package fr.WizardStoneCraft.Commands;

import fr.WizardStoneCraft.Manager.StaffModeManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class StaffModeCommand implements CommandExecutor {

    private final StaffModeManager manager;

    public StaffModeCommand(StaffModeManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Cette commande est réservée aux joueurs.");
            return true;
        }

        if (!player.hasPermission("wizardstonecraft.moderator")) {
            player.sendMessage("§cPermission manquante.");
            return true;
        }

        if (manager.isInStaffMode(player)) {
            manager.disableStaffMode(player);
            player.sendMessage("§cMode staff désactivé.");
        } else {
            manager.enableStaffMode(player);
            player.sendMessage("§aMode staff activé !");
        }

        return true;
    }
}
