package fr.WizardStoneCraft.data;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum QuestType {
    FARMING(EnumSet.of(Material.WHEAT, Material.CARROTS, Material.POTATOES)),
    PLANTING(EnumSet.of(Material.WHEAT_SEEDS, Material.CARROT, Material.POTATO)),
    MINING_COMPLEX(EnumSet.of(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.NETHER_QUARTZ_ORE)),
    COOKING(EnumSet.of(Material.COOKED_BEEF, Material.COOKED_CHICKEN, Material.BREAD)),
    EATING_BIOMES(EnumSet.of(Material.APPLE, Material.SWEET_BERRIES, Material.DRIED_KELP)),
    BOSS_TRIO(EnumSet.of(EntityType.ENDER_DRAGON, EntityType.WITHER, EntityType.ELDER_GUARDIAN)),
    SPECIFIC_BOSS(EnumSet.of(EntityType.WITHER)),
    VILLAGERZOMBIE(EnumSet.of(EntityType.ZOMBIE_VILLAGER)),
    BREZZE(EnumSet.of(EntityType.BREEZE)), // Exemple
    PVP_DUEL(Collections.emptySet()),      // PvP = aucun mob ciblé
    CUSTOM(Collections.emptySet());        // Type générique pour quêtes personnalisées

    private final Set<?> targets;

    QuestType(Set<?> targets) {
        this.targets = targets;
    }

    /**
     * Récupère les cibles de type Material si pertinentes,
     * sinon renvoie un Set vide.
     */
    public Set<Material> getMaterialTargets() {
        if (!targets.isEmpty() && targets.iterator().next() instanceof Material) {
            @SuppressWarnings("unchecked")
            Set<Material> materials = (Set<Material>) targets;
            return materials;
        }
        return Collections.emptySet();
    }

    /**
     * Récupère les cibles de type EntityType si pertinentes,
     * sinon renvoie un Set vide.
     */
    public Set<EntityType> getEntityTargets() {
        if (!targets.isEmpty() && targets.iterator().next() instanceof EntityType) {
            @SuppressWarnings("unchecked")
            Set<EntityType> entities = (Set<EntityType>) targets;
            return entities;
        }
        return Collections.emptySet();
    }

    /**
     * Vérifie si l'entité passée est une cible valide pour cette quête.
     */
    public boolean isEntityTarget(EntityType entityType) {
        return getEntityTargets().contains(entityType);
    }

    /**
     * Vérifie si le matériau passé est une cible valide pour cette quête.
     */
    public boolean isMaterialTarget(Material material) {
        return getMaterialTargets().contains(material);
    }
}
