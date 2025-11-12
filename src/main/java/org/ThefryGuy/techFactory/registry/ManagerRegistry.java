package org.ThefryGuy.techFactory.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central registry for all system managers in TechFactory.
 * 
 * WHY THIS EXISTS:
 * - Before: Adding a new manager required editing TechFactory.java in 4+ places
 * - After: Adding a new manager = 1 line (register call) + auto-initialization
 * - Provides consistent lifecycle management (initialize → disable)
 * - Makes the codebase more maintainable and scalable
 * 
 * USAGE IN TechFactory.onEnable():
 * ```java
 * // Create managers
 * databaseManager = new DatabaseManager(this);
 * smeltingManager = new SmeltingManager(this);
 * energyManager = new EnergyManager(this);
 * 
 * // Register all managers
 * ManagerRegistry.register("DatabaseManager", databaseManager);
 * ManagerRegistry.register("SmeltingManager", smeltingManager);
 * ManagerRegistry.register("EnergyManager", energyManager);
 * 
 * // Initialize all at once (calls initialize() on each in order)
 * ManagerRegistry.initializeAll(getLogger());
 * ```
 * 
 * USAGE IN TechFactory.onDisable():
 * ```java
 * // Disable all managers (calls disable() on each in reverse order)
 * ManagerRegistry.disableAll(getLogger());
 * ```
 * 
 * BENEFITS:
 * - Adding cargo system? Just register it - no listener changes needed
 * - Adding power panels? Just register it - initialization is automatic
 * - Consistent error handling across all managers
 * - Guaranteed initialization order (LinkedHashMap preserves insertion order)
 * - Guaranteed shutdown order (reverse of initialization)
 * 
 * @see SystemManager
 */
public class ManagerRegistry {
    
    /**
     * Map of manager name → manager instance
     * LinkedHashMap preserves insertion order for predictable initialization
     */
    private static final Map<String, SystemManager> managers = new LinkedHashMap<>();
    
    /**
     * Register a system manager.
     * 
     * IMPORTANT:
     * - Call this BEFORE initializeAll()
     * - Managers are initialized in the order they're registered
     * - Managers are disabled in REVERSE order (LIFO - Last In First Out)
     * 
     * @param name Unique name for this manager (e.g., "SmeltingManager")
     * @param manager The manager instance
     * @throws IllegalArgumentException if name is already registered
     */
    public static void register(String name, SystemManager manager) {
        if (managers.containsKey(name)) {
            throw new IllegalArgumentException("Manager already registered: " + name);
        }
        
        if (manager == null) {
            throw new IllegalArgumentException("Manager cannot be null: " + name);
        }
        
        managers.put(name, manager);
    }
    
    /**
     * Initialize all registered managers in order.
     * 
     * LIFECYCLE:
     * 1. DatabaseManager.initialize() - Creates tables
     * 2. MultiblockCache.initialize() - Loads multiblocks from DB
     * 3. EnergyManager.initialize() - Loads networks, starts holograms
     * 4. SmeltingManager.initialize() - Loads active operations, starts tick task
     * 5. AutoSaveManager.initialize() - Starts auto-save task
     * 
     * ERROR HANDLING:
     * - If a manager fails to initialize, logs error and continues
     * - Plugin will still start, but that manager may not work
     * - This prevents one broken manager from crashing the entire plugin
     * 
     * @param logger Logger for error reporting
     */
    public static void initializeAll(Logger logger) {
        logger.info("Initializing " + managers.size() + " system managers...");
        
        int successCount = 0;
        int failCount = 0;
        
        for (Map.Entry<String, SystemManager> entry : managers.entrySet()) {
            String name = entry.getKey();
            SystemManager manager = entry.getValue();
            
            try {
                logger.info("  → Initializing " + name + "...");
                manager.initialize();
                successCount++;
                logger.info("  ✓ " + name + " initialized successfully");
                
            } catch (Exception e) {
                failCount++;
                logger.log(Level.SEVERE, "  ✗ Failed to initialize " + name, e);
                // Continue initializing other managers even if one fails
            }
        }
        
        logger.info("Manager initialization complete: " + successCount + " succeeded, " + failCount + " failed");
    }
    
    /**
     * Disable all registered managers in REVERSE order.
     * 
     * WHY REVERSE ORDER:
     * - Managers may depend on each other
     * - Example: SmeltingManager needs DatabaseManager to save operations
     * - By disabling in reverse, we ensure dependencies are still available
     * - DatabaseManager is disabled LAST (after all others have saved their data)
     * 
     * LIFECYCLE (reverse of initialization):
     * 1. AutoSaveManager.disable() - Perform final save, stop task
     * 2. SmeltingManager.disable() - Save active operations, stop tick task
     * 3. EnergyManager.disable() - Save networks, stop holograms
     * 4. MultiblockCache.disable() - Clear cache, log stats
     * 5. DatabaseManager.disable() - Close connection (LAST)
     * 
     * ERROR HANDLING:
     * - If a manager fails to disable, logs error and continues
     * - This ensures all managers get a chance to clean up
     * - Prevents one broken manager from leaving others in a bad state
     * 
     * @param logger Logger for error reporting
     */
    public static void disableAll(Logger logger) {
        logger.info("Disabling " + managers.size() + " system managers...");
        
        int successCount = 0;
        int failCount = 0;
        
        // Convert to array and iterate in reverse
        String[] names = managers.keySet().toArray(new String[0]);
        for (int i = names.length - 1; i >= 0; i--) {
            String name = names[i];
            SystemManager manager = managers.get(name);
            
            try {
                logger.info("  → Disabling " + name + "...");
                manager.disable();
                successCount++;
                logger.info("  ✓ " + name + " disabled successfully");
                
            } catch (Exception e) {
                failCount++;
                logger.log(Level.SEVERE, "  ✗ Failed to disable " + name, e);
                // Continue disabling other managers even if one fails
            }
        }
        
        logger.info("Manager shutdown complete: " + successCount + " succeeded, " + failCount + " failed");
    }
    
    /**
     * Get a registered manager by name.
     * 
     * USAGE:
     * ```java
     * SystemManager manager = ManagerRegistry.getManager("SmeltingManager");
     * if (manager instanceof SmeltingManager) {
     *     SmeltingManager smeltingManager = (SmeltingManager) manager;
     *     // Use it...
     * }
     * ```
     * 
     * NOTE: Usually you should use TechFactory.getInstance().getXxxManager() instead
     * This method is mainly for debugging/testing
     * 
     * @param name Manager name
     * @return Manager instance, or null if not found
     */
    public static SystemManager getManager(String name) {
        return managers.get(name);
    }
    
    /**
     * Get the number of registered managers.
     * Mainly for debugging/testing.
     * 
     * @return Number of registered managers
     */
    public static int getManagerCount() {
        return managers.size();
    }
    
    /**
     * Clear all registered managers.
     * 
     * WARNING: Only use this for testing/debugging!
     * In production, managers should stay registered for the plugin's lifetime.
     */
    public static void clear() {
        managers.clear();
    }
}

