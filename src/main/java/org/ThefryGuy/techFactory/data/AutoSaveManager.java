package org.ThefryGuy.techFactory.data;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.registry.SystemManager;

import java.util.logging.Level;

/**
 * Manages automatic saving of all plugin data
 * Saves periodically and on shutdown to prevent data loss
 *
 * PERFORMANCE CRITICAL: Also handles cleanup of stale data
 * - Removes old player data
 * - Cleans up disconnected players
 * - Prevents database bloat
 *
 * LIFECYCLE: Implements SystemManager for automatic initialization/shutdown via ManagerRegistry
 */
public class AutoSaveManager implements SystemManager {

    private final TechFactory plugin;
    private BukkitRunnable autoSaveTask;
    private BukkitRunnable cleanupTask;
    private final int saveIntervalMinutes;
    private boolean enabled;
    
    /**
     * Create auto-save manager
     * @param plugin The plugin instance
     * @param saveIntervalMinutes How often to auto-save (in minutes)
     */
    public AutoSaveManager(TechFactory plugin, int saveIntervalMinutes) {
        this.plugin = plugin;
        this.saveIntervalMinutes = saveIntervalMinutes;
        this.enabled = true;
    }
    
    /**
     * Start the auto-save task
     */
    public void startTask() {
        if (!enabled) {
            return;
        }

        // Convert minutes to ticks (20 ticks = 1 second, 60 seconds = 1 minute)
        long intervalTicks = saveIntervalMinutes * 60 * 20L;

        autoSaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                performAutoSave(false);
            }
        };

        // Run async to avoid lag
        autoSaveTask.runTaskTimerAsynchronously(plugin, intervalTicks, intervalTicks);

        plugin.getLogger().info("Auto-save started! Saving every " + saveIntervalMinutes + " minutes.");

        // Start cleanup task (runs every 5 minutes)
        startCleanupTask();
    }

    /**
     * Start the cleanup task for stale data
     * Runs every 5 minutes to prevent memory/database bloat
     */
    private void startCleanupTask() {
        long cleanupIntervalTicks = 5 * 60 * 20L; // 5 minutes

        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                performCleanup();
            }
        };

        // Run async to avoid lag
        cleanupTask.runTaskTimerAsynchronously(plugin, cleanupIntervalTicks, cleanupIntervalTicks);

        plugin.getLogger().info("Cleanup task started! Running every 5 minutes.");
    }
    
    /**
     * Stop the auto-save task
     */
    public void stopTask() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
    }

    /**
     * Perform cleanup of stale data
     *
     * PERFORMANCE CRITICAL: Prevents database bloat
     * - Cleans up disconnected players from operation queues
     * - Future: Could remove blocks from inactive players
     */
    private void performCleanup() {
        long startTime = System.currentTimeMillis();

        try {
            // Clean up player operation queues
            PlayerOperationQueue.cleanupDisconnectedPlayers();

            // Clean up multiblock cache expired entries
            if (plugin.getMultiblockCache() != null) {
                plugin.getMultiblockCache().cleanupExpired();
            }

            // TODO: Future cleanup tasks
            // - Remove blocks owned by players offline for 2+ weeks
            // - Clean up orphaned energy networks
            // - Remove invalid multiblocks

            long duration = System.currentTimeMillis() - startTime;
            plugin.getLogger().info("Cleanup completed in " + duration + "ms");

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to perform cleanup!", e);
        }
    }
    
    /**
     * Perform an auto-save
     * @param isShutdown Whether this is a shutdown save
     */
    public void performAutoSave(boolean isShutdown) {
        if (!enabled && !isShutdown) {
            return;
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Note: SQLite auto-commits by default, so data is already saved
            // This method is here for future expansion (e.g., saving config, caches, etc.)
            
            // For now, we just log the save
            // In the future, we could:
            // - Flush any pending writes
            // - Save energy network states
            // - Save smelting operations
            // - Backup the database
            
            long duration = System.currentTimeMillis() - startTime;
            
            if (isShutdown) {
                plugin.getLogger().info("Shutdown save completed in " + duration + "ms");
            } else {
                plugin.getLogger().info("Auto-save completed in " + duration + "ms");
            }
            
            // Notify online ops if configured
            if (!isShutdown && plugin.getConfig().getBoolean("auto-save.notify-ops", false)) {
                String message = ChatColor.GRAY + "[TechFactory] Auto-save completed (" + duration + "ms)";
                Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.isOp())
                    .forEach(player -> player.sendMessage(message));
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to perform auto-save!", e);
        }
    }
    
    /**
     * Perform a manual save (triggered by command)
     */
    public void performManualSave() {
        plugin.getLogger().info("Manual save triggered...");
        performAutoSave(false);
    }
    
    /**
     * Enable or disable auto-save
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            plugin.getLogger().info("Auto-save enabled");
        } else {
            plugin.getLogger().info("Auto-save disabled");
        }
    }
    
    /**
     * Check if auto-save is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Get the save interval in minutes
     */
    public int getSaveIntervalMinutes() {
        return saveIntervalMinutes;
    }

    // ========================================
    // SYSTEM MANAGER INTERFACE
    // ========================================

    /**
     * Initialize method for SystemManager interface
     * Called by ManagerRegistry during plugin startup
     *
     * LIFECYCLE:
     * 1. Start auto-save task
     * 2. Start cleanup task
     */
    @Override
    public void initialize() {
        startTask();
    }

    /**
     * Disable method for SystemManager interface
     * Called by ManagerRegistry during plugin shutdown
     *
     * LIFECYCLE:
     * 1. Perform final save
     * 2. Stop all tasks
     */
    @Override
    public void disable() {
        performAutoSave(true); // Final save on shutdown
        stopTask();
    }
}

