package fr.WizardStoneCraft.Manager;

import fr.WizardStoneCraft.WizardStoneCraft;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StaffModeManager {

    private final WizardStoneCraft plugin;
    private final Set<UUID> staffModePlayers = new HashSet<>();
    private final LuckPerms luckPerms;

    public StaffModeManager(WizardStoneCraft plugin) {
        this.plugin = plugin;
        this.luckPerms = LuckPermsProvider.get();
    }

    public boolean isInStaffMode(Player player) {
        return staffModePlayers.contains(player.getUniqueId());
    }

    public void toggleStaffMode(Player player) {
        if (isInStaffMode(player)) {
            disableStaffMode(player);
        } else {
            enableStaffMode(player);
        }
    }

    public void enableStaffMode(Player player) {
        staffModePlayers.add(player.getUniqueId());

        // LuckPerms: ajouter au groupe "hiddenstaff"
        addStaffGroup(player);

        // TAB: masquer du tablist
        setTabGroupHidden(player);

        // Capacité + mode
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvisible(true);
        player.setInvulnerable(true);

        // Inventaire
        player.getInventory().clear();
        giveStaffItems(player);

        // Masquer aux non-staff uniquement
        hideFromNonStaff(player);

        // Broadcast
        Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.RED + "–" + ChatColor.GRAY + "] " + ChatColor.RESET + player.getName());

        player.sendMessage("§7[§aStaff§7] §aMode staff activé.");
    }

    public void disableStaffMode(Player player) {
        staffModePlayers.remove(player.getUniqueId());

        // LuckPerms: retirer du groupe
        removeStaffGroup(player);

        // TAB: réinitialiser
        resetTabGroup(player);

        // Capacité + mode
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setInvisible(false);
        player.setInvulnerable(false);

        // Inventaire
        player.getInventory().clear();

        // Ne pas remontrer aux joueurs (nécessite déco/reco)
        // showToAll(player);

        // Broadcast
        Bukkit.broadcastMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "+" + ChatColor.GRAY + "] " + ChatColor.RESET + player.getName());

        player.sendMessage("§7[§cStaff§7] §cMode staff désactivé. §7Reconnecte-toi pour revoir les autres joueurs.");
    }

    private void giveStaffItems(Player player) {
        player.getInventory().setItem(0, createItem(Material.COMPASS, "§bTéléportation"));
        player.getInventory().setItem(1, createItem(Material.CHEST, "§eInspecter l’inventaire"));
        player.getInventory().setItem(2, createItem(Material.BARRIER, "§cMute"));
        player.getInventory().setItem(3, createItem(Material.ENDER_CHEST, "§dVoir l'EnderChest"));
        player.getInventory().setItem(4, createItem(Material.NETHERITE_AXE, "§cKill"));
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void hideFromNonStaff(Player staff) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!isInStaffMode(online)) {
                online.hidePlayer(plugin, staff); // le staff devient invisible pour les non-staff
                // staff.hidePlayer(plugin, online); // NE cache PAS les autres joueurs au staff
            }
        }
    }

    private void setTabGroupHidden(Player player) {
        TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
        if (tabPlayer != null) {
            tabPlayer.setTemporaryGroup("hiddenstaff"); // Assure-toi que ce groupe existe dans TAB config
        }
    }

    private void resetTabGroup(Player player) {
        TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
        if (tabPlayer != null) {
            tabPlayer.setTemporaryGroup(null);
        }
    }

    private void addStaffGroup(Player player) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            InheritanceNode node = InheritanceNode.builder("hiddenstaff").build();
            user.data().add(node);
            luckPerms.getUserManager().saveUser(user);
        }
    }

    private void removeStaffGroup(Player player) {
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            InheritanceNode node = InheritanceNode.builder("hiddenstaff").build();
            user.data().remove(node);
            luckPerms.getUserManager().saveUser(user);
        }
    }
}
