package me.alex.mythicenchants.api;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import me.alex.mythicenchants.enchant.EnchantTier;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public record EnchantDefinition(NamespacedKey id, String displayName, EnchantTier tier,
                               int maxLevel, List<String> description, Set<Material> materials) {
    public EnchantDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(tier, "tier");
        if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("Display name is required");
        if (maxLevel < 1 || maxLevel > 3999) throw new IllegalArgumentException("max-level must be 1..3999");
        description = List.copyOf(description);
        materials = Set.copyOf(materials);
        if (materials.isEmpty() || materials.stream().anyMatch(m -> !m.isItem() || m.isAir() || m.getMaxStackSize() != 1)) {
            throw new IllegalArgumentException("Materials must be non-empty, non-stackable gear items");
        }
    }
}
