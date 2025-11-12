package org.ThefryGuy.techFactory.registry;

/**
 * Interface for all system managers in TechFactory.
 * 
 * WHY THIS EXISTS:
 * - Provides consistent lifecycle management for all managers
 * - Allows ManagerRegistry to initialize/disable all managers uniformly
 * - Makes adding new managers require minimal code changes (just register + auto-init)
 * 
 * USAGE:
 * 1. Create a manager class that implements SystemManager
 * 2. Implement initialize() - called during plugin startup
 * 3. Implement disable() - called during plugin shutdown
 * 4. Register in TechFactory: ManagerRegistry.register("ManagerName", instance)
 * 5. ManagerRegistry.initializeAll() handles the rest
 * 
 * EXAMPLES:
 * - SmeltingManager: initialize() starts tick task, disable() stops task and saves operations
 * - EnergyManager: initialize() loads networks and starts holograms, disable() stops task
 * - DatabaseManager: initialize() creates tables, disable() closes connection
 * 
 * @see ManagerRegistry
 */
public interface SystemManager {
    
    /**
     * Initialize this manager.
     * Called during plugin startup after all managers are registered.
     * 
     * IMPORTANT:
     * - This is called AFTER all managers are created and registered
     * - Safe to access other managers via TechFactory.getInstance().getXxxManager()
     * - Should start background tasks, load data, etc.
     * - Should NOT throw exceptions - handle errors gracefully
     * 
     * EXAMPLES:
     * - SmeltingManager: Start tick task to check for completed operations
     * - EnergyManager: Load networks from database, start hologram updates
     * - DatabaseManager: Create database tables if they don't exist
     */
    void initialize();
    
    /**
     * Disable this manager.
     * Called during plugin shutdown before database closes.
     * 
     * IMPORTANT:
     * - This is called BEFORE database connection closes
     * - Safe to save data to database
     * - Should stop background tasks, save state, clean up resources
     * - Should NOT throw exceptions - handle errors gracefully
     * - Should be idempotent (safe to call multiple times)
     * 
     * EXAMPLES:
     * - SmeltingManager: Stop tick task, save active operations to database
     * - EnergyManager: Stop hologram task, save networks to database
     * - AutoSaveManager: Perform final save, stop task
     */
    void disable();
}

