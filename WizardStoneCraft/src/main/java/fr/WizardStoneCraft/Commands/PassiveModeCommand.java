package fr.WizardStoneCraft.Commands;

import fr.WizardStoneCraft.Listener.PassiveModeGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PassiveModeCommand implements CommandExecutor {

    private final PassiveModeGUI gui;

    public PassiveModeCommand(PassiveModeGUI gui) {
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Cette commande est uniquement pour les joueurs.");
            return true;
        }

        gui.open(player);
        return true;
    }
}
