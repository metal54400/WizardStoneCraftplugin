package fr.WizardStoneCraft.PlaceHolderApi;


import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;



public class PlaceHolderApi extends PlaceholderExpansion{
    private static boolean placeholderAPILoaded = false;

public static void checkPlaceholderAPI() {
    Plugin plugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
    System.out.println("§7[§e?§7] PlaceholderApi est activé");
    if (plugin != null) {
        placeholderAPILoaded = true;
    }
}

    public static String parse(Player player, String text) {
        if (placeholderAPILoaded && player != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    @Override
    public String getIdentifier() {
        return "WizardStoneCraft";
    }

    @Override
    public String getAuthor() {
        return "metal54400";
    }

    @Override
    public String getVersion() {
        return "0.0.15.1-Alpha";
    }

    @Override
    public boolean register() {
        return super.register();
    }

    @Override
    public boolean canRegister() {
        return super.canRegister();
    }
}
