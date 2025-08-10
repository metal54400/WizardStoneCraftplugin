package fr.WizardStoneCraft.Listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PassiveModeGUI implements Listener {

    private final Inventory gui;

    public PassiveModeGUI() {
        gui = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "Mode Passif");

        ItemStack greenWool = new ItemStack(Material.GREEN_WOOL);
        ItemMeta greenMeta = greenWool.getItemMeta();
        greenMeta.setDisplayName(ChatColor.GREEN + "Activer le mode passif");
        greenWool.setItemMeta(greenMeta);

        ItemStack redWool = new ItemStack(Material.RED_WOOL);
        ItemMeta redMeta = redWool.getItemMeta();
        redMeta.setDisplayName(ChatColor.RED + "Désactiver le mode passif");
        redWool.setItemMeta(redMeta);

        gui.setItem(3, greenWool);
        gui.setItem(5, redWool);
    }

    public void open(Player player) {
        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() == null) return;
        if (!event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Mode Passif")) return;

        event.setCancelled(true); // Empêche de prendre/déplacer les items

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = clicked.getItemMeta().getDisplayName();

        if (name.equals(ChatColor.GREEN + "Activer le mode passif")) {
            // Execute /passifset via console
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "passifset " + player.getName());
            player.sendMessage(ChatColor.GREEN + "Mode passif activé !");
            PvpListener.updateNametag(player);
            player.closeInventory();
        } else if (name.equals(ChatColor.RED + "Désactiver le mode passif")) {
            // Remove directly without command, then update nametag
            PvpListener.removePassiveMode(player);
            player.sendMessage(ChatColor.RED + "Mode passif désactivé !");
            player.closeInventory();
        }
    }
}
