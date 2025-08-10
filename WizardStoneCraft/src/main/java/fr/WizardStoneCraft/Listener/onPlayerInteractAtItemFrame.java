package fr.WizardStoneCraft.Listener;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class onPlayerInteractAtItemFrame implements Listener {
    @EventHandler
    public void onPlayerInteractAtItemFrame(PlayerInteractEntityEvent event) {
        // Vérifier si l'entité cliquée est un ItemFrame et si c'est une montre à l'intérieur
        if (!(event.getRightClicked() instanceof ItemFrame frame)) return;
        if (!(event.getPlayer().isSneaking())) return; // Vérifier si le joueur est accroupi
        if (!(frame.getItem().getType() == Material.CLOCK)) return; // Vérifier si l'item dans l'ItemFrame est une montre

        Player player = event.getPlayer();
        long time = player.getWorld().getTime();

        // Déterminer le moment de la journée
        String moment;
        if (time >= 0 && time < 12300) {
            moment = "§eMatin";
        } else if (time < 13800) {
            moment = "§6Midi";
        } else if (time < 22800) {
            moment = "§cSoir";
        } else {
            moment = "§9Nuit";
        }

        // Formater l'heure en jeu (ticks)
        int hours = (int) ((time / 1000 + 6) % 24); // Convertir les ticks en heures
        int minutes = (int) ((time % 1000) * 60 / 1000);

        // Récupérer l'heure réelle (IRL)
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(TimeZone.getDefault()); // Fuseau horaire du serveur
        String realTime = dateFormat.format(new Date());

        // Message formaté avec l'heure en jeu et l'heure réelle
        String message = ChatColor.GRAY + "In-game: " + ChatColor.AQUA + String.format("%02d:%02d", hours, minutes) +
                ChatColor.GRAY + " | IRL: " + ChatColor.LIGHT_PURPLE + realTime +
                ChatColor.GRAY + " | " + moment;

        // Envoyer le message dans le chat ou dans la barre d'action
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        // Si tu préfères l'envoyer dans le chat à la place :
        // player.sendMessage(message);
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Material item = player.getInventory().getItemInMainHand().getType();

        // Vérifier si le joueur fait un clic droit avec une montre ou une boussole
        if (event.getAction().toString().contains("RIGHT_CLICK") &&
                (item == Material.CLOCK || item == Material.COMPASS)) {

            String message = "";

            if (item == Material.CLOCK) {
                long time = player.getWorld().getTime();
                String moment;
                if (time >= 0 && time < 12300) {
                    moment = "§eMatin";
                } else if (time < 13800) {
                    moment = "§6Midi";
                } else if (time < 22800) {
                    moment = "§cSoir";
                } else {
                    moment = "§9Nuit";
                }

                // Récupérer l'heure en jeu

                int hours = (int) ((time / 1000 + 6) % 24);
                int minutes = (int) ((time % 1000) * 60 / 1000);

                // Récupérer l'heure réelle (IRL)
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                dateFormat.setTimeZone(TimeZone.getDefault()); // Fuseau horaire du serveur
                String realTime = dateFormat.format(new Date());

                // Créer le message d'heure
                message = ChatColor.GRAY + "In-game: " + ChatColor.AQUA + String.format("%02d:%02d", hours, minutes) +
                        ChatColor.GRAY + " | IRL: " + ChatColor.LIGHT_PURPLE + realTime +
                        ChatColor.GRAY + " | " + moment;

            } else if (item == Material.COMPASS) {
                // Créer le message des coordonnées
                Location loc = player.getLocation();
                message = ChatColor.GRAY + "Position: " +
                        ChatColor.YELLOW + "X: " + loc.getBlockX() +
                        ChatColor.GRAY + " Y: " + ChatColor.YELLOW + loc.getBlockY() +
                        ChatColor.GRAY + " Z: " + ChatColor.YELLOW + loc.getBlockZ();
            }

            // Envoyer le message dans la barre d'action
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        }
    }
}
