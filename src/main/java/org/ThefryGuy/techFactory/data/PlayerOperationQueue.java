package org.ThefryGuy.techFactory.data;

import org.bukkit.entity.Player;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.TechFactoryConstants;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Per-player operation queue to prevent DoS attacks
 * 
 * SECURITY CRITICAL: Prevents single player from lagging server
 * - Rate limits operations per player
 * - Queues operations instead of executing immediately
 * - Prevents spam-click exploits
 * 
 * Example usage:
 * if (!PlayerOperationQueue.canPerformOperation(player, OperationType.MULTIBLOCK_CRAFT)) {
 *     player.sendMessage("Too many operations! Please wait.");
 *     return;
 * }
 */
public class PlayerOperationQueue {

    // Operation types
    public enum OperationType {
        MULTIBLOCK_CRAFT,    // Crafting in multiblock machines
        SMELTING_START,      // Starting smelting operation
        INVENTORY_CLICK,     // Clicking in GUI
        BLOCK_PLACE,         // Placing TechFactory blocks
        BLOCK_BREAK          // Breaking TechFactory blocks
    }
    
    // Per-player operation tracking
    private static final Map<UUID, Map<OperationType, Queue<Long>>> PLAYER_OPERATIONS = new ConcurrentHashMap<>();
    
    /**
     * Check if player can perform an operation
     * 
     * @param player The player attempting the operation
     * @param type The type of operation
     * @return true if allowed, false if rate limited
     */
    public static boolean canPerformOperation(Player player, OperationType type) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        
        // Get or create player's operation map
        Map<OperationType, Queue<Long>> playerOps = PLAYER_OPERATIONS.computeIfAbsent(
            uuid, 
            k -> new ConcurrentHashMap<>()
        );
        
        // Get or create queue for this operation type
        Queue<Long> timestamps = playerOps.computeIfAbsent(
            type,
            k -> new ConcurrentLinkedQueue<>()
        );
        
        // Remove old timestamps outside the time window
        timestamps.removeIf(timestamp -> now - timestamp > TechFactoryConstants.RATE_LIMIT_TIME_WINDOW_MS());

        // Check if player has exceeded rate limit
        int maxOps = getMaxOperations(type);
        if (timestamps.size() >= maxOps) {
            return false; // Rate limited
        }

        // Add current timestamp
        timestamps.offer(now);
        return true;
    }

    /**
     * Get maximum operations per second for a given type
     */
    private static int getMaxOperations(OperationType type) {
        return switch (type) {
            case MULTIBLOCK_CRAFT -> TechFactoryConstants.MAX_MULTIBLOCK_CRAFTS_PER_SECOND();
            case SMELTING_START -> TechFactoryConstants.MAX_SMELTING_STARTS_PER_SECOND();
            case INVENTORY_CLICK -> TechFactoryConstants.MAX_INVENTORY_CLICKS_PER_SECOND();
            case BLOCK_PLACE -> TechFactoryConstants.MAX_BLOCK_PLACES_PER_SECOND();
            case BLOCK_BREAK -> TechFactoryConstants.MAX_BLOCK_BREAKS_PER_SECOND();
        };
    }
    
    /**
     * Clean up data for disconnected players
     * Should be called periodically (e.g., every 5 minutes)
     */
    public static void cleanupDisconnectedPlayers() {
        PLAYER_OPERATIONS.entrySet().removeIf(entry -> {
            UUID uuid = entry.getKey();
            return org.bukkit.Bukkit.getPlayer(uuid) == null;
        });
    }
    
    /**
     * Get statistics for monitoring
     */
    public static String getStats() {
        int totalPlayers = PLAYER_OPERATIONS.size();
        int totalOperations = PLAYER_OPERATIONS.values().stream()
            .mapToInt(map -> map.values().stream().mapToInt(Queue::size).sum())
            .sum();
        
        return "Tracking " + totalPlayers + " players, " + totalOperations + " recent operations";
    }
    
    /**
     * Clear all data (for plugin reload/shutdown)
     */
    public static void clear() {
        PLAYER_OPERATIONS.clear();
    }
}

