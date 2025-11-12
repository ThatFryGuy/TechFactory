package org.ThefryGuy.techFactory.data;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.TechFactoryConstants;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.registry.SystemManager;
import org.ThefryGuy.techFactory.workstations.multiblocks.SmelterMachine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages all active smelting operations across all Alloy Smelters
 *
 * PERFORMANCE OPTIMIZED: Reduced tick frequency and thread-safe collections
 * LIFECYCLE: Implements SystemManager for automatic initialization/shutdown via ManagerRegistry
 */
public class SmeltingManager implements SystemManager {

    private final TechFactory plugin;
    private final Map<String, SmeltingOperation> activeOperations = new ConcurrentHashMap<>();
    private int taskId = -1;

    // BUG FIX 4: Message spam throttling
    // Tracks last message time per player to prevent spam when 50+ smelters complete at once
    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();

    // BUG FIX 4: Batch messages per tick
    // Collects all completions in current tick, sends summary instead of individual messages
    private final Map<UUID, List<String>> pendingMessages = new ConcurrentHashMap<>();

    // PHASE 3: Recipe queueing
    // In-memory cache of queues per smelter location
    private final Map<String, SmeltingQueue> queueCache = new ConcurrentHashMap<>();

    public SmeltingManager(TechFactory plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize the smelting manager (SystemManager interface)
     * Called automatically by ManagerRegistry during plugin startup
     *
     * LIFECYCLE:
     * 1. Load active smelting operations from database (PRIORITY 2: IMPLEMENTED)
     * 2. Start background tick task
     */
    @Override
    public void initialize() {
        // PRIORITY 2: Load active operations from database
        DatabaseManager dbManager = plugin.getDatabaseManager();
        List<SmeltingOperation> savedOperations = dbManager.loadAllSmeltingOperations();

        for (SmeltingOperation op : savedOperations) {
            String key = locationToKey(op.getBlastFurnaceLocation());
            activeOperations.put(key, op);
        }

        if (!savedOperations.isEmpty()) {
            plugin.getLogger().info("Restored " + savedOperations.size() + " active smelting operations");
        }

        startTask();
    }

    /**
     * Disable the smelting manager (SystemManager interface)
     * Called automatically by ManagerRegistry during plugin shutdown
     *
     * LIFECYCLE:
     * 1. Save active smelting operations to database (PRIORITY 2: IMPLEMENTED)
     * 2. Stop background tick task
     * 3. Clear all operations
     * 4. Clear message throttling maps (BUG FIX 4)
     */
    @Override
    public void disable() {
        // PRIORITY 2: Save active operations to database
        DatabaseManager dbManager = plugin.getDatabaseManager();

        // First, clear all old operations from database
        dbManager.deleteAllSmeltingOperations();

        // Then save current active operations
        int savedCount = 0;
        for (SmeltingOperation op : activeOperations.values()) {
            dbManager.saveSmeltingOperation(op);
            savedCount++;
        }

        if (savedCount > 0) {
            plugin.getLogger().info("Saved " + savedCount + " active smelting operations to database");
        }

        stopTask();
        clearAll();

        // BUG FIX 4: Clear message throttling maps
        lastMessageTime.clear();
        pendingMessages.clear();
    }

    /**
     * Start the background task that checks for completed smelting operations
     *
     * OPTIMIZED: Runs every 5 ticks instead of every tick (80% less CPU usage)
     * BUG FIX 4: Also schedules periodic cleanup of message throttling map and message flushing
     */
    public void startTask() {
        if (taskId != -1) {
            return; // Already running
        }

        // Run every 5 ticks (4 times per second) - still very responsive but much more efficient
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            checkCompletedOperations();
            flushPendingMessages(); // BUG FIX 4: Flush batched messages every tick
        }, 0L, TechFactoryConstants.SMELTING_CHECK_INTERVAL_TICKS());

        plugin.getLogger().info("Smelting Manager started (checking every " + TechFactoryConstants.SMELTING_CHECK_INTERVAL_TICKS() + " ticks)");

