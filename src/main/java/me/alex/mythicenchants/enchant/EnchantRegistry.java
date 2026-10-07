package me.alex.mythicenchants.enchant;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import me.alex.mythicenchants.api.EnchantDefinition;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class EnchantRegistry {
    private Map<NamespacedKey, EnchantDefinition> configured = Map.of();
    private final Map<NamespacedKey, Registration> registered = new LinkedHashMap<>();
    private record Registration(Plugin owner, EnchantDefinition definition) {}

    public void validateReplacement(Map<NamespacedKey, EnchantDefinition> definitions) {
        for (NamespacedKey id : definitions.keySet()) {
            if (registered.containsKey(id)) throw new IllegalArgumentException("Config enchant conflicts with API registration: " + id);
        }
    }
    public void replaceConfigured(Map<NamespacedKey, EnchantDefinition> definitions) {
        validateReplacement(definitions);
        configured = Map.copyOf(definitions);
    }
    public void register(Plugin owner, EnchantDefinition definition) {
        if (!owner.isEnabled()) throw new IllegalArgumentException("Owner plugin must be enabled");
        if (find(definition.id()).isPresent()) throw new IllegalArgumentException("Duplicate enchant ID: " + definition.id());
        registered.put(definition.id(), new Registration(owner, definition));
    }
    public boolean unregister(Plugin owner, NamespacedKey id) {
        Registration registration = registered.get(id);
        if (registration == null || registration.owner() != owner) return false;
        registered.remove(id);
        return true;
    }
    public void unregisterAll(Plugin owner) { registered.values().removeIf(r -> r.owner() == owner); }
    public Optional<EnchantDefinition> find(NamespacedKey id) {
        Registration registration = registered.get(id);
        return Optional.ofNullable(registration == null ? configured.get(id) : registration.definition());
    }
    public Collection<EnchantDefinition> all() {
        Map<NamespacedKey, EnchantDefinition> all = new LinkedHashMap<>(configured);
        registered.forEach((id, r) -> all.put(id, r.definition()));
        return all.values().stream().sorted(java.util.Comparator.comparing(d -> d.id().toString())).toList();
    }
}
