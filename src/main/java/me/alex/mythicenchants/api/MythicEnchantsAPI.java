package me.alex.mythicenchants.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public interface MythicEnchantsAPI {
    void register(Plugin owner, EnchantDefinition definition);
    boolean unregister(Plugin owner, NamespacedKey id);
    Collection<EnchantDefinition> definitions();
    Optional<EnchantDefinition> definition(NamespacedKey id);
    ItemStack createBook(NamespacedKey id, int level, int successChance, int destroyChance);
    Optional<BookData> inspectBook(ItemStack item);
    Map<NamespacedKey, Integer> appliedEnchants(ItemStack item);
    ApplicationResult attemptApplication(Player player, ItemStack books, ItemStack gear);
}
