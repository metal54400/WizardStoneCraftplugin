package fr.WizardStoneCraft.Commands.Anticheat;

import fr.WizardStoneCraft.WizardStoneCraft;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.*;

public class Topluck implements CommandExecutor, Listener {

    private final JavaPlugin plugin;

    // Liste des minerais qu'on veut tracker
    private static final Set<Material> TRACKED_MINERALS = Set.of(
            Material.DIAMOND_ORE,
            Material.IRON_ORE,
            Material.COAL_ORE,
            Material.GOLD_ORE,
            Material.REDSTONE_ORE,
            Material.LAPIS_ORE,
            Material.EMERALD_ORE,
            Material.NETHER_GOLD_ORE,
            Material.ANCIENT_DEBRIS,
            Material.NETHER_QUARTZ_ORE
    );

    public Topluck(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Lorsqu’un joueur casse un bloc (minerai), on enregistre
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        if (!TRACKED_MINERALS.contains(blockType)) return;

        String playerName = player.getName();
        var config = WizardStoneCraft.getInstance().topluckConfig;

        // Récupération du compteur actuel
        int currentCount = config.getInt("players." + playerName + "." + blockType.name(), 0);
        config.set("players." + playerName + "." + blockType.name(), currentCount + 1);

        // Mettre à jour la date du dernier minage
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        config.set("players." + playerName + ".last_mine", now);

        // Sauvegarde config
        WizardStoneCraft.getInstance().saveTopluckConfig();
    }

    // Lorsqu’un joueur tue un autre joueur, on compte les kills
    @EventHandler
    public void onPlayerKill(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;

        String killerName = killer.getName();
        var config = WizardStoneCraft.getInstance().topluckConfig;

        int currentKills = config.getInt("players." + killerName + ".kills", 0);
        config.set("players." + killerName + ".kills", currentKills + 1);

        WizardStoneCraft.getInstance().saveTopluckConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSeuls les joueurs peuvent exécuter cette commande.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("anticheat.topluck")) {
            player.sendMessage("§cVous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        var config = WizardStoneCraft.getInstance().topluckConfig;
        if (config.getConfigurationSection("players") == null || config.getConfigurationSection("players").getKeys(false).isEmpty()) {
            player.sendMessage("§cAucun joueur n'a encore miné de minerai.");
            return true;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Top Luck");

        for (String playerName : config.getConfigurationSection("players").getKeys(false)) {
            // Calcul total minerai miné
            int totalMined = 0;
            int diamondMined = config.getInt("players." + playerName + ".DIAMOND_ORE", 0);

            for (Material mat : TRACKED_MINERALS) {
                totalMined += config.getInt("players." + playerName + "." + mat.name(), 0);
            }

            double ratio = totalMined == 0 ? 0.0 : ((double) diamondMined / totalMined) * 100;

            int kills = config.getInt("players." + playerName + ".kills", 0);
            String lastMine = config.getString("players." + playerName + ".last_mine", "N/A");

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
                meta.setDisplayName("§6" + playerName + " §f- Minerais: §e" + totalMined);
                List<String> lore = new ArrayList<>();
                lore.add("§dDiamants : §f" + diamondMined);
                lore.add("§dRatio diamants/minerais : §f" + String.format("%.2f", ratio) + " %");
                lore.add("§cKills : §f" + kills);
                lore.add("§7Dernier minage : §f" + lastMine);
                meta.setLore(lore);
                skull.setItemMeta(meta);
            }

            inv.addItem(skull);
        }

        player.openInventory(inv);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();

        if (!(clicker instanceof Player)) return;

        InventoryView view = event.getView();

        if (ChatColor.stripColor(view.getTitle()).equalsIgnoreCase("Top Luck")) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);

            // Facultatif: Fermer l'inventaire si clic sur le contenu
            if (event.getClickedInventory() != null && event.getClickedInventory().equals(view.getTopInventory())) {
                clicker.closeInventory();
            }
        }
    }
}
