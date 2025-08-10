package fr.WizardStoneCraft.data;

import org.bukkit.Material;
import java.util.EnumSet;
import org.bukkit.entity.EntityType;
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
    BREZZE(EnumSet.of(EntityType.BREEZE)),// Exemple : quête ciblée sur le Wither
    PVP_DUEL(EnumSet.noneOf(EntityType.class)); // PvP = aucun mob ciblé

    private final Set<?> targets;

    QuestType(Set<?> targets) {
        this.targets = targets;
    }

    public Set<?> getTargets() {
        return targets;
    }

    // Méthode utilitaire pour vérifier si une entité valide la quête
    public boolean isEntityTarget(EntityType entityType) {
        return targets.contains(entityType);
    }

    // Méthode utilitaire pour vérifier si un item ou un bloc valide la quête
    public boolean isMaterialTarget(Material material) {
        return targets.contains(material);
    }

}

