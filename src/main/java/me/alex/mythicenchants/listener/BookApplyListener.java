package me.alex.mythicenchants.listener;

import me.alex.mythicenchants.api.MythicEnchantsAPI;
import me.alex.mythicenchants.book.BookService;
import me.alex.mythicenchants.util.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class BookApplyListener implements Listener {
    private final JavaPlugin plugin;
    private final MythicEnchantsAPI api;
    private final BookService books;
    public BookApplyListener(JavaPlugin plugin, MythicEnchantsAPI api, BookService books) {
        this.plugin = plugin; this.api = api; this.books = books;
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || event.getClick() != ClickType.LEFT
            || event.getClickedInventory() != player.getInventory() || !books.isMarked(event.getCursor())) return;
        ItemStack beforeBooks = event.getCursor().clone();
        ItemStack beforeGear = event.getCurrentItem() == null ? null : event.getCurrentItem().clone();
        event.setCancelled(true);
        int slot = event.getSlot();
        var top = event.getView().getTopInventory();
        // Bukkit recommends scheduling inventory changes after the click transaction.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline() || !player.getOpenInventory().getTopInventory().equals(top)
                || !same(player.getItemOnCursor(), beforeBooks) || !same(player.getInventory().getItem(slot), beforeGear)) return;
            var result = api.attemptApplication(player, beforeBooks, beforeGear);
            // Event subscribers can change inventories; never overwrite their changes.
            if (!player.isOnline() || !player.getOpenInventory().getTopInventory().equals(top)
                || !same(player.getItemOnCursor(), beforeBooks) || !same(player.getInventory().getItem(slot), beforeGear)) return;
            if (result.consumedBook()) {
                player.getInventory().setItem(slot, result.resultingGear());
                player.setItemOnCursor(result.remainingBooks());
            }
            player.sendMessage(Text.colour((result.status() == me.alex.mythicenchants.api.ApplicationResult.Status.APPLIED ? "&a" : "&c") + result.message()));
        });
    }
    private static boolean same(ItemStack first, ItemStack second) {
        boolean firstEmpty = first == null || first.getType().isAir() || first.getAmount() == 0;
        boolean secondEmpty = second == null || second.getType().isAir() || second.getAmount() == 0;
        return firstEmpty || secondEmpty ? firstEmpty == secondEmpty : first.equals(second);
    }
}
