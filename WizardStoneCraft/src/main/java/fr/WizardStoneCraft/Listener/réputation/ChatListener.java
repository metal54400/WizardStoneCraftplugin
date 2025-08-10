package fr.WizardStoneCraft.Listener.réputation;

import com.booksaw.betterTeams.Team;
import fr.WizardStoneCraft.WizardStoneCraft;
import fr.WizardStoneCraft.API.Dependency.DependencyManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ChatListener implements Listener {

    private final WizardStoneCraft plugin;
    private final DependencyManager dependencyManager;

    public ChatListener(WizardStoneCraft plugin, DependencyManager dependencyManager) {
        this.plugin = plugin;
        this.dependencyManager = dependencyManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (dependencyManager.getEssentials() != null) {
            applyCustomFormat(event);
        }
    }

    private void applyCustomFormat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        int reputation = plugin.reputation.getOrDefault(uuid, plugin.getReputationManager().loadPlayerReputation(uuid));
        String repPrefix = plugin.getReputationManager().getReputationStatus(reputation);

        String lpPrefix = "";
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user != null) {
            CachedMetaData metaData = user.getCachedData().getMetaData();
            if (metaData.getPrefix() != null) {
                lpPrefix = metaData.getPrefix();
            }
        }

        String teamPrefix = "";
        Team team = Team.getTeam(player);
        if (team != null && team.getDisplayName() != null) {
            teamPrefix = ChatColor.GRAY + team.getDisplayName();
        }

        String displayName = player.getDisplayName();

        String format = ChatColor.translateAlternateColorCodes('&',
                repPrefix + " " + lpPrefix + ChatColor.WHITE + displayName + " " + teamPrefix + ChatColor.RESET + " " + ChatColor.GRAY + ": " + ChatColor.WHITE + "%s"
        );

        event.setFormat(format.replace("%s", event.getMessage()));
    }
}
