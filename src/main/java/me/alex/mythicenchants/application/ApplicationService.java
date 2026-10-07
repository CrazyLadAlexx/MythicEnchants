package me.alex.mythicenchants.application;

import java.util.function.Consumer;
import me.alex.mythicenchants.api.ApplicationResult;
import me.alex.mythicenchants.api.ApplicationResult.Status;
import me.alex.mythicenchants.api.event.EnchantApplyResultEvent;
import me.alex.mythicenchants.api.event.EnchantPreApplyEvent;
import me.alex.mythicenchants.book.BookService;
import me.alex.mythicenchants.enchant.EnchantRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

public final class ApplicationService {
    private final EnchantRegistry registry;
    private final BookService books;
    private final GearService gear;
    private final ChanceRoller rolls;
    private final Consumer<Event> events;
    public ApplicationService(EnchantRegistry registry, BookService books, GearService gear,
                              ChanceRoller rolls, Consumer<Event> events) {
        this.registry = registry; this.books = books; this.gear = gear; this.rolls = rolls; this.events = events;
    }
    public ApplicationResult attempt(Player player, ItemStack bookStack, ItemStack target) {
        if (player == null) throw new IllegalArgumentException("Player is required");
        var inspected = books.inspect(bookStack);
        if (inspected.isEmpty() || bookStack.getAmount() < 1) return invalid("Invalid enchant book.", bookStack, target);
        var data = inspected.get();
        var definition = registry.find(data.enchantId());
        if (definition.isEmpty()) return invalid("Unknown enchant: " + data.enchantId(), bookStack, target);
        if (data.level() > definition.get().maxLevel()) return invalid("Book exceeds this enchant's maximum level.", bookStack, target);
        if (target == null || target.getAmount() != 1 || !definition.get().materials().contains(target.getType())) {
            return invalid("This enchant cannot be applied to that gear.", bookStack, target);
        }
        final ItemStack applied;
        try {
            if (gear.read(target).getOrDefault(data.enchantId(), 0) >= data.level()) {
                return invalid("Gear already has this enchant at an equal or higher level.", bookStack, target);
            }
            // Validate all metadata and prepare the success snapshot before any destructive roll.
            applied = gear.apply(target, data.enchantId(), data.level());
        } catch (IllegalArgumentException e) { return invalid(e.getMessage(), bookStack, target); }
        var pre = new EnchantPreApplyEvent(player, data, target);
        events.accept(pre);
        if (pre.isCancelled()) return result(Status.CANCELLED, "Enchant application was cancelled.", bookStack, target);
        Status outcome = rolls.outcome(data.successChance(), data.destroyChance());
        ItemStack remaining = bookStack.getAmount() == 1 ? null : bookStack.clone();
        if (remaining != null) remaining.setAmount(bookStack.getAmount() - 1);
        ApplicationResult result = switch (outcome) {
            case APPLIED -> result(outcome, "Enchantment applied!", remaining, applied);
            case FAILED -> result(outcome, "The enchant failed. Your gear survived.", remaining, target);
            case DESTROYED -> result(outcome, "The enchant failed and your gear was destroyed!", remaining, null);
            default -> throw new IllegalStateException("Unexpected outcome");
        };
        events.accept(new EnchantApplyResultEvent(player, data, result));
        return result;
    }
    private static ApplicationResult invalid(String message, ItemStack books, ItemStack gear) {
        return result(Status.INVALID, message, books, gear);
    }
    private static ApplicationResult result(Status status, String message, ItemStack books, ItemStack gear) {
        return new ApplicationResult(status, message, books == null ? null : books.clone(), gear == null ? null : gear.clone());
    }
}
