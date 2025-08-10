package fr.WizardStoneCraft.PlaceHolderApi;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaceholdersCommand implements CommandExecutor {

    private final String[] placeholders = {
            "%wizardstonecraft_passif%",
            "%wizardstonecraft_repamount%",
            "%wizardstonecraft_repprefix%",
            "%wizardstonecraft_repprefixe%",
            "%wizardstonecraft_Vstatus%"

    };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cette commande est réservée aux joueurs.");
            return true;
        }

        sender.sendMessage("§6[WizardStoneCraft] §ePlaceholders disponibles :");
        for (String ph : placeholders) {
            sender.sendMessage(" §7- " + ph);
        }
        return true;
    }
}