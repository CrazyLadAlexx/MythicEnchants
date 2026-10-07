package me.alex.mythicenchants.application;

import java.util.function.IntSupplier;
import me.alex.mythicenchants.api.ApplicationResult.Status;

public final class ChanceRoller {
    private final IntSupplier roll;
    public ChanceRoller(IntSupplier roll) { this.roll = roll; }
    public Status outcome(int success, int destroy) {
        if (success < 1 || success > 100 || destroy < 1 || destroy > 100) throw new IllegalArgumentException("Chances must be 1..100");
        if (next() <= success) return Status.APPLIED;
        return next() <= destroy ? Status.DESTROYED : Status.FAILED;
    }
    private int next() {
        int number = roll.getAsInt();
        if (number < 1 || number > 100) throw new IllegalStateException("Roll must be 1..100");
        return number;
    }
}
