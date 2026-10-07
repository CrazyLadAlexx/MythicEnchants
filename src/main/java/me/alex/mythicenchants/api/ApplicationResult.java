package me.alex.mythicenchants.api;

import org.bukkit.inventory.ItemStack;

/** Output snapshots; the caller commits them to inventory. Inputs are never mutated. */
public record ApplicationResult(Status status, String message, ItemStack remainingBooks, ItemStack resultingGear) {
    public enum Status { INVALID, CANCELLED, APPLIED, FAILED, DESTROYED }
    public boolean consumedBook() { return status == Status.APPLIED || status == Status.FAILED || status == Status.DESTROYED; }
}
