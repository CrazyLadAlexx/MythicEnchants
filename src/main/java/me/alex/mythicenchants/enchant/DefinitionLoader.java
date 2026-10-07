package me.alex.mythicenchants.enchant;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import me.alex.mythicenchants.api.EnchantDefinition;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public final class DefinitionLoader {
    private DefinitionLoader() {}
    public static Map<NamespacedKey, EnchantDefinition> load(ConfigurationSection yaml) {
        ConfigurationSection root = yaml.getConfigurationSection("enchants");
        if (root == null) throw new IllegalArgumentException("enchants.yml requires an enchants section");
        Map<NamespacedKey, EnchantDefinition> result = new LinkedHashMap<>();
        for (String rawId : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(rawId);
            if (section == null) throw new IllegalArgumentException("Expected definition section: " + rawId);
            if (section.contains("enabled") && !section.isBoolean("enabled")) throw new IllegalArgumentException(rawId + ": enabled must be boolean");
            if (!section.getBoolean("enabled", true)) continue;
            NamespacedKey id = rawId.contains(":") ? NamespacedKey.fromString(rawId) : null;
            if (id == null) throw new IllegalArgumentException("Expected namespaced enchant ID: " + rawId);
            try {
                if (!section.isInt("max-level")) throw new IllegalArgumentException("max-level must be an integer");
                if (!section.isList("description") || !section.getList("description").stream().allMatch(String.class::isInstance)) {
                    throw new IllegalArgumentException("description must be a list of strings");
                }
                if (!section.isList("materials") || !section.getList("materials").stream().allMatch(String.class::isInstance)) {
                    throw new IllegalArgumentException("materials must be a list of material names");
                }
                var materials = new LinkedHashSet<Material>();
                for (String name : section.getStringList("materials")) {
                    Material material = Material.getMaterial(name);
                    if (material == null) throw new IllegalArgumentException("Unknown material: " + name);
                    materials.add(material);
                }
                EnchantDefinition definition = new EnchantDefinition(id, section.getString("name"),
                    EnchantTier.valueOf(section.getString("tier", "").toUpperCase(java.util.Locale.ROOT)),
                    section.getInt("max-level"), section.getStringList("description"), materials);
                if (result.put(id, definition) != null) throw new IllegalArgumentException("Duplicate ID");
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new IllegalArgumentException(rawId + ": " + e.getMessage(), e);
            }
        }
        return Map.copyOf(result);
    }
}
