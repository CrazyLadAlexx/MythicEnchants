package me.alex.mythicenchants.book;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Supplier;
import me.alex.mythicenchants.api.BookData;
import me.alex.mythicenchants.api.EnchantDefinition;
import me.alex.mythicenchants.config.Settings;
import me.alex.mythicenchants.util.RomanNumerals;
import me.alex.mythicenchants.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public final class BookService {
    private final NamespacedKey id = new NamespacedKey("mythicenchants", "book_enchant");
    private final NamespacedKey level = new NamespacedKey("mythicenchants", "book_level");
    private final NamespacedKey success = new NamespacedKey("mythicenchants", "book_success");
    private final NamespacedKey destroy = new NamespacedKey("mythicenchants", "book_destroy");
    private final Supplier<Settings> settings;
    public BookService(Supplier<Settings> settings) { this.settings = settings; }
    public ItemStack create(EnchantDefinition definition, int value, int successChance, int destroyChance) {
        BookData data = new BookData(definition.id(), value, successChance, destroyChance);
        if (value > definition.maxLevel()) throw new IllegalArgumentException("Level exceeds maximum of " + definition.maxLevel());
        ItemStack item = new ItemStack(Material.BOOK);
        var meta = item.getItemMeta();
        meta.displayName(Text.colour(BookText.NAME.format(settings.get().colour(definition.tier()), definition.displayName(), RomanNumerals.format(value))));
        var lore = new ArrayList<Component>();
        lore.add(Text.colour(BookText.SUCCESS.format(successChance)));
        lore.add(Text.colour(BookText.DESTROY.format(destroyChance)));
        definition.description().forEach(line -> lore.add(Text.colour(BookText.DESCRIPTION.format(line))));
        lore.add(Text.colour(BookText.SEPARATOR.format()));
        lore.add(Text.colour(BookText.HINT_FIRST.format()));
        lore.add(Text.colour(BookText.HINT_SECOND.format()));
        meta.lore(lore);
        var pdc = meta.getPersistentDataContainer();
        pdc.set(id, PersistentDataType.STRING, data.enchantId().toString());
        pdc.set(level, PersistentDataType.INTEGER, data.level());
        pdc.set(success, PersistentDataType.INTEGER, data.successChance());
        pdc.set(destroy, PersistentDataType.INTEGER, data.destroyChance());
        item.setItemMeta(meta);
        return item;
    }
    public boolean isMarked(ItemStack item) {
        if (item == null || item.getType() != Material.BOOK || !item.hasItemMeta()) return false;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(id) || pdc.has(level) || pdc.has(success) || pdc.has(destroy);
    }
    public Optional<BookData> inspect(ItemStack item) {
        if (!isMarked(item)) return Optional.empty();
        var pdc = item.getItemMeta().getPersistentDataContainer();
        try {
            String rawId = pdc.get(id, PersistentDataType.STRING);
            NamespacedKey key = rawId == null || !rawId.contains(":") ? null : NamespacedKey.fromString(rawId);
            Integer value = pdc.get(level, PersistentDataType.INTEGER);
            Integer chance = pdc.get(success, PersistentDataType.INTEGER);
            Integer risk = pdc.get(destroy, PersistentDataType.INTEGER);
            if (key == null || value == null || chance == null || risk == null) return Optional.empty();
            return Optional.of(new BookData(key, value, chance, risk));
        } catch (IllegalArgumentException e) { return Optional.empty(); }
    }
}
