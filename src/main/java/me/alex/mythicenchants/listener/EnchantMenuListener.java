package me.alex.mythicenchants.listener;

import me.alex.mythicenchants.gui.EnchantMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class EnchantMenuListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        var top = event.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof EnchantMenu)) return;
        if (event.getClickedInventory() == top || event.isShiftClick()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        var top = event.getView().getTopInventory();
        if (!(top.getHolder(false) instanceof EnchantMenu)) return;
        if (event.getRawSlots().stream().anyMatch(slot -> slot >= 0 && slot < top.getSize())) event.setCancelled(true);
    }
}
