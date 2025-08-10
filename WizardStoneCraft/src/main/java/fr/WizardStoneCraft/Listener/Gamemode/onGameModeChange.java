package fr.WizardStoneCraft.Listener.Gamemode;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class onGameModeChange implements Listener {
    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Vérifie si le joueur est un staff
        if (!player.hasPermission("wizardstonecraft.moderator") && !player.hasPermission("op")) {
            return;
        }

        GameMode oldGameMode = player.getGameMode(); // Mode actuel avant le changement
        GameMode newGameMode = event.getNewGameMode();

        // Crée le dossier s'il n'existe pas
        File folder = new File(WizardStoneCraft.getInstance().getDataFolder(), "inv");
        if (!folder.exists()) folder.mkdirs();

        File oldFile = new File(folder, uuid + "_" + oldGameMode.name() + ".dat");
        File newFile = new File(folder, uuid + "_" + newGameMode.name() + ".dat");

        saveInventory(player, oldFile);   // Sauvegarde l’inventaire actuel
        loadInventory(player, newFile);   // Charge l’inventaire du nouveau mode

        Bukkit.getLogger().info("[Inventaire] " + player.getName() + " passe en " + newGameMode.name() + " (inventaire chargé).");
        player.sendMessage("§7[§e?§7] Vous avez changé d'inventaire §7[§c!§7]");
    }

    private void saveInventory(Player player, File file) {
        YamlConfiguration config = new YamlConfiguration();

        // Inventaire
        config.set("contents", player.getInventory().getContents());
        config.set("armor", player.getInventory().getArmorContents());
        config.set("extra", player.getInventory().getExtraContents());

        // Expérience
        config.set("exp.level", player.getLevel());
        config.set("exp.progress", player.getExp());

        // Effets de potions
        List<String> effects = new ArrayList<>();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            effects.add(effect.getType().getName() + ":" + effect.getDuration() + ":" + effect.getAmplifier() + ":" + effect.isAmbient() + ":" + effect.hasParticles());
        }
        config.set("potion-effects", effects);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadInventory(Player player, File file) {
        if (!file.exists()) {
            player.getInventory().clear();
            player.setLevel(0);
            player.setExp(0);
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Inventaire
        List<?> contents = config.getList("contents");
        List<?> armor = config.getList("armor");
        List<?> extra = config.getList("extra");

        if (contents != null)
            player.getInventory().setContents(contents.toArray(new ItemStack[0]));

        if (armor != null)
            player.getInventory().setArmorContents(armor.toArray(new ItemStack[0]));

        if (extra != null)
            player.getInventory().setExtraContents(extra.toArray(new ItemStack[0]));

        // Expérience
        player.setLevel(config.getInt("exp.level", 0));
        player.setExp((float) config.getDouble("exp.progress", 0));

        // Effets de potions
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        List<String> effects = config.getStringList("potion-effects");
        for (String str : effects) {
            String[] parts = str.split(":");
            if (parts.length >= 5) {
                try {
                    PotionEffectType type = PotionEffectType.getByName(parts[0]);
                    int duration = Integer.parseInt(parts[1]);
                    int amplifier = Integer.parseInt(parts[2]);
                    boolean ambient = Boolean.parseBoolean(parts[3]);
                    boolean particles = Boolean.parseBoolean(parts[4]);

                    if (type != null) {
                        PotionEffect effect = new PotionEffect(type, duration, amplifier, ambient, particles);
                        player.addPotionEffect(effect);
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Erreur lors du chargement d'un effet de potion : " + str);
                }
            }
        }
    }
}
