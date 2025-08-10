package fr.WizardStoneCraft.Commands.Reputation;

import fr.WizardStoneCraft.PlaceHolderApi.PlaceHolderApi;
import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getPlayer;

public class ManageRepCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("reputation.manage")) {
            sender.sendMessage(WizardStoneCraft.getInstance().getMessage("no_permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(WizardStoneCraft.getInstance().getMessage("usage").replace("%command%", label));
            return true;
        }

        String targetPlayer = args[0];
        Player target = getPlayer(targetPlayer);
        if (target == null) {
            sender.sendMessage(WizardStoneCraft.getInstance().getMessage("player_not_found"));
            return true;
        }
        if (!(sender instanceof Player) && args[0].contains("%player%")) {
            sender.sendMessage(WizardStoneCraft.getInstance().getMessage("console_cannot_use_placeholder"));
            return true;
        }

        // Remplacer le placeholder avec PlaceholderAPI
        if (sender instanceof Player) {
            targetPlayer = PlaceHolderApi.parse((Player) sender, targetPlayer);
        }


        int amount = Integer.parseInt(args[1]);
        UUID targetId = target.getUniqueId();
        int newReputation = label.equals("repadd")
                ? WizardStoneCraft.getInstance().reputation.getOrDefault(targetId, WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(targetId)) + amount
                : WizardStoneCraft.getInstance().reputation.getOrDefault(targetId, WizardStoneCraft.getInstance().getReputationManager().loadPlayerReputation(targetId)) - amount;
        newReputation = Math.max(Math.min(newReputation, WizardStoneCraft.getInstance().MAX_REP),WizardStoneCraft.getInstance().MIN_REP);
        WizardStoneCraft.getInstance().reputation.put(targetId, newReputation);
        sender.sendMessage(WizardStoneCraft.getInstance().getMessage("rep_modified")
                .replace("%player%", target.getName())
                .replace("%amount%", String.valueOf(amount)));
        WizardStoneCraft.getInstance().getReputationManager().savePlayerReputation(targetId, newReputation);

        if (newReputation <= -120) {
            // Auto-ban de 15 jours
            long banDuration = System.currentTimeMillis() + (15L * 24 * 60 * 60 * 1000);
            Bukkit.getScheduler().runTask((Plugin) this, () -> {
                target.kickPlayer(WizardStoneCraft.getInstance().getMessage("auto_ban_message"));
                Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), WizardStoneCraft.getInstance().getMessage("ban_reason"), new Date(banDuration), "System");
            });
        } else if (newReputation <= -100) {
            // Auto-mute de 5 jours
            mutePlayer(targetId, 5);
            sender.sendMessage(WizardStoneCraft.getInstance().getMessage("auto_mute_message").replace("%player%", target.getName()));
        }
        return true;
    }



    public void mutePlayer(UUID playerId, int days) {
        WizardStoneCraft.getInstance().mutedPlayers.put(playerId, System.currentTimeMillis() + (days * 24L * 60 * 60 * 1000));
    }



    private void checkMutedPlayers() {
        long currentTime = System.currentTimeMillis();
        WizardStoneCraft.getInstance().mutedPlayers.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;  // Pas de suggestions du tout
    }
}
