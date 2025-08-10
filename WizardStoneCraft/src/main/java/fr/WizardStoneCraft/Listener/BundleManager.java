package fr.WizardStoneCraft.Listener;

import fr.WizardStoneCraft.Enum.BundleSession;
import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.*;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.*;

public class BundleManager implements Listener {

    private final WizardStoneCraft plugin;
    private final Map<UUID, BundleSession> sessions = new HashMap<>();
    private final NamespacedKey key;

    public BundleManager(WizardStoneCraft plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "bundle_data");
    }

    @EventHandler
    public void onBundleUse(PlayerInteractEvent event) {
        if (event.getItem() == null || event.getItem().getType() != Material.BUNDLE) return;
        if (!event.getHand().equals(EquipmentSlot.HAND)) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        ItemStack bundle = player.getInventory().getItemInMainHand(); // ← CORRECT

        Inventory inv = Bukkit.createInventory(null, 27, "Bundle Étendu");

        loadInventoryFromBundle(bundle, inv);

        sessions.put(player.getUniqueId(), new BundleSession(bundle, inv));
        player.openInventory(inv);
    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!sessions.containsKey(uuid)) return;

        BundleSession session = sessions.remove(uuid);
        saveInventoryToBundles(session.getBundle(), session.getInventory());
    }

    private void saveInventoryToBundles(ItemStack bundle, Inventory inv) {
        try {
            Map<Integer, ItemStack> serializedContents = new HashMap<>();

            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && !item.isSimilar(bundle)) {
                    serializedContents.put(i, item);
                }
            }

            // Écriture de la map (index -> item)
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOut = new BukkitObjectOutputStream(output);
            dataOut.writeObject(serializedContents);
            dataOut.close();

            ItemMeta meta = bundle.getItemMeta();
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE_ARRAY, output.toByteArray());
            bundle.setItemMeta(meta);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadInventoryFromBundle(ItemStack bundle, Inventory inv) {
        ItemMeta meta = bundle.getItemMeta();
        if (meta == null) return;

        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE_ARRAY)) return;

        try {
            byte[] data = meta.getPersistentDataContainer().get(key, PersistentDataType.BYTE_ARRAY);
            if (data == null) return;

            ByteArrayInputStream input = new ByteArrayInputStream(data);
            BukkitObjectInputStream dataIn = new BukkitObjectInputStream(input);

            Object obj = dataIn.readObject();
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Integer, ItemStack> serializedContents = (Map<Integer, ItemStack>) obj;

                for (Map.Entry<Integer, ItemStack> entry : serializedContents.entrySet()) {
                    if (entry.getKey() < inv.getSize()) {
                        inv.setItem(entry.getKey(), entry.getValue());
                    }
                }
            }

            dataIn.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
