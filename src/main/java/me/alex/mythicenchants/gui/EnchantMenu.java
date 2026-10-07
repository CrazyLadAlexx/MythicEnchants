package me.alex.mythicenchants.gui;

import me.alex.mythicenchants.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class EnchantMenu implements InventoryHolder {
    private final Inventory inventory;

    private EnchantMenu() {
        inventory = Bukkit.createInventory(this, 36, Text.colour("&8MythicEnchantments"));
    }

    public static void open(Player player) {
        if (!player.isOp()) return;
        EnchantMenu menu = new EnchantMenu();
        player.openInventory(menu.inventory);
        if (player.getOpenInventory().getTopInventory().equals(menu.inventory)) {
            player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 0.6F, 1.0F);
        }
    }

    @Override public Inventory getInventory() { return inventory; }
}
