package me.alex.mythicenchants.api.event;

import me.alex.mythicenchants.api.BookData;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public final class EnchantPreApplyEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final BookData book;
    private final ItemStack gear;
    private boolean cancelled;
    public EnchantPreApplyEvent(Player player, BookData book, ItemStack gear) {
        this.player = player; this.book = book; this.gear = gear.clone();
    }
    public Player getPlayer() { return player; }
    public BookData getBook() { return book; }
    public ItemStack getGear() { return gear.clone(); }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
