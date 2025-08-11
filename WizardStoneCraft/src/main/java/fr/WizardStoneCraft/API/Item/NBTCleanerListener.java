package fr.WizardStoneCraft.API.Item;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
 // Hypothétique, à remplacer par le vrai event BeautyQuests

public class NBTCleanerListener implements Listener {

    @EventHandler
    public void onQuestItemCheck(QuestItemCheckEvent event) {
        ItemStack item = event.getItem();
        if (item != null) {
            event.setItem(cleanItemNBT(item));
        }
    }

    public static ItemStack cleanItemNBT(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(null);
        meta.setLore(null);
        // Si besoin, enlever d'autres données personnalisées
        item.setItemMeta(meta);

        return item;
    }


}
