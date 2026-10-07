package me.alex.mythicenchants.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import me.alex.mythicenchants.config.Settings;
import me.alex.mythicenchants.enchant.EnchantRegistry;
import me.alex.mythicenchants.util.RomanNumerals;
import me.alex.mythicenchants.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public final class GearService {
    private final NamespacedKey enchants = new NamespacedKey("mythicenchants", "gear_enchants");
    private final NamespacedKey managedLore = new NamespacedKey("mythicenchants", "managed_lore");
    private final EnchantRegistry registry;
    private final Supplier<Settings> settings;
    public GearService(EnchantRegistry registry, Supplier<Settings> settings) { this.registry = registry; this.settings = settings; }
    public Map<NamespacedKey, Integer> read(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return Map.of();
        var pdc = item.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(enchants)) return Map.of();
        var container = pdc.get(enchants, PersistentDataType.TAG_CONTAINER);
        if (container == null) throw new IllegalArgumentException("Malformed applied enchant data");
        Map<NamespacedKey, Integer> result = new LinkedHashMap<>();
        for (NamespacedKey key : container.getKeys()) {
            Integer level = container.get(key, PersistentDataType.INTEGER);
            if (level == null || level < 1 || level > 3999) throw new IllegalArgumentException("Malformed level for " + key);
            result.put(key, level);
        }
        return Map.copyOf(result);
    }
    public ItemStack apply(ItemStack original, NamespacedKey id, int level) {
        Map<NamespacedKey, Integer> levels = new LinkedHashMap<>(read(original));
        levels.put(id, level);
        ItemStack result = original.clone();
        var meta = result.getItemMeta();
        var pdc = meta.getPersistentDataContainer();
        var container = pdc.getAdapterContext().newPersistentDataContainer();
        levels.forEach((key, value) -> container.set(key, PersistentDataType.INTEGER, value));
        pdc.set(enchants, PersistentDataType.TAG_CONTAINER, container);
        List<Component> lore = new ArrayList<>(meta.lore() == null ? List.of() : meta.lore());
        String old = pdc.get(managedLore, PersistentDataType.STRING);
        if (old != null && !old.isEmpty()) {
            List<Component> previous;
            try { previous = old.lines().map(line -> GsonComponentSerializer.gson().deserialize(line)).toList(); }
            catch (RuntimeException e) { throw new IllegalArgumentException("Malformed managed enchant lore", e); }
            for (int offset = lore.size() - previous.size(); offset >= 0; offset--) {
                if (lore.subList(offset, offset + previous.size()).equals(previous)) {
                    lore.subList(offset, offset + previous.size()).clear(); break;
                }
            }
        }
        List<Component> generated = new ArrayList<>();
        levels.entrySet().stream().sorted(Map.Entry.comparingByKey(java.util.Comparator.comparing(NamespacedKey::toString))).forEach(entry -> {
            var definition = registry.find(entry.getKey());
            String colour = definition.map(d -> settings.get().colour(d.tier())).orElse("&f");
            String name = definition.map(d -> d.displayName()).orElse(entry.getKey().toString());
            generated.add(Text.colour(colour + name + " " + RomanNumerals.format(entry.getValue())));
        });
        lore.addAll(generated);
        meta.lore(lore);
        pdc.set(managedLore, PersistentDataType.STRING, String.join("\n", generated.stream().map(GsonComponentSerializer.gson()::serialize).toList()));
        result.setItemMeta(meta);
        return result;
    }
}
