package fr.WizardStoneCraft.Listener;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class VoicechatMuteListener implements Listener {

    private final VoicechatServerApi voicechatServerApi;

    public VoicechatMuteListener(VoicechatServerApi voicechatServerApi) {
        this.voicechatServerApi = voicechatServerApi;
    }

    @EventHandler
    public void onPlayerCommands(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");

        if (args.length < 2) return; // On a besoin d'au moins 2 arguments (commande + joueur)

        String command = args[0].toLowerCase();
        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            event.getPlayer().sendMessage("§cLe joueur '" + targetName + "' n'est pas en ligne.");
            return;
        }

        VoicechatConnection connection = voicechatServerApi.getConnectionOf(target.getUniqueId());
        if (connection == null) {
            event.getPlayer().sendMessage("§cLe joueur '" + targetName + "' n'est pas connecté à Voicechat.");
            return;
        }

        switch (command) {
            case "/mute":
                connection.setDisabled(true); // Mute vocal
                target.sendMessage("§7[§e?§7] §cVous avez été muté dans le chat vocal.");
                event.getPlayer().sendMessage("§aLe joueur '" + targetName + "' a été muté dans le chat vocal.");
                break;

            case "/unmute":
                connection.setDisabled(false); // Unmute vocal
                target.sendMessage("§7[§e?§7] §aVotre mute vocal a été levé.");
                event.getPlayer().sendMessage("§aLe joueur '" + targetName + "' a été unmute dans le chat vocal.");
                break;

            default:
                // Ne rien faire pour les autres commandes
                break;
        }
    }
}
