package fr.WizardStoneCraft.Enum;


import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BundleSession {
    private final ItemStack bundle;
    private final Inventory inventory;

    public BundleSession(ItemStack bundle, Inventory inventory) {
        this.bundle = bundle;
        this.inventory = inventory;
    }

    public ItemStack getBundle() {
        return bundle;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
