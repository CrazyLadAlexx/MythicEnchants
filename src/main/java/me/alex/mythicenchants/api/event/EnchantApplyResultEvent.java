package me.alex.mythicenchants.api.event;

import me.alex.mythicenchants.api.ApplicationResult;
import me.alex.mythicenchants.api.BookData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** Outcome notification; inventory callers commit the result after this event. */
public final class EnchantApplyResultEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final BookData book;
    private final ApplicationResult result;
    public EnchantApplyResultEvent(Player player, BookData book, ApplicationResult result) {
        this.player = player; this.book = book;
        this.result = copy(result);
    }
    private static ApplicationResult copy(ApplicationResult result) {
        return new ApplicationResult(result.status(), result.message(),
            result.remainingBooks() == null ? null : result.remainingBooks().clone(),
            result.resultingGear() == null ? null : result.resultingGear().clone());
    }
    public Player getPlayer() { return player; }
    public BookData getBook() { return book; }
    public ApplicationResult getResult() { return copy(result); }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
