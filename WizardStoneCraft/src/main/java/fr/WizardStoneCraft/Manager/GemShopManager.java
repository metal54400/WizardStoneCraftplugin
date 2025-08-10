package fr.WizardStoneCraft.Manager;

import fr.WizardStoneCraft.data.ShopItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GemShopManager {
    private final Map<Integer, ShopItem> slotToItem = new HashMap<>();
    private final Map<String, ShopItem> idToItem = new HashMap<>();

    public GemShopManager(JavaPlugin plugin) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "shop.yml"));
        ConfigurationSection shopSection = config.getConfigurationSection("shop");
        int slot = 0;

        if (shopSection != null) {
            for (String key : shopSection.getKeys(false)) {
                ConfigurationSection itemSec = shopSection.getConfigurationSection(key);
                Material mat = Material.getMaterial(itemSec.getString("material", "DIRT").toUpperCase());

                String name = itemSec.getString("name", key);
                List<String> lore = itemSec.getStringList("lore");
                int price = itemSec.getInt("price", 0);

                Map<Enchantment, Integer> enchants = new HashMap<>();
                ConfigurationSection enchSec = itemSec.getConfigurationSection("enchantments");
                if (enchSec != null) {
                    for (String ench : enchSec.getKeys(false)) {
                        Enchantment enchantment = Enchantment.getByName(ench.toUpperCase());
                        if (enchantment != null) {
                            enchants.put(enchantment, enchSec.getInt(ench));
                        }
                    }
                }

                ShopItem item = new ShopItem(key, mat, name, lore, price, enchants);
                slotToItem.put(slot, item);
                idToItem.put(key, item);
                slot++;
            }
        }
    }

    public Map<Integer, ShopItem> getShopItems() {
        return slotToItem;
    }

    public ShopItem getByItem(ItemStack stack) {
        for (ShopItem item : idToItem.values()) {
            if (stack.getItemMeta() != null &&
                    stack.getItemMeta().getDisplayName().equals(item.name)) {
                return item;
            }
        }
        return null;
    }
}
