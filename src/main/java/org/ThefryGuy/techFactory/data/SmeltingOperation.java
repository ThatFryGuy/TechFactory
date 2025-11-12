package org.ThefryGuy.techFactory.data;

import org.bukkit.Location;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

/**
 * Represents an active smelting operation in an Alloy Smelter
 *
 * PRIORITY 2: Now supports persistence - can be saved/loaded from database
 */
public class SmeltingOperation {
    private final Location blastFurnaceLocation;
    private final RecipeItem output;
    private final long startTime;
    private final long duration; // in milliseconds

    /**
     * Create a new smelting operation (starts now)
     */
    public SmeltingOperation(Location blastFurnaceLocation, RecipeItem output, long duration) {
        this.blastFurnaceLocation = blastFurnaceLocation;
        this.output = output;
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
    }

    /**
     * Create a smelting operation with a specific start time (for loading from database)
     * PRIORITY 2: Used when restoring operations after server restart
     */
    public SmeltingOperation(Location blastFurnaceLocation, RecipeItem output, long startTime, long duration) {
        this.blastFurnaceLocation = blastFurnaceLocation;
        this.output = output;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Location getBlastFurnaceLocation() {
        return blastFurnaceLocation;
    }

    public RecipeItem getOutput() {
        return output;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDuration() {
        return duration;
    }

    public boolean isComplete() {
        return System.currentTimeMillis() >= startTime + duration;
    }

    public long getRemainingTime() {
        long remaining = (startTime + duration) - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public int getProgressPercentage() {
        long elapsed = System.currentTimeMillis() - startTime;
        return (int) Math.min(100, (elapsed * 100) / duration);
    }
}

