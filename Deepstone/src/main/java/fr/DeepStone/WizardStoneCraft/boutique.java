package fr.DeepStone.WizardStoneCraft;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class boutique implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        Player player = (Player) sender;

        TextComponent message = new TextComponent("📦 §aClique ici pour créer ta boutique sur Deepstone !");
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://minecraft-farms.ddns.net:5501/pages/deepstone/boutique.html"));

        player.spigot().sendMessage(message);
        return true;
    }
}
