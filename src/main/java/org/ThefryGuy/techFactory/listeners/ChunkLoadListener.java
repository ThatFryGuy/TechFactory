package org.ThefryGuy.techFactory.listeners;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.data.DatabaseManager;

import java.util.logging.Level;

/**
 * Listens for chunks loading and loads their data from the database.
 *
 * WHY THIS EXISTS:
 * - When server starts, only spawn chunks are loaded into cache (fast startup)
 * - When player travels to a new chunk, that chunk loads
 * - This listener intercepts the ChunkLoadEvent and populates cache with that chunk's data
 * - Result: Cache stays in sync with what's actually in the world
 *
 * LOADS:
 * - Multiblocks (smelters, crushers, etc.)
 * - Placed blocks (energy regulators, connectors, generators)
 *
 * PERFORMANCE:
 * - Runs asynchronously to avoid blocking the main thread
 * - Only queries database for the specific chunk that loaded
 * - Uses chunk_x and chunk_z indexes for fast queries
 *
 * THREAD SAFETY:
 * - Caches are ConcurrentHashMaps, so async updates are safe
 * - No synchronization needed
 */
public class ChunkLoadListener implements Listener {

    private final TechFactory plugin;
    private final DatabaseManager databaseManager;

    public ChunkLoadListener(TechFactory plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    /**
     * When a chunk loads, load its data from database asynchronously
     *
     * IMPORTANT: This is called EVERY time a chunk loads, including:
     * - Server startup (spawn chunks)
     * - Player traveling to new areas
     * - Chunk loaders or other plugins forcing chunks to load
     *
     * We run this async to avoid blocking the server thread.
     */
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        String worldName = chunk.getWorld().getName();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        // Run asynchronously to avoid blocking the server
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Load multiblocks in this chunk
                databaseManager.loadChunkMultiblocks(worldName, chunkX, chunkZ);

                // Load placed blocks (energy blocks) in this chunk
                databaseManager.loadChunkBlocks(worldName, chunkX, chunkZ);

                // CRITICAL FIX: Retry orphaned energy devices when chunk loads
                // This solves the problem where connector chains span multiple chunks
                // and can't connect at startup because chunks aren't loaded yet
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getEnergyManager().retryOrphanedDevicesInChunk(worldName, chunkX, chunkZ);
                });

                // Log for debugging (use FINE level to avoid spam)
                plugin.getLogger().fine("ChunkLoad: Loaded data for chunk (" + chunkX + ", " + chunkZ + ") in world " + worldName);

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load data for chunk (" + chunkX + ", " + chunkZ + ")", e);
            }
        });
    }
}

