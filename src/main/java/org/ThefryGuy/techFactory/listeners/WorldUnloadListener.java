package org.ThefryGuy.techFactory.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.ThefryGuy.techFactory.TechFactory;

/**
 * CRITICAL FIX: Listens for world unload events and cleans up energy networks
 * 
 * WHY THIS EXISTS:
 * - When a world is unloaded (deleted, disabled, etc.), all blocks in that world are gone
 * - Energy networks and devices in that world become invalid (null world references)
 * - Without cleanup, these dead objects stay in memory causing:
 *   1. Memory leaks (maps grow forever)
 *   2. NullPointerExceptions when iterating networks
 *   3. Confusion for players (devices vanish with no warning)
 * 
 * WHAT IT DOES:
 * - Saves all network energy states before cleanup (prevent data loss)
 * - Removes all networks in the unloaded world from memory
 * - Removes all devices in the unloaded world from memory
 * - Removes all holograms in the unloaded world
 * - Logs cleanup statistics for debugging
 * 
 * NOTE: Database entries are NOT deleted - if the world is reloaded later,
 * the networks will be restored from the database.
 */
public class WorldUnloadListener implements Listener {

    private final TechFactory plugin;

    public WorldUnloadListener(TechFactory plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle world unload - clean up all energy networks and devices in that world
     * 
     * PRIORITY: MONITOR - Run after other plugins have finished their cleanup
     * This ensures we don't interfere with other plugins' world unload logic
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldUnload(WorldUnloadEvent event) {
        String worldName = event.getWorld().getName();
        
        plugin.getLogger().info("World '" + worldName + "' is unloading - cleaning up energy networks...");
        
        // Clean up all energy networks and devices in this world
        plugin.getEnergyManager().cleanupWorld(worldName);
    }
}

