package fr.WizardStoneCraft.Commands.Reputation;


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
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§7[§e?§7]§c Cette commande ne peut être exécutée que par un joueur.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("wizardstonecraft.repspawnnpc")) {
            player.sendMessage("§7[§e?§7]§c Vous n'avez pas la permission d'exécuter cette commande.");
            return true;
        }

        spawnPacificateur(player.getLocation());
        player.sendMessage("§7[§e?§7]§a Le Pacificateur a été invoqué à votre position.");
        return true;
    }

    private void spawnPacificateur(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        // Supprimer anciens
        world.getEntitiesByClass(Villager.class).stream()
                .filter(v -> "§7[§e?§7]§a Pacificateur".equals(v.getCustomName()))
                .forEach(Villager::remove);

        // Nouveau
        Villager pacificateur = world.spawn(location, Villager.class);
        pacificateur.setCustomName("§7[§e?§7]§a Pacificateur");
        pacificateur.setCustomNameVisible(true);
        pacificateur.setAI(false);
        pacificateur.setCollidable(false);
        pacificateur.setInvulnerable(true);
        pacificateur.setSilent(true);
    }
}

