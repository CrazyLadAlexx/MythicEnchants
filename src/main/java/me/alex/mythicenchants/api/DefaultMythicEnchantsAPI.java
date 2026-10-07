package me.alex.mythicenchants.api;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import me.alex.mythicenchants.application.ApplicationService;
import me.alex.mythicenchants.application.GearService;
import me.alex.mythicenchants.book.BookService;
import me.alex.mythicenchants.enchant.EnchantRegistry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class DefaultMythicEnchantsAPI implements MythicEnchantsAPI {
    private final EnchantRegistry registry;
    private final BookService books;
    private final GearService gear;
    private final ApplicationService application;
    public DefaultMythicEnchantsAPI(EnchantRegistry registry, BookService books, GearService gear, ApplicationService application) {
        this.registry = registry; this.books = books; this.gear = gear; this.application = application;
    }
    private static void serverThread() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("MythicEnchants API must be called on the server thread");
    }
    @Override public void register(Plugin owner, EnchantDefinition definition) { serverThread(); registry.register(owner, definition); }
    @Override public boolean unregister(Plugin owner, NamespacedKey id) { serverThread(); return registry.unregister(owner, id); }
    @Override public Collection<EnchantDefinition> definitions() { serverThread(); return registry.all(); }
    @Override public Optional<EnchantDefinition> definition(NamespacedKey id) { serverThread(); return registry.find(id); }
    @Override public ItemStack createBook(NamespacedKey id, int level, int successChance, int destroyChance) {
        serverThread();
        return books.create(registry.find(id).orElseThrow(() -> new IllegalArgumentException("Unknown enchant: " + id)), level, successChance, destroyChance);
    }
    @Override public Optional<BookData> inspectBook(ItemStack item) { serverThread(); return books.inspect(item); }
    @Override public Map<NamespacedKey, Integer> appliedEnchants(ItemStack item) { serverThread(); return gear.read(item); }
    @Override public ApplicationResult attemptApplication(Player player, ItemStack books, ItemStack gear) {
        serverThread(); return application.attempt(player, books, gear);
    }
}
