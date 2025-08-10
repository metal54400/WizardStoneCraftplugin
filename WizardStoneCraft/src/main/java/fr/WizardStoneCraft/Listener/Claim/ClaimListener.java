package fr.WizardStoneCraft.Listener.Claim;

import fr.WizardStoneCraft.WizardStoneCraft;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class ClaimListener implements Listener {

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Vérifier si le joueur tient un bâton et fait un clic droit
        if (item.getType() == Material.STICK && event.getAction().toString().contains("RIGHT_CLICK")) {
            Location loc = player.getLocation();
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);

            if (claim == null) {
                player.sendMessage(ChatColor.RED + "§7(§c!§7)§c Ce terrain n'est pas claim.");
                return;
            }

            String owner = claim.getOwnerName();
            player.sendMessage(ChatColor.GREEN + "§7(§e!§7) Ce terrain appartient à : " + ChatColor.GOLD + owner);

            // Récupérer les coins du claim
            Location min = claim.getLesserBoundaryCorner();
            Location max = claim.getGreaterBoundaryCorner();
            World world = min.getWorld();

            // Vérifier si le monde est valide
            if (world == null) {
                player.sendMessage(ChatColor.RED + "§7(§c!§7)§c Erreur : Impossible de récupérer le monde.");
                return;
            }

            // Utilisation d'un HashSet pour éviter les doublons et améliorer la performance
            Set<Block> placedBlocks = new HashSet<>();

            // Placer les bordures avec laine jaune et torches
            for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
                addBlockIfValid(world, x, min.getBlockY(), min.getBlockZ(), Material.YELLOW_WOOL, placedBlocks);
                addBlockIfValid(world, x, min.getBlockY(), max.getBlockZ(), Material.YELLOW_WOOL, placedBlocks);
            }
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                addBlockIfValid(world, min.getBlockX(), min.getBlockY(), z, Material.YELLOW_WOOL, placedBlocks);
                addBlockIfValid(world, max.getBlockX(), min.getBlockY(), z, Material.YELLOW_WOOL, placedBlocks);
            }

            // Ajouter glowstone aux coins du claim
            addBlockIfValid(world, min.getBlockX(), min.getBlockY(), min.getBlockZ(), Material.GLOWSTONE, placedBlocks);
            addBlockIfValid(world, max.getBlockX(), min.getBlockY(), max.getBlockZ(), Material.GLOWSTONE, placedBlocks);
            addBlockIfValid(world, min.getBlockX(), min.getBlockY(), max.getBlockZ(), Material.GLOWSTONE, placedBlocks);
            addBlockIfValid(world, max.getBlockX(), min.getBlockY(), min.getBlockZ(), Material.GLOWSTONE, placedBlocks);

            // Ajouter des torches à intervalles réguliers sur les bords
            for (int x = min.getBlockX(); x <= max.getBlockX(); x += 5) {
                addBlockIfValid(world, x, min.getBlockY() + 1, min.getBlockZ(), Material.TORCH, placedBlocks);
                addBlockIfValid(world, x, min.getBlockY() + 1, max.getBlockZ(), Material.TORCH, placedBlocks);
            }
            for (int z = min.getBlockZ(); z <= max.getBlockZ(); z += 5) {
                addBlockIfValid(world, min.getBlockX(), min.getBlockY() + 1, z, Material.TORCH, placedBlocks);
                addBlockIfValid(world, max.getBlockX(), min.getBlockY() + 1, z, Material.TORCH, placedBlocks);
            }

            // Supprimer les blocs après 10 secondes
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Block block : placedBlocks) {
                        if (block.getType() != Material.AIR) {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }.runTaskLater(WizardStoneCraft.getInstance(), 200L);
        }
    }

    // Méthode pour placer un bloc temporaire seulement si l'emplacement est libre
    private void addBlockIfValid(World world, int x, int y, int z, Material material, Set<Block> placedBlocks) {
        Block block = world.getBlockAt(x, y, z);
        if (block.getType() == Material.AIR) {
            block.setType(material);
            placedBlocks.add(block);
        }
    }

    public static boolean isInClaim(Location loc) {
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);
        return claim != null;
    }



}