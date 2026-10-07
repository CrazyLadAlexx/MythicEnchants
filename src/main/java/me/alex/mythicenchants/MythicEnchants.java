package me.alex.mythicenchants;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import me.alex.mythicenchants.api.DefaultMythicEnchantsAPI;
import me.alex.mythicenchants.api.MythicEnchantsAPI;
import me.alex.mythicenchants.application.ApplicationService;
import me.alex.mythicenchants.application.ChanceRoller;
import me.alex.mythicenchants.application.GearService;
import me.alex.mythicenchants.book.BookService;
import me.alex.mythicenchants.command.MythicEnchantsCommand;
import me.alex.mythicenchants.config.ConfigurationManager;
import me.alex.mythicenchants.config.Settings;
import me.alex.mythicenchants.enchant.EnchantRegistry;
import me.alex.mythicenchants.listener.BookApplyListener;
import me.alex.mythicenchants.listener.CommandAliasListener;
import me.alex.mythicenchants.listener.EnchantMenuListener;
import me.alex.mythicenchants.update.UpdateChecker;
import me.alex.mythicenchants.util.Text;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class MythicEnchants extends JavaPlugin implements Listener {
    private final EnchantRegistry registry = new EnchantRegistry();
    private ConfigurationManager configuration;
    private Settings settings;
    private UpdateChecker updates;
    @Override public void onEnable() {
        try {
            saveDefaultConfig();
            if (!new java.io.File(getDataFolder(), "enchants.yml").exists()) saveResource("enchants.yml", false);
            configuration = new ConfigurationManager(this);
            reloadSettings();
            var books = new BookService(() -> settings);
            var gear = new GearService(registry, () -> settings);
            var application = new ApplicationService(registry, books, gear,
                new ChanceRoller(() -> ThreadLocalRandom.current().nextInt(1, 101)), getServer().getPluginManager()::callEvent);
            MythicEnchantsAPI api = new DefaultMythicEnchantsAPI(registry, books, gear, application);
            getServer().getServicesManager().register(MythicEnchantsAPI.class, api, this, ServicePriority.Normal);
            var command = new MythicEnchantsCommand(this, api, this::reloadSettings);
            var registered = Objects.requireNonNull(getCommand("mythicenchants"));
            registered.setExecutor(command);
            registered.setTabCompleter(command);
            getServer().getPluginManager().registerEvents(new CommandAliasListener(registered), this);
            getServer().getPluginManager().registerEvents(new EnchantMenuListener(), this);
            getServer().getPluginManager().registerEvents(new BookApplyListener(this, api, books), this);
            getServer().getPluginManager().registerEvents(this, this);
            updates = new UpdateChecker(this);
            getServer().getPluginManager().registerEvents(updates, this);
            updates.start(settings);
            getServer().getConsoleSender().sendMessage(Text.colour("&aMythicEnchants loaded successfully!"));
        } catch (Exception e) {
            String issue = e.getMessage() == null || e.getMessage().isBlank() ? e.getClass().getSimpleName() : e.getMessage();
            getServer().getConsoleSender().sendMessage(Text.colour("&cMythicEnchants produced " + issue + "!"));
            getLogger().log(java.util.logging.Level.SEVERE, "Could not enable MythicEnchants", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    private void reloadSettings() throws Exception {
        var loaded = configuration.load();
        registry.validateReplacement(loaded.definitions());
        registry.replaceConfigured(loaded.definitions());
        settings = loaded.settings();
        if (updates != null) updates.start(settings);
    }
    @EventHandler public void onPluginDisable(PluginDisableEvent event) { registry.unregisterAll(event.getPlugin()); }
    @Override public void onDisable() {
        if (updates != null) updates.stop();
        getServer().getServicesManager().unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);
    }
}
