package org.ThefryGuy.techFactory.data;

import org.bukkit.Location;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a queue of recipes for a specific smelter
 * 
 * PHASE 3: Recipe Queueing System
 * - Allows players to queue multiple recipes instead of clicking repeatedly
 * - Auto-starts next recipe when current operation completes
 * - Persists to database to survive server restarts
 */
public class SmeltingQueue {
    
    private final Location smelterLocation;
    private final List<String> queuedRecipeIds; // List of recipe output IDs (e.g., "bronze_ingot")
    
    /**
     * Create a new empty queue for a smelter
     */
    public SmeltingQueue(Location smelterLocation) {
        this.smelterLocation = smelterLocation;
        this.queuedRecipeIds = new ArrayList<>();
    }
    
    /**
     * Create a queue with existing recipe IDs (for loading from database)
     */
    public SmeltingQueue(Location smelterLocation, List<String> queuedRecipeIds) {
        this.smelterLocation = smelterLocation;
        this.queuedRecipeIds = new ArrayList<>(queuedRecipeIds);
    }
    
    /**
     * Add a recipe to the end of the queue
     * 
     * @param recipeId The recipe output ID (e.g., "bronze_ingot")
     */
    public void addRecipe(String recipeId) {
        queuedRecipeIds.add(recipeId);
    }
    
    /**
     * Remove a recipe at a specific position
     * 
     * @param index The position in the queue (0-based)
     * @return true if removed, false if index out of bounds
     */
    public boolean removeRecipe(int index) {
        if (index < 0 || index >= queuedRecipeIds.size()) {
            return false;
        }
        queuedRecipeIds.remove(index);
        return true;
    }
    
    /**
     * Get the next recipe in the queue (without removing it)
     * 
     * @return The next recipe ID, or null if queue is empty
     */
    public String peekNext() {
        if (queuedRecipeIds.isEmpty()) {
            return null;
        }
        return queuedRecipeIds.get(0);
    }
    
    /**
     * Get and remove the next recipe from the queue
     * 
     * @return The next recipe ID, or null if queue is empty
     */
    public String pollNext() {
        if (queuedRecipeIds.isEmpty()) {
            return null;
        }
        return queuedRecipeIds.remove(0);
    }
    
    /**
     * Clear all recipes from the queue
     */
    public void clear() {
        queuedRecipeIds.clear();
    }
    
    /**
     * Check if the queue is empty
     */
    public boolean isEmpty() {
        return queuedRecipeIds.isEmpty();
    }
    
    /**
     * Get the number of recipes in the queue
     */
    public int size() {
        return queuedRecipeIds.size();
    }
    
    /**
     * Get all queued recipe IDs (for display/persistence)
     */
    public List<String> getQueuedRecipeIds() {
        return new ArrayList<>(queuedRecipeIds);
    }
    
    /**
     * Get the smelter location
     */
    public Location getSmelterLocation() {
        return smelterLocation;
    }
    
    /**
     * Get a location key for this queue (for map storage)
     */
    public String getLocationKey() {
        return locationToKey(smelterLocation);
    }
    
    /**
     * Convert a location to a unique string key
     */
    public static String locationToKey(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            throw new IllegalArgumentException("Invalid location: location or world is null");
        }
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }
}

