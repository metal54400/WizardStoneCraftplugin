package fr.WizardStoneCraft.data;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class ShopItem {
    public final String id;
    public final Material material;
    public final String name;
    public final List<String> lore;
    public final int price;
    public final Map<Enchantment, Integer> enchantments;

    public ShopItem(String id, Material material, String name, List<String> lore, int price, Map<Enchantment, Integer> enchantments) {
        this.id = id;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.price = price;
        this.enchantments = enchantments;
    }

    public ItemStack toItemStack() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
        }

        return item;
    }
}
