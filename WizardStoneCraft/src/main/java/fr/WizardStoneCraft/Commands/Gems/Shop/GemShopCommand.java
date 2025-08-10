package fr.WizardStoneCraft.Commands.Gems.Shop;

import fr.WizardStoneCraft.Manager.GemShopManager;
import fr.WizardStoneCraft.data.ShopItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public class GemShopCommand implements CommandExecutor {

    private final GemShopManager shopManager;

    public GemShopCommand(GemShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cCommande réservée aux joueurs.");
            return true;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "§6Boutique des Gems");

        for (Map.Entry<Integer, ShopItem> entry : shopManager.getShopItems().entrySet()) {
            inv.setItem(entry.getKey(), entry.getValue().toItemStack());
        }

        player.openInventory(inv);
        return true;
    }
}

