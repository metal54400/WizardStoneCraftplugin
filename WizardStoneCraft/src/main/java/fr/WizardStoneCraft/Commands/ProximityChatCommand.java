package fr.WizardStoneCraft.Commands;

import com.booksaw.betterTeams.Team;
import fr.WizardStoneCraft.WizardStoneCraft;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProximityChatCommand implements CommandExecutor {

    private static final double RADIUS = 50.0;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Utilisation : /t <message>");
            return true;
        }

        String msg = String.join(" ", args);
        String proximityPrefix = ChatColor.GOLD + " *Proximité* " + ChatColor.GRAY + ": ";

        String fullMessage;

        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();

            // Récupération de la réputation
            String rep = WizardStoneCraft.getInstance().getReputationManager().getReputationStatus(
                    WizardStoneCraft.getInstance().reputation.getOrDefault(
                            uuid,
                            WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(uuid)
                    )
            );

            // Prefix LuckPerms
            String lpPrefix = "";
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(uuid);
            if (user != null) {
                CachedMetaData metaData = user.getCachedData().getMetaData();
                if (metaData.getPrefix() != null) {
                    lpPrefix = metaData.getPrefix();
                }
            }

            // Prefix d'équipe
            String teamPrefix = "";
            Team team = Team.getTeam(player);
            if (team != null && team.getDisplayName() != null) {
                teamPrefix = team.getDisplayName();
            }

            fullMessage = ChatColor.GRAY + rep + "§f " + lpPrefix + player.getDisplayName()
                    + "§7 " + teamPrefix + proximityPrefix + ChatColor.WHITE + msg;

            // Envoi autour du joueur
            for (Player target : player.getWorld().getPlayers()) {
                if (target.getLocation().distance(player.getLocation()) <= RADIUS) {
                    target.sendMessage(fullMessage);
                }
            }

        } else {
            // Envoi depuis la console
            Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation(); // monde principal
            fullMessage = ChatColor.GRAY + "§d☬ §f[§9Terminal§f] [Console]" + proximityPrefix + ChatColor.WHITE + msg;

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.getWorld().equals(spawn.getWorld()) &&
                        target.getLocation().distance(spawn) <= RADIUS) {
                    target.sendMessage(fullMessage);
                }
            }

            sender.sendMessage(ChatColor.GREEN + "Message envoyé à tous les joueurs autour du spawn.");
        }

        return true;
    }
}
