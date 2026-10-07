package me.alex.mythicenchants.config;

import java.net.URI;
import java.util.EnumMap;
import java.util.Map;
import me.alex.mythicenchants.enchant.EnchantTier;
import org.bukkit.configuration.ConfigurationSection;

public record Settings(Map<EnchantTier, String> colours, boolean updatesEnabled, int updateIntervalHours,
                       URI versionUrl, URI repositoryUrl) {
    public Settings { colours = Map.copyOf(colours); }
    public String colour(EnchantTier tier) { return colours.getOrDefault(tier, tier.defaultColour()); }
    public static Settings load(ConfigurationSection yaml) {
        Map<EnchantTier, String> colours = new EnumMap<>(EnchantTier.class);
        for (EnchantTier tier : EnchantTier.values()) {
            String colour = yaml.getString("tiers." + tier.name(), tier.defaultColour());
            if (!colour.matches("(?i)&[0-9a-f]")) throw new IllegalArgumentException("tiers." + tier.name() + " requires a colour such as &9");
            colours.put(tier, colour);
        }
        if (!yaml.isBoolean("updates.enabled")) throw new IllegalArgumentException("updates.enabled must be boolean");
        if (!yaml.isInt("updates.interval-hours")) throw new IllegalArgumentException("updates.interval-hours must be an integer");
        int interval = yaml.getInt("updates.interval-hours");
        if (interval < 1 || interval > 168) throw new IllegalArgumentException("updates.interval-hours must be 1..168");
        return new Settings(colours, yaml.getBoolean("updates.enabled"), interval,
            https(yaml.getString("updates.version-url")), https(yaml.getString("updates.repository-url")));
    }
    private static URI https(String value) {
        if (value == null) throw new IllegalArgumentException("Update URLs are required");
        URI uri = URI.create(value);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("Update URLs must be absolute HTTPS URLs");
        }
        return uri;
    }
}
