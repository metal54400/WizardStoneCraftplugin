package fr.WizardStoneCraft.API.Item;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder setName(String name) {
        meta.setDisplayName(name);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        meta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        meta.setLore(lore);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level, boolean force) {
        // Vérification de l'enchantement pour s'assurer qu'il est valide pour cet objet
        if (meta instanceof Enchantment) {
            meta.addEnchant(enchantment, level, force);
        }
        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder setCustomModelData(int data) {
        meta.setCustomModelData(data);
        return this;
    }

    public ItemBuilder setLeatherColor(Color color) {
        if (meta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(color);
        } else {
            throw new IllegalArgumentException("Cet item n'est pas une armure en cuir.");
        }
        return this;
    }

    public ItemBuilder setSkullOwner(String owner) {
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwner(owner);
        } else {
            throw new IllegalArgumentException("Cet item n'est pas une tête.");
        }
        return this;
    }

    public ItemBuilder addPotionEffect(PotionEffect effect) {
        if (meta instanceof PotionMeta potionMeta) {
            potionMeta.addCustomEffect(effect, true);
        }
        return this;
    }

    public ItemBuilder addStoredEnchant(Enchantment enchantment, int level) {
        if (meta instanceof EnchantmentStorageMeta bookMeta) {
            bookMeta.addStoredEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemBuilder setArmorTrim(Player viewer, TrimMaterial material, TrimPattern pattern) {
        if (meta instanceof ArmorMeta armorMeta) {
            if (armorMeta.hasTrim()) {
                // Empêcher l'ajout de trim si l'armure en a déjà un
                throw new IllegalStateException("Cette armure a déjà un trim.");
            }
            ArmorTrim trim = new ArmorTrim(material, pattern);
            armorMeta.setTrim(trim);
        } else {
            throw new IllegalArgumentException("Cet item n'est pas une armure.");
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}
