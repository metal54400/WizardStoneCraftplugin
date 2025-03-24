package fr.WizardStoneCraft.Commands;

import fr.WizardStoneCraft.WizardStoneCraft;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CookFoodCommand implements CommandExecutor {

    private static CookFoodCommand instance;
    public CookFoodCommand getInstance(){
        return instance;
    }

    private static final int REQUIRED_REPUTATION = 10;
    private static final Map<Material, Material> COOKABLE_FOODS = new HashMap<>();

    static {
        COOKABLE_FOODS.put(Material.LEGACY_RAW_BEEF, Material.COOKED_BEEF);
        COOKABLE_FOODS.put(Material.LEGACY_RAW_CHICKEN, Material.COOKED_CHICKEN);
        COOKABLE_FOODS.put(Material.PORKCHOP, Material.COOKED_PORKCHOP);
        COOKABLE_FOODS.put(Material.SALMON, Material.COOKED_SALMON);
        COOKABLE_FOODS.put(Material.COD, Material.COOKED_COD);
        COOKABLE_FOODS.put(Material.RABBIT, Material.COOKED_RABBIT);
        COOKABLE_FOODS.put(Material.MUTTON, Material.COOKED_MUTTON);
        COOKABLE_FOODS.put(Material.POTATO, Material.POTATOES);
    }

    static {
        COOKABLE_FOODS.put(Material.BEEF, Material.COOKED_BEEF);
        COOKABLE_FOODS.put(Material.CHICKEN, Material.COOKED_CHICKEN);
        COOKABLE_FOODS.put(Material.PORKCHOP, Material.COOKED_PORKCHOP);
        COOKABLE_FOODS.put(Material.MUTTON, Material.COOKED_MUTTON);
        COOKABLE_FOODS.put(Material.RABBIT, Material.COOKED_RABBIT);
        COOKABLE_FOODS.put(Material.SALMON, Material.COOKED_SALMON);
        COOKABLE_FOODS.put(Material.COD, Material.COOKED_COD);
        COOKABLE_FOODS.put(Material.POTATO, Material.POTATOES);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seuls les joueurs peuvent exécuter cette commande !");
            return true;
        }

        Player player = (Player) sender;

        // Vérifie la réputation du joueur (remplace getReputation avec ta propre méthode)
        int reputation = WizardStoneCraft.getInstance().getReputation(player);
        if (reputation < 10) {
            player.sendMessage(ChatColor.RED + "§7[§e?§7]§2 Vous devez avoir au moins 10 de réputation pour cuire votre nourriture !");
            return true;
        }

        // Vérifie et cuit la nourriture
        boolean foundFood = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && COOKABLE_FOODS.containsKey(item.getType())) {
                Material cooked = COOKABLE_FOODS.get(item.getType());
                item.setType(cooked);
                foundFood = true;
            }
        }

        if (foundFood) {
            player.sendMessage(ChatColor.GREEN + "§7[§e?§7]§a Votre nourriture a été cuite !");
        } else {
            player.sendMessage(ChatColor.YELLOW + "§7[§e?§7]§e Vous n'avez pas de nourriture crue à cuire.");
        }

        return true;
    }
}
