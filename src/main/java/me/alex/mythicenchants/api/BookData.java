package me.alex.mythicenchants.api;

import java.util.Objects;
import org.bukkit.NamespacedKey;

public record BookData(NamespacedKey enchantId, int level, int successChance, int destroyChance) {
    public BookData {
        Objects.requireNonNull(enchantId, "enchantId");
        if (level < 1 || level > 3999) throw new IllegalArgumentException("Level must be 1..3999");
        if (successChance < 1 || successChance > 100 || destroyChance < 1 || destroyChance > 100) {
            throw new IllegalArgumentException("Chances must be 1..100");
        }
    }
}
