package fr.WizardStoneCraft.Commands.Gems.Shop;

import fr.WizardStoneCraft.Manager.GemManager;
import fr.WizardStoneCraft.Manager.GemShopManager;
import fr.WizardStoneCraft.Shop.ShopItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GemShopListener implements Listener {

    private final GemManager gemManager;
    private final GemShopManager shopManager;

    public GemShopListener(GemManager gemManager, GemShopManager shopManager) {
        this.gemManager = gemManager;
        this.shopManager = shopManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals("§6Boutique des Gems")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ShopItem shopItem = shopManager.getByItem(clicked);
        if (shopItem == null) return;

        int currentGems = gemManager.getGems(player.getName());

        if (currentGems < shopItem.price) {
            player.sendMessage("§cTu n’as pas assez de gems !");
            return;
        }

        gemManager.addGems(player.getName(), -shopItem.price);
        player.getInventory().addItem(shopItem.toItemStack());
        player.sendMessage("§aTu as acheté : §e" + shopItem.name + " §apour §e" + shopItem.price + " gems !");
    }
}
