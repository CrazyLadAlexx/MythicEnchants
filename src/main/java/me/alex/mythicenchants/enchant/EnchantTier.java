package me.alex.mythicenchants.enchant;

public enum EnchantTier {
    SIMPLE("Simple", "&f"), UNCOMMON("Uncommon", "&a"), ELITE("Elite", "&9"),
    ULTIMATE("Ultimate", "&e"), LEGENDARY("Legendary", "&6"), GODLY("Godly", "&c"), MYTHIC("Mythic", "&d");

    private final String displayName;
    private final String defaultColour;
    EnchantTier(String displayName, String defaultColour) {
        this.displayName = displayName;
        this.defaultColour = defaultColour;
    }
    public String displayName() { return displayName; }
    public String defaultColour() { return defaultColour; }
}
