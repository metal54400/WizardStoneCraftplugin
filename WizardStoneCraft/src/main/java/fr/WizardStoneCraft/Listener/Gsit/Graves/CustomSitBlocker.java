package fr.WizardStoneCraft.Listener.Gsit.Graves;

import com.ranull.graves.Graves;
import com.ranull.graves.data.EntityData;
import com.ranull.graves.data.LocationData;
import dev.cwhead.GravesX.GravesXAPI;
import dev.geco.gsit.object.GSeat;
import fr.WizardStoneCraft.API.event.PlayerAttemptSitEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CustomSitBlocker implements Listener {

    private final GravesXAPI gravesXAPI;

    public CustomSitBlocker() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("GravesX");
        if (plugin == null || !plugin.isEnabled()) {
            Bukkit.getLogger().warning("[WizardStoneCraft] ⚠️ GravesX non chargé ou désactivé.");
            this.gravesXAPI = null;
            return;
        }

        if (!(plugin instanceof Graves)) {
            Bukkit.getLogger().warning("[WizardStoneCraft] ⚠️ Le plugin GravesX n'est pas une instance de Graves.");
            this.gravesXAPI = null;
            return;
        }

        Graves gravesPlugin = (Graves) plugin;
        GravesXAPI apiInstance = new GravesXAPI(gravesPlugin).getInstance();
        this.gravesXAPI = apiInstance;

        if (this.gravesXAPI == null) {
            Bukkit.getLogger().warning("[WizardStoneCraft] ⚠️ GravesXAPI non trouvé. Le blocage de tombes ne fonctionnera pas.");
        }
    }

    @EventHandler
    public void onPlayerAttemptSit(PlayerAttemptSitEvent event) {
        if (gravesXAPI == null) return;

        GSeat seat = event.getSeat();
        Entity entity = seat.getSeatEntity();

        if (!(entity instanceof ArmorStand armorStand)) return;

        Location armorStandLoc = armorStand.getLocation();

        // 1) Vérifier si tombe présente à l'emplacement ou juste en dessous
        LocationData graveData = gravesXAPI.getLocationData(armorStandLoc);
        if (graveData == null) {
            graveData = gravesXAPI.getLocationData(armorStandLoc.clone().subtract(0, 1, 0));
        }

        if (graveData != null) {
            Location graveLoc = graveData.getLocation();
            UUID worldUUID = graveLoc.getWorld().getUID();

            // Puisque getEntityData demande Location, deux UUID, et Type
            // Je suppose que uuid1 = world UUID (ou autre UUID lié à la tombe)
            // et uuid2 = grave UUID (pas dans LocationData, donc on peut passer null ou inventer)

            // Ici, on va essayer d'appeler getEntityData pour ARMOR_STAND
            // Si tu as un UUID spécifique pour la tombe, remplace null par cet UUID

            EntityData entityData = gravesXAPI.getEntityData(
                    graveLoc,
                    worldUUID,
                    null,  // UUID de la tombe si possible, sinon null
                    EntityData.Type.ARMOR_STAND
            );

            List<EntityData> entities = entityData == null ? Collections.emptyList() : Collections.singletonList(entityData);


            if (entities != null) {
                boolean isGraveArmorStand = entities.stream()
                        .anyMatch(ed -> ed.getLocation().getBlock().equals(armorStandLoc.getBlock()));

                if (isGraveArmorStand) {
                    event.setCancelled(true);
                    Bukkit.getLogger().info("[CustomSitBlocker] Sit annulé sur armorstand de tombe à " + armorStandLoc);
                    return;
                }
            }
        }

        // Sinon, bloquer sit sur tête de joueur si demandé
        Block blockBelow = armorStandLoc.clone().subtract(0, 1, 0).getBlock();
        boolean disableSitOnHeads = true;
        if (disableSitOnHeads &&
                (blockBelow.getType() == Material.PLAYER_HEAD || blockBelow.getType() == Material.PLAYER_WALL_HEAD)) {
            event.setCancelled(true);
            Bukkit.getLogger().info("[CustomSitBlocker] Sit annulé sur tête de joueur à " + blockBelow.getLocation());
        }
    }






}
