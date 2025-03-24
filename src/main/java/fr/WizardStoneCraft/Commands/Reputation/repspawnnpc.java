package fr.WizardStoneCraft.Commands.Reputation;

import fr.WizardStoneCraft.Commands.PassiveCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public class repspawnnpc implements CommandExecutor {
    private static repspawnnpc instance;
    public repspawnnpc getInstance(){
        return instance;
    }
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("repspawnnpc")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                // Vérifier si le joueur a la permission d'exécuter cette commande
                if (player.hasPermission("wizardstonecraft.repspawnnpc")) {
                    // Invoquer le NPC "Pacificateur" à la position du joueur
                    spawnPacificateur(player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "§7[§e?§7]§a Le Pacificateur a été invoqué à votre position §7[§c!§7]");
                } else {
                    player.sendMessage(ChatColor.RED + "§7[§e?§7]§c Vous n'avez pas la permission d'exécuter cette commande. §7[§c!§7]");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "§7[§e?§7]§e Cette commande ne peut être exécutée que par un joueur. §7[§c!§7]");
            }
            return true;
        }
        return true;
    }
    private void spawnPacificateur(Location location) {
        World world = location.getWorld();
        if (world != null) {
            // Supprimer les anciens NPC "Pacificateur"
            world.getEntitiesByClass(Villager.class).stream()
                    .filter(v -> v.getCustomName() != null && v.getCustomName().equals("Pacificateur"))
                    .forEach(Villager::remove);
            // Invoquer un nouveau NPC "Pacificateur"
            Villager pacificateur = world.spawn(location, Villager.class);
            pacificateur.setCustomName("§7[§e?§7]§a Pacificateur");
            pacificateur.setCustomNameVisible(true);
            pacificateur.setAI(false);
        }
    }
    public static boolean isPassive(Player player) {
        return PassiveCommand.passivePlayers.getOrDefault(player.getUniqueId(), false);
    }
}
