package fr.WizardStoneCraft.Listener.Gamemode.Staff;




import fr.WizardStoneCraft.Manager.StaffModeManager;
import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StaffModeListener implements Listener {

    private final StaffModeManager staffModeManager;
    private final WizardStoneCraft plugin;

    public StaffModeListener(StaffModeManager staffModeManager, WizardStoneCraft plugin) {
        this.staffModeManager = staffModeManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player staff = event.getPlayer();

        // Vérifie si joueur est en mode staff
        if (!staffModeManager.isInStaffMode(staff)) return;

        // On ne gère que la main principale
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = staff.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String itemName = meta.getDisplayName();

        // Récupérer le joueur ciblé par le staff
        Player target = getTargetPlayer(staff);
        if (target == null) {
            staff.sendMessage("§cAucun joueur ciblé à portée.");
            return;
        }

        event.setCancelled(true); // annule l'action par défaut

        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

        switch (itemName) {
            case "§bTéléportation" -> {
                Bukkit.dispatchCommand(console, "tp " + staff.getName() + " " + target.getName());
                staff.sendMessage("§aTéléporté vers §e" + target.getName());
            }
            case "§eInspecter l’inventaire" -> {
                staff.openInventory(target.getInventory());
                staff.sendMessage("§aOuverture de l'inventaire de §e" + target.getName());
            }
            case "§cMute" -> {
                Bukkit.dispatchCommand(console, "mute " + target.getName() + " 15m insulte");
                staff.sendMessage("§aLe joueur §e" + target.getName() + " §aa été mute pour 15 minutes (§cinsulte§a).");
            }
            case "§dVoir l'EnderChest" -> {
                staff.openInventory(target.getEnderChest());
                staff.sendMessage("§aVisualisation de l'EnderChest de §e" + target.getName());
            }
            case "§cKill" -> {
                Bukkit.dispatchCommand(console, "kill " + target.getName());
                staff.sendMessage("§cLe joueur §e" + target.getName() + " §ca été tué.");
            }
            default -> staff.sendMessage("§cItem non reconnu.");
        }
    }

    // Méthode pour récupérer le joueur ciblé (dans une portée de 5 blocs, visible)
    private Player getTargetPlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(player)) continue;
            if (player.getLocation().distance(p.getLocation()) <= 5 && player.hasLineOfSight(p)) {
                return p;
            }
        }
        return null;
    }
}

