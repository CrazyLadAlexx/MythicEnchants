package me.alex.mythicenchants.config;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import me.alex.mythicenchants.api.EnchantDefinition;
import me.alex.mythicenchants.enchant.DefinitionLoader;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigurationManager {
    private final JavaPlugin plugin;
    public record Loaded(Settings settings, Map<NamespacedKey, EnchantDefinition> definitions) {}
    public ConfigurationManager(JavaPlugin plugin) { this.plugin = plugin; }
    public Loaded load() throws IOException, InvalidConfigurationException {
        return new Loaded(Settings.load(read("config.yml")), DefinitionLoader.load(read("enchants.yml")));
    }
    private YamlConfiguration read(String name) throws IOException, InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        // Namespaced IDs may contain dots and slashes; treat them as literal YAML keys.
        if (name.equals("enchants.yml")) yaml.options().pathSeparator('\0');
        yaml.load(new File(plugin.getDataFolder(), name));
        return yaml;
    }
}