        // BUG FIX 4: Schedule periodic cleanup of message throttling map (every 30 seconds)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            cleanupMessageThrottling();
        }, 600L, 600L); // 600 ticks = 30 seconds
    }

    /**
     * Stop the background task
     */
    public void stopTask() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    /**
     * Start a new smelting operation
     * PRIORITY 2: Now saves to database for persistence
     * BUG FIX 3: Added null checks to prevent NullPointerException
     */
    public void startSmelting(Location blastFurnaceLocation, RecipeItem output, long durationMs) {
        // BUG FIX 3: Null checks
        if (blastFurnaceLocation == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot start smelting: location is null");
            return;
        }
        if (output == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot start smelting: output is null");
            return;
        }

        String key = locationToKey(blastFurnaceLocation);
        SmeltingOperation operation = new SmeltingOperation(blastFurnaceLocation, output, durationMs);
        activeOperations.put(key, operation);

        // PRIORITY 2: Save to database (async to avoid lag)
        DatabaseManager dbManager = plugin.getDatabaseManager();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            dbManager.saveSmeltingOperation(operation);
        });
    }

    /**
     * Check if a location has an active smelting operation
     */
    public boolean isSmelting(Location blastFurnaceLocation) {
        String key = locationToKey(blastFurnaceLocation);
        return activeOperations.containsKey(key);
    }

    /**
     * Get the active operation at a location
     */
    public SmeltingOperation getOperation(Location blastFurnaceLocation) {
        String key = locationToKey(blastFurnaceLocation);
        return activeOperations.get(key);
    }

    /**
     * Cancel a smelting operation
     * PRIORITY 2: Now deletes from database
     * BUG FIX 3: Added null check to prevent NullPointerException
     */
    public void cancelSmelting(Location blastFurnaceLocation) {
        // BUG FIX 3: Null check
        if (blastFurnaceLocation == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot cancel smelting: location is null");
            return;
        }

        String key = locationToKey(blastFurnaceLocation);
        activeOperations.remove(key);

        // PRIORITY 2: Delete from database (async to avoid lag)
        DatabaseManager dbManager = plugin.getDatabaseManager();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            dbManager.deleteSmeltingOperation(blastFurnaceLocation);
        });
    }

    /**
     * Check for completed operations and output items
     *
     * CHUNK LOADING PROTECTION: Only processes smelters in loaded chunks
     * PRIORITY 2: Now deletes completed operations from database
     */
    private void checkCompletedOperations() {
        List<String> toRemove = new ArrayList<>();
        DatabaseManager dbManager = plugin.getDatabaseManager();

        for (Map.Entry<String, SmeltingOperation> entry : activeOperations.entrySet()) {
            SmeltingOperation operation = entry.getValue();
            Location loc = operation.getBlastFurnaceLocation();

            // BUG FIX 3: Null checks to prevent crashes
            if (loc == null || loc.getWorld() == null) {
                toRemove.add(entry.getKey()); // Remove invalid operation
                continue;
            }

            // CHUNK LOADING CHECK: Skip if chunk is not loaded
            // This prevents lag from trying to access unloaded chunks
            if (!loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                continue; // Skip this operation, will check again when chunk loads
            }

            if (operation.isComplete()) {
                // Complete the smelting
                completeSmelting(operation);
                toRemove.add(entry.getKey());

                // PRIORITY 2: Delete from database (async to avoid lag)
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    dbManager.deleteSmeltingOperation(loc);
                });
            }
        }

        // Remove completed operations from memory
        for (String key : toRemove) {
            activeOperations.remove(key);
        }
    }

    /**
     * Complete a smelting operation and output the item
     * BUG FIX 3: Added null checks to prevent NullPointerException
     */
    private void completeSmelting(SmeltingOperation operation) {
        // BUG FIX 3: Null checks
        if (operation == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot complete smelting: operation is null");
            return;
        }

        Location loc = operation.getBlastFurnaceLocation();
        if (loc == null || loc.getWorld() == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot complete smelting: location or world is null");
            return;
        }

        Block blastFurnace = loc.getBlock();
        if (blastFurnace == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot complete smelting: block is null at " + loc);
            return;
        }

        // Verify the multiblock is still valid
        if (!SmelterMachine.isValidStructure(blastFurnace)) {
            return; // Multiblock was broken
        }

        // Create output item
        RecipeItem output = operation.getOutput();
        if (output == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot complete smelting: output is null");
            return;
        }

        ItemStack outputItem = output.getItemStack();
        if (outputItem == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot complete smelting: output ItemStack is null");
            return;
        }

        // Try to output to chest first, then to GUI output slot
        // Build multiblock blocks list for chest detection
        Block ironBars = blastFurnace.getRelative(0, 1, 0);
        Block campfire = blastFurnace.getRelative(0, -1, 0);

        // Determine which bricks are actually part of the structure
        Block side1 = blastFurnace.getRelative(1, 0, 0);
        Block side2 = blastFurnace.getRelative(-1, 0, 0);
        Block bottom1 = campfire.getRelative(1, 0, 0);
        Block bottom2 = campfire.getRelative(-1, 0, 0);
        boolean xAxisValid = side1.getType() == org.bukkit.Material.BRICKS && side2.getType() == org.bukkit.Material.BRICKS
                && bottom1.getType() == org.bukkit.Material.BRICKS && bottom2.getType() == org.bukkit.Material.BRICKS;

        Block side3 = blastFurnace.getRelative(0, 0, 1);
        Block side4 = blastFurnace.getRelative(0, 0, -1);
        Block bottom3 = campfire.getRelative(0, 0, 1);
        Block bottom4 = campfire.getRelative(0, 0, -1);
        boolean zAxisValid = side3.getType() == org.bukkit.Material.BRICKS && side4.getType() == org.bukkit.Material.BRICKS
                && bottom3.getType() == org.bukkit.Material.BRICKS && bottom4.getType() == org.bukkit.Material.BRICKS;

        java.util.List<Block> multiblockBlocks = new java.util.ArrayList<>();
        multiblockBlocks.add(blastFurnace);
        multiblockBlocks.add(ironBars);
        multiblockBlocks.add(campfire);

        if (xAxisValid) {
            multiblockBlocks.add(side1);
            multiblockBlocks.add(side2);
            multiblockBlocks.add(bottom1);
            multiblockBlocks.add(bottom2);
        } else if (zAxisValid) {
            multiblockBlocks.add(side3);
            multiblockBlocks.add(side4);
            multiblockBlocks.add(bottom3);
            multiblockBlocks.add(bottom4);
        }

        boolean outputToChest = org.ThefryGuy.techFactory.util.ItemUtils.outputToChest(multiblockBlocks, outputItem);
        boolean outputToGUI = false;

        if (!outputToChest) {
            // No chest found, try to put in GUI output slot
            outputToGUI = SmelterMachine.outputToGUI(loc, outputItem);
        }

        // BUG FIX 4: Throttled notification to nearby players
        // Instead of sending individual messages, batch them to prevent spam
        notifyNearbyPlayers(loc, output, outputToChest, outputToGUI);

        // PHASE 3: Check for queued recipes and auto-start next one
        processNextQueuedRecipe(loc);
    }

    /**
     * Notify nearby players about smelting completion
     *
     * BUG FIX 4: Batched notification system
     * - Only sends messages to players within notification distance
     * - Batches multiple completions into summary messages
     * - Prevents 5000+ messages when 50+ smelters complete at once
     * - Messages are flushed every tick by scheduled task
     */
    private void notifyNearbyPlayers(Location loc, RecipeItem output, boolean outputToChest, boolean outputToGUI) {
        // BUG FIX 3: Null checks
        if (loc == null || loc.getWorld() == null || output == null) {
            return;
        }

        // Build message
        String message = output.getColor() + output.getDisplayName();
        if (outputToChest) {
            message += ChatColor.GREEN + " → Chest";
        } else if (outputToGUI) {
            message += ChatColor.YELLOW + " → Output Slot";
        } else {
            message += ChatColor.RED + " (Output full!)";
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            // BUG FIX 3: Null check
            if (player == null || player.getWorld() == null) {
                continue;
            }

            // Only notify players in same world
            if (!player.getWorld().equals(loc.getWorld())) {
                continue;
            }

            // Only notify players within notification distance
            if (player.getLocation().distance(loc) >= TechFactoryConstants.SMELTING_NOTIFICATION_DISTANCE()) {
                continue;
            }

            // BUG FIX 4: Add to pending messages instead of sending immediately
            UUID playerId = player.getUniqueId();
            pendingMessages.computeIfAbsent(playerId, k -> new ArrayList<>()).add(message);
        }
    }

    /**
     * Flush pending messages to players
     * BUG FIX 4: Batches multiple smelting completions into summary messages
     * Called every tick by scheduled task
     */
    private void flushPendingMessages() {
        if (pendingMessages.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();

        for (Map.Entry<UUID, List<String>> entry : pendingMessages.entrySet()) {
            UUID playerId = entry.getKey();
            List<String> messages = entry.getValue();

            if (messages.isEmpty()) {
                continue;
            }

            Player player = Bukkit.getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                continue;
            }

            // Throttle: Only send if enough time has passed since last message
            Long lastTime = lastMessageTime.get(playerId);
            if (lastTime != null && (now - lastTime) < TechFactoryConstants.MESSAGE_SPAM_COOLDOWN_MS()) {
                continue; // Skip this player, will try again next tick
            }

            // Send batched message
            if (messages.size() == 1) {
                player.sendMessage(ChatColor.GREEN + "Smelter completed: " + messages.get(0));
            } else {
                player.sendMessage(ChatColor.GREEN + "Smelters completed (" + messages.size() + "):");
                // Show first 3 items, then summarize rest
                for (int i = 0; i < Math.min(3, messages.size()); i++) {
                    player.sendMessage(ChatColor.GRAY + "  • " + messages.get(i));
                }
                if (messages.size() > 3) {
                    player.sendMessage(ChatColor.GRAY + "  ... and " + (messages.size() - 3) + " more");
                }
            }

            lastMessageTime.put(playerId, now);
        }

        // Clear all pending messages
        pendingMessages.clear();
    }

    /**
     * Convert a location to a unique string key
     */
    private String locationToKey(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return "UNKNOWN";
        }
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    /**
     * Get the number of active operations
     */
    public int getActiveCount() {
        return activeOperations.size();
    }

    /**
     * Clear all operations (for plugin reload/shutdown)
     */
    public void clearAll() {
        activeOperations.clear();
    }

    /**
     * Cleanup old message throttling entries to prevent memory leak
     *
     * BUG FIX 4: Removes entries older than 5 minutes
     * Called every 30 seconds by background task
     */
    private void cleanupMessageThrottling() {
        long now = System.currentTimeMillis();
        long maxAge = 5 * 60 * 1000; // 5 minutes

        int sizeBefore = lastMessageTime.size();
        lastMessageTime.entrySet().removeIf(entry ->
            (now - entry.getValue()) > maxAge
        );

        int removed = sizeBefore - lastMessageTime.size();
        if (removed > 0 && TechFactoryConstants.LOG_RATE_LIMITER_CLEANUP()) {
            plugin.getLogger().fine("Cleaned up " + removed + " old message throttling entries");
        }
    }

    // ========================================
    // PHASE 3: RECIPE QUEUEING
    // ========================================

    /**
     * Get or create a queue for a smelter location
     * Loads from database if not in cache
     *
     * @param location The smelter location
     * @return The queue for this smelter (never null)
     */
    public SmeltingQueue getQueue(Location location) {
        String key = locationToKey(location);

        // Check cache first
        SmeltingQueue queue = queueCache.get(key);
        if (queue != null) {
            return queue;
        }

        // Try to load from database
        DatabaseManager dbManager = plugin.getDatabaseManager();
        queue = dbManager.loadSmeltingQueue(location);

        // If no queue in database, create new empty queue
        if (queue == null) {
            queue = new SmeltingQueue(location);
        }

        // Cache it
        queueCache.put(key, queue);
        return queue;
    }

    /**
     * Save a queue to database (async)
     *
     * @param queue The queue to save
     */
    public void saveQueue(SmeltingQueue queue) {
        DatabaseManager dbManager = plugin.getDatabaseManager();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            dbManager.saveSmeltingQueue(queue);
        });
    }

    /**
     * Process the next queued recipe for a smelter
     * Called automatically when a smelting operation completes
     *
     * @param location The smelter location
     */
    private void processNextQueuedRecipe(Location location) {
        SmeltingQueue queue = getQueue(location);

        // Check if there's a next recipe
        String nextRecipeId = queue.pollNext();
        if (nextRecipeId == null) {
            return; // Queue is empty
        }

        // Get the recipe from registry
        RecipeItem recipe = org.ThefryGuy.techFactory.registry.ItemRegistry.getItemById(nextRecipeId);
        if (recipe == null) {
            plugin.getLogger().log(Level.WARNING, "Queued recipe not found: " + nextRecipeId + " at " + location);
            // Continue processing queue (skip invalid recipe)
            processNextQueuedRecipe(location);
            return;
        }

        // Start the smelting operation
        startSmelting(location, recipe, TechFactoryConstants.SMELTING_DURATION_MS());

        // Save updated queue to database
        saveQueue(queue);

        // Notify nearby players
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().distance(location) <= TechFactoryConstants.SMELTING_NOTIFICATION_DISTANCE()) {
                player.sendMessage(ChatColor.GRAY + "[Queue] Auto-starting: " + recipe.getColor() + recipe.getDisplayName());
            }
        }
    }

    /**
     * Clear queue cache for a location (called when multiblock is destroyed)
     *
     * @param location The smelter location
     */
    public void clearQueueCache(Location location) {
        String key = locationToKey(location);
        queueCache.remove(key);

        // Also delete from database
        DatabaseManager dbManager = plugin.getDatabaseManager();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            dbManager.deleteSmeltingQueue(location);
        });
    }
}

