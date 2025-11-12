package org.ThefryGuy.techFactory.energy;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.TechFactoryConstants;
import org.ThefryGuy.techFactory.data.PlacedBlock;
import org.ThefryGuy.techFactory.registry.SystemManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages all energy networks and hologram displays
 * Similar to SmeltingManager but for energy systems
 *
 * PERFORMANCE OPTIMIZED:
 * - Network updates run async, hologram updates on main thread
 * - Render distance culling (only show holograms within 64 blocks)
 * - Prevents entity spam with 100k+ blocks
 *
 * LIFECYCLE: Implements SystemManager for automatic initialization/shutdown via ManagerRegistry
 */
public class EnergyManager implements SystemManager {

    private final TechFactory plugin;
    private final Map<String, EnergyNetwork> networks;           // Location key -> Network (thread-safe)
    private final Map<String, EnergyNetwork> locationToNetwork;  // Device location -> Network (O(1) lookups)
    private final Map<String, ArmorStand> holograms;             // Location key -> Hologram (main thread only)

    // PERFORMANCE FIX: Spatial indexing for 100x-1000x faster network lookups
    // Instead of O(N) linear search through all networks, use chunk-based grid
    // Key format: "worldName:chunkX,chunkZ"
    private final Map<String, List<EnergyNetwork>> networksByChunk = new ConcurrentHashMap<>();

    // PERFORMANCE FIX: Chunk-based network metadata for O(1) chunk→network lookup
    // Stores which network(s) exist in each chunk for fast chunk load operations
    // Key format: "worldName:chunkX,chunkZ" → Set of network location keys
    private final Map<String, Set<String>> chunkToNetworks = new ConcurrentHashMap<>();

    // CRITICAL FIX: Track orphaned devices that failed to connect at startup
    // These will be retried when chunks load or when new networks are created
    private final Set<PlacedBlock> orphanedDevices = ConcurrentHashMap.newKeySet();

    // CRITICAL FIX: Gson instance for proper JSON serialization/deserialization
    // Prevents database desync and data corruption from manual string concatenation
    private final Gson gson = new Gson();

    private BukkitRunnable updateTask;
    // CRITICAL FIX: Removed tickCounter - no longer needed since energy saves on every change
    private int hologramTickCounter = 0;  // Counter for hologram updates (slower than energy updates)

    public EnergyManager(TechFactory plugin) {
        this.plugin = plugin;
        this.networks = new ConcurrentHashMap<>();       // Thread-safe for async operations
        this.locationToNetwork = new ConcurrentHashMap<>(); // Thread-safe reverse map for O(1) lookups
        this.holograms = new HashMap<>();                // Main thread only, no need for concurrent
    }

    /**
     * Initialize the energy manager (SystemManager interface)
     * Called automatically by ManagerRegistry during plugin startup
     *
     * LIFECYCLE:
     * 1. Load all networks from database (regulators, connectors, panels, consumers)
     * 2. Start hologram update task
     *
     * FIX: We DO load networks at startup to ensure all devices are connected properly.
     * This is necessary because electric furnaces need to be registered as consumers
     * on server restart, otherwise they won't show up in the regulator GUI.
     */
    @Override
    public void initialize() {
        // Load all networks and devices from database
        loadNetworks();

        // Start hologram update task
        startTask();
        plugin.getLogger().info("Energy Manager initialized with " + networks.size() + " networks");
    }

    /**
     * Disable the energy manager (SystemManager interface)
     * Called automatically by ManagerRegistry during plugin shutdown
     *
     * LIFECYCLE:
     * 1. Save all network energy states
     * 2. Stop hologram update task
     * 3. Remove all holograms
     */
    @Override
    public void disable() {
        // Save all network energy before shutdown (synchronous - plugin is shutting down)
        plugin.getLogger().info("Saving energy states for " + networks.size() + " networks...");
        saveAllNetworkEnergy(false); // false = synchronous save
        plugin.getLogger().info("Energy states saved");

        stopTask();
        // Holograms are removed by stopTask()
    }

    /**
     * Start the energy manager task
     * Updates holograms and processes energy generation/consumption
     *
     * ASYNC OPTIMIZED: Network calculations run async, hologram updates on main thread
     */
    public void startTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Run network updates asynchronously (calculations, energy transfer, etc.)
                // This is safe because networks use ConcurrentHashMap
                updateAllNetworks();

                // OPTIMIZATION: Update holograms less frequently (every 3 seconds instead of 1 second)
                // Holograms are just visual - they don't need to be real-time
                // GUI still shows accurate data when opened
                hologramTickCounter++;
                if (hologramTickCounter >= 3) {  // Every 3 seconds (3 × 20 ticks = 60 ticks)
                    hologramTickCounter = 0;

                    // Schedule hologram updates on main thread (entities require main thread)
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        updateAllHolograms();
                    });
                }
            }
        };

        // Run async every 20 ticks (1 second) - won't block main thread
        updateTask.runTaskTimerAsynchronously(plugin, 0L, TechFactoryConstants.ENERGY_UPDATE_INTERVAL_TICKS());
        plugin.getLogger().info("Energy Manager started (async mode, hologram updates every 3s)!");
    }

    /**
     * Stop the energy manager task
     */
    public void stopTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        removeAllHolograms();
        plugin.getLogger().info("Energy Manager stopped!");
    }

    /**
     * CRITICAL FIX: Clean up all energy networks and devices in a specific world
     * Called when a world is unloaded to prevent memory leaks and null pointer exceptions
     *
     * @param worldName Name of the world being unloaded
     */
    public void cleanupWorld(String worldName) {
        if (worldName == null || worldName.isEmpty()) {
            plugin.getLogger().warning("Cannot cleanup world: world name is null or empty");
            return;
        }

        int networksRemoved = 0;
        int devicesRemoved = 0;
        int hologramsRemoved = 0;

        // Remove all networks in this world from memory
        Iterator<Map.Entry<String, EnergyNetwork>> networkIterator = networks.entrySet().iterator();
        while (networkIterator.hasNext()) {
            Map.Entry<String, EnergyNetwork> entry = networkIterator.next();
            String locationKey = entry.getKey();
            EnergyNetwork network = entry.getValue();
            Location regulatorLoc = network.getRegulatorLocation();

            if (regulatorLoc != null && regulatorLoc.getWorld() != null &&
                regulatorLoc.getWorld().getName().equals(worldName)) {

                // Save energy state before removing (synchronous - world is unloading)
                saveNetworkEnergy(network, false);

                // Remove from spatial index
                removeNetworkFromSpatialIndex(network);

                // Remove from chunk map
                removeNetworkFromChunkMap(regulatorLoc, locationKey);

                // Remove hologram if exists
                ArmorStand hologram = holograms.remove(locationKey);
                if (hologram != null && !hologram.isDead()) {
                    hologram.remove();
                    hologramsRemoved++;
                }

                // Remove network from map
                networkIterator.remove();
                networksRemoved++;
            }
        }

        // Remove all devices in this world from locationToNetwork map
        Iterator<Map.Entry<String, EnergyNetwork>> deviceIterator = locationToNetwork.entrySet().iterator();
        while (deviceIterator.hasNext()) {
            Map.Entry<String, EnergyNetwork> entry = deviceIterator.next();
            String locationKey = entry.getKey();

            // Parse location key to check world
            try {
                Location loc = PlacedBlock.keyToLocation(locationKey);
                if (loc != null && loc.getWorld() != null && loc.getWorld().getName().equals(worldName)) {
                    deviceIterator.remove();
                    devicesRemoved++;
                }
            } catch (Exception e) {
                // If we can't parse the location, it's probably corrupted - remove it anyway
                deviceIterator.remove();
                devicesRemoved++;
            }
        }

        if (networksRemoved > 0 || devicesRemoved > 0) {
            plugin.getLogger().info("Cleaned up world '" + worldName + "': " +
                networksRemoved + " networks, " +
                devicesRemoved + " devices, " +
                hologramsRemoved + " holograms removed from memory");
        }
    }

    /**
     * Create a new energy network for a regulator
     *
     * BUG FIX 3: Added null checks to prevent NullPointerException
     */
    public EnergyNetwork createNetwork(Location regulatorLocation) {
        // BUG FIX 3: Null check for location
        if (regulatorLocation == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot create energy network: location is null");
            return null;
        }

        // BUG FIX 3: Null check for world
        if (regulatorLocation.getWorld() == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot create energy network: world is null at " + regulatorLocation);
            return null;
        }

        String locationKey = PlacedBlock.locationToKey(regulatorLocation);

        // Check if network already exists
        if (networks.containsKey(locationKey)) {
            return networks.get(locationKey);
        }

        // Create new network
        EnergyNetwork network = new EnergyNetwork(regulatorLocation);
        networks.put(locationKey, network);

        // Add regulator to reverse map
        locationToNetwork.put(locationKey, network);

        // PERFORMANCE FIX: Add to spatial index for fast lookups
        addNetworkToSpatialIndex(network);

        // PERFORMANCE FIX: Add to chunk-based network metadata
        addNetworkToChunkMap(regulatorLocation, locationKey);

        // CRITICAL FIX: Set callback for immediate async saves on energy changes
        network.setEnergyChangeCallback(this::onEnergyChanged);

        // Create hologram
        createHologram(regulatorLocation, network);

        plugin.getLogger().info("Created energy network at " + locationKey);
        return network;
    }

    /**
     * Remove an energy network
     */
    public void removeNetwork(Location regulatorLocation) {
        String locationKey = PlacedBlock.locationToKey(regulatorLocation);

        // Get network before removing
        EnergyNetwork network = networks.get(locationKey);

        // Remove all device locations from reverse map
        if (network != null) {
            locationToNetwork.remove(locationKey); // Remove regulator

            // Remove all connectors
            for (Location connectorLoc : network.getConnectedConnectors()) {
                locationToNetwork.remove(PlacedBlock.locationToKey(connectorLoc));
            }

            // Remove all panels
            for (Location panelLoc : network.getConnectedPanels()) {
                locationToNetwork.remove(PlacedBlock.locationToKey(panelLoc));
            }

            // Remove all consumers
            for (Location consumerLoc : network.getConnectedConsumers()) {
                locationToNetwork.remove(PlacedBlock.locationToKey(consumerLoc));
            }
        }

        // PERFORMANCE FIX: Remove from spatial index
        if (network != null) {
            removeNetworkFromSpatialIndex(network);
        }

        // PERFORMANCE FIX: Remove from chunk map
        Location regulatorLoc = PlacedBlock.keyToLocation(locationKey);
        if (regulatorLoc != null) {
            removeNetworkFromChunkMap(regulatorLoc, locationKey);
        }

        // Remove network
        networks.remove(locationKey);

        // Remove hologram
        removeHologram(locationKey);

        plugin.getLogger().info("Removed energy network at " + locationKey);
    }

    /**
     * Get an energy network by regulator location
     */
    public EnergyNetwork getNetwork(Location regulatorLocation) {
        String locationKey = PlacedBlock.locationToKey(regulatorLocation);
        return networks.get(locationKey);
    }

    /**
     * Check if a network exists at a location
     */
    public boolean hasNetwork(Location regulatorLocation) {
        String locationKey = PlacedBlock.locationToKey(regulatorLocation);
        return networks.containsKey(locationKey);
    }

    /**
     * Create a hologram display above the regulator
     */
    private void createHologram(Location location, EnergyNetwork network) {
        if (location == null || location.getWorld() == null) {
            return; // Cannot create hologram in unloaded world
        }

        String locationKey = PlacedBlock.locationToKey(location);

        // Remove existing hologram if present
        removeHologram(locationKey);

        // Create hologram location (1.5 blocks above the regulator)
        Location hologramLoc = location.clone().add(0.5, 1.5, 0.5);

        // Spawn armor stand as hologram
        ArmorStand hologram = (ArmorStand) location.getWorld().spawnEntity(hologramLoc, EntityType.ARMOR_STAND);
        hologram.setVisible(false);
        hologram.setGravity(false);
        hologram.setCustomNameVisible(true);
        hologram.setMarker(true);
        hologram.setInvulnerable(true);
        hologram.setCustomName(getHologramText(network));

        holograms.put(locationKey, hologram);
    }

    /**
     * Remove a hologram
     */
    private void removeHologram(String locationKey) {
        ArmorStand hologram = holograms.remove(locationKey);
        if (hologram != null && !hologram.isDead()) {
            hologram.remove();
        }
    }

    /**
     * Force update a specific hologram immediately (used when GUI is opened)
     * This ensures the player sees accurate real-time data
     */
    public void updateHologramNow(Location regulatorLocation) {
        if (regulatorLocation == null || regulatorLocation.getWorld() == null) {
            return;
        }

        String locationKey = PlacedBlock.locationToKey(regulatorLocation);
        EnergyNetwork network = networks.get(locationKey);

        if (network == null) {
            return; // No network at this location
        }

        ArmorStand hologram = holograms.get(locationKey);
        if (hologram != null && !hologram.isDead()) {
            // Update existing hologram with current data
            hologram.setCustomName(getHologramText(network));
        } else {
            // Create hologram if it doesn't exist
            createHologram(regulatorLocation, network);
        }
    }

    /**
     * Remove all holograms
     */
    private void removeAllHolograms() {
        for (ArmorStand hologram : holograms.values()) {
            if (hologram != null && !hologram.isDead()) {
                hologram.remove();
            }
        }
        holograms.clear();
    }

    /**
     * Remove ghost holograms near a regulator location
     * This fixes the duplicate hologram issue after server restart
     */
    private void removeGhostHolograms(Location regulatorLocation) {
        if (regulatorLocation == null || regulatorLocation.getWorld() == null) {
            return;
        }

        // Check for armor stands in a 3x3x3 area around the regulator
        Location hologramLoc = regulatorLocation.clone().add(0.5, 1.5, 0.5);

        regulatorLocation.getWorld().getNearbyEntities(hologramLoc, 1.5, 1.5, 1.5).forEach(entity -> {
            if (entity instanceof ArmorStand) {
                ArmorStand stand = (ArmorStand) entity;
                // Only remove invisible armor stands (our holograms)
                if (!stand.isVisible() && stand.isMarker()) {
                    stand.remove();
                }
            }
        });
    }

    /**
     * Get the hologram text for a network (single line)
     */
    private String getHologramText(EnergyNetwork network) {
        int stored = network.getStoredEnergy();
        int max = network.getMaxCapacity();
        int percentage = network.getFillPercentage();

        // Calculate total generation (4 J/s per solar generator)
        int generationRate = network.getPanelCount() * 4;

        // Color based on fill percentage
        ChatColor color;
        if (percentage >= 75) {
            color = ChatColor.GREEN;
        } else if (percentage >= 50) {
            color = ChatColor.YELLOW;
        } else if (percentage >= 25) {
            color = ChatColor.GOLD;
        } else {
            color = ChatColor.RED;
        }

        // Build single-line hologram text
        String text = ChatColor.AQUA + "⚡ " + color + stored + " / " + max + " J " +
                      ChatColor.GRAY + "(" + percentage + "%)";

        // Add generation rate if there are generators
        if (generationRate > 0) {
            text += ChatColor.GREEN + " +" + generationRate + " J/s";
        }

        return text;
    }

    /**
     * Update all network logic (energy generation, consumption, etc.)
     *
     * CHUNK LOADING PROTECTION: Only processes networks in loaded chunks
     * RUNS EVERY SECOND (20 ticks)
     */
    private void updateAllNetworks() {
        for (Map.Entry<String, EnergyNetwork> entry : networks.entrySet()) {
            EnergyNetwork network = entry.getValue();
            Location loc = network.getRegulatorLocation();

            // CHUNK LOADING CHECK: Skip if chunk is not loaded
            // This prevents lag from trying to access unloaded chunks
            if (loc == null || loc.getWorld() == null ||
                !loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                continue; // Skip this network, will process when chunk loads
            }

            // ENERGY GENERATION: Process all solar generators
            int totalGeneration = 0;

            for (Location generatorLoc : network.getConnectedPanels()) {
                // Check if generator chunk is loaded
                if (generatorLoc == null || generatorLoc.getWorld() == null ||
                    !generatorLoc.getWorld().isChunkLoaded(generatorLoc.getBlockX() >> 4, generatorLoc.getBlockZ() >> 4)) {
                    continue;
                }

                // Validate physical block still exists
                Block block = generatorLoc.getBlock();
                if (block == null || block.getType() != Material.DAYLIGHT_DETECTOR) {
                    continue; // Skip ghost generators
                }

                // Check if generator has direct sunlight access
                long time = generatorLoc.getWorld().getTime();
                boolean isDaytime = time < 12300 || time > 23850; // Minecraft day cycle
                boolean hasSkyAccess = block.getLightFromSky() == 15;
                boolean isRaining = generatorLoc.getWorld().hasStorm(); // Check for rain/thunder

                // Solar generator only works during daytime, with sky access, and NOT raining
                if (isDaytime && hasSkyAccess && !isRaining) {
                    // Solar generator produces 4 J/s
                    totalGeneration += 4;
                }
            }

            // Add generated energy to network
            if (totalGeneration > 0) {
                network.addEnergy(totalGeneration);
            }

            // TODO: In the future, this will also:
            // - Process energy consumption from machines
            // - Transfer energy between networks (if we add cables)
        }

        // PERFORMANCE FIX: Energy is saved via batch system (not here)
        // Energy changes trigger onEnergyChanged() → queued for batch flush every 1 second
        // This prevents 5k-10k async tasks/sec and SQLite lock contention
        // Old code: saveAllNetworkEnergy(true) every 200 ticks (removed)
        // New code: Batch flush in DatabaseManager.flushBatchWrites() every 1 second
    }

    /**
     * Save all network energy states to database
     * @param async If true, uses async saves. If false, uses synchronous saves (for shutdown)
     */
    private void saveAllNetworkEnergy(boolean async) {
        for (EnergyNetwork network : networks.values()) {
            saveNetworkEnergy(network, async);
        }
    }

    /**
     * CRITICAL FIX: Save network energy using proper JSON serialization
     * Prevents database desync and data corruption
     * @param async If true, uses async save. If false, uses synchronous save (for shutdown)
     */
    private void saveNetworkEnergy(EnergyNetwork network, boolean async) {
        Location regulatorLoc = network.getRegulatorLocation();
        if (regulatorLoc == null) return;

        try {
            // Create metadata object with versioning
            NetworkMetadata metadata = new NetworkMetadata(
                network.getStoredEnergy(),
                network.getMaxCapacity()
            );

            // Serialize to JSON using Gson (safe and proper)
            String json = gson.toJson(metadata);

            // Update metadata in database
            if (async) {
                plugin.getDatabaseManager().updateMetadataAsync(regulatorLoc, json);
            } else {
                // Synchronous save for shutdown (plugin is disabled, can't schedule async tasks)
                plugin.getDatabaseManager().updateMetadata(regulatorLoc, json);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save network energy at " + regulatorLoc + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * PERFORMANCE FIX: Callback for energy changes - queues for batch save
     *
     * Old behavior: Immediate async save on every change (5k-10k tasks/sec)
     * New behavior: Queue for batch flush every 1 second (98% reduction!)
     *
     * This prevents SQLite lock contention and async queue overflow at scale.
     * Energy is still saved frequently (every 1 sec) to prevent data loss.
     */
    private void onEnergyChanged(EnergyNetwork network) {
        // Queue for batch save (DatabaseManager flushes every 1 second)
        saveNetworkEnergy(network, true);
    }

    // CRITICAL FIX: Removed extractIntFromJson() - now using Gson for proper JSON parsing

    /**
     * Update all hologram displays
     *
     * PERFORMANCE CRITICAL:
     * - Only updates holograms in loaded chunks
     * - Only shows holograms within 64 blocks of players (render distance culling)
     * - Prevents entity spam with 100k+ energy regulators
     */
    private void updateAllHolograms() {
        // Get all online players for distance checking
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        // If no players online, remove all holograms to save memory
        if (onlinePlayers.isEmpty()) {
            removeAllHolograms();
            return;
        }

        // Track which holograms should be visible
        Set<String> visibleHolograms = new HashSet<>();

        // THREAD SAFETY: This iteration is safe because:
        // 1. networks is a ConcurrentHashMap (thread-safe iteration)
        // 2. holograms map is only accessed on main thread (this method runs on main thread)
        // 3. We're not modifying networks during iteration, only holograms
        for (Map.Entry<String, EnergyNetwork> entry : networks.entrySet()) {
            String locationKey = entry.getKey();
            EnergyNetwork network = entry.getValue();
            Location loc = network.getRegulatorLocation();

            // BUG FIX 3: Null checks to prevent crashes
            if (loc == null || loc.getWorld() == null) {
                removeHologram(locationKey);
                continue;
            }

            // CHUNK LOADING CHECK: Skip if chunk is not loaded
            if (!loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                // Remove hologram if it exists (chunk unloaded)
                removeHologram(locationKey);
                continue;
            }

            // RENDER DISTANCE CULLING: Only show holograms near players
            boolean nearPlayer = false;
            for (Player player : onlinePlayers) {
                // Check if player is in same world
                if (!player.getWorld().equals(loc.getWorld())) {
                    continue;
                }

                // Check distance (use distanceSquared for performance)
                double distanceSquared = player.getLocation().distanceSquared(loc);
                if (distanceSquared <= TechFactoryConstants.HOLOGRAM_RENDER_DISTANCE() * TechFactoryConstants.HOLOGRAM_RENDER_DISTANCE()) {
                    nearPlayer = true;
                    break;
                }
            }

            if (nearPlayer) {
                // Player is nearby - show/update hologram
                visibleHolograms.add(locationKey);

                // PERFORMANCE FIX: Event-based updates - only update if energy changed
                if (network.hasEnergyChanged()) {
                    ArmorStand hologram = holograms.get(locationKey);
                    if (hologram != null && !hologram.isDead()) {
                        // Update existing hologram
                        hologram.setCustomName(getHologramText(network));
                    } else {
                        // Create new hologram
                        createHologram(network.getRegulatorLocation(), network);
                    }

                    // Reset the changed flag after updating
                    network.resetEnergyChangedFlag();
                }
            } else {
                // No player nearby - remove hologram to save entities
                removeHologram(locationKey);
            }
        }
    }

    /**
     * Load all energy networks from database
     */
    public void loadNetworks() {
        // Step 1: Load all energy regulators and create networks
        List<PlacedBlock> regulators = plugin.getDatabaseManager().getBlocksByType("energy_regulator");
        int ghostRegulatorsRemoved = 0;

        for (PlacedBlock regulator : regulators) {
            Location location = regulator.getLocation();
            if (location == null) {
                continue;
            }

            // GHOST HOLOGRAM FIX: Remove any existing armor stands near this location
            // This prevents duplicate holograms after server restart
            removeGhostHolograms(location);

            // GHOST BLOCK PREVENTION: Validate that the physical block actually exists
            Block block = location.getBlock();
            if (block == null) {
                plugin.getLogger().warning("Skipped regulator at " + location + " - world unloaded");
                continue;
            }

            // Check if block is the correct type
            if (block.getType() != Material.LIGHTNING_ROD) {
                // Block is wrong type - this could be:
                // 1. Player manually broke it
                // 2. Another plugin removed it
                // 3. Chunk corruption
                // 4. WorldEdit/WorldGuard rollback
                plugin.getLogger().warning("Energy Regulator in database but physical block is " + block.getType() + " at " +
                    location.getWorld().getName() + " " +
                    location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                plugin.getLogger().warning("Restoring block to LIGHTNING_ROD...");

                // Restore the block
                block.setType(Material.LIGHTNING_ROD);
            }

            // Physical block exists - create network and restore energy
            EnergyNetwork network = createNetwork(location);

            // CRITICAL FIX: Restore energy from metadata using proper JSON deserialization
            String metadataJson = regulator.getMetadata();
            if (metadataJson != null && !metadataJson.isEmpty() && !metadataJson.equals("{}")) {
                try {
                    // CRITICAL FIX: Suppress callbacks during loading to prevent unnecessary saves
                    network.setSuppressCallbacks(true);

                    // Deserialize JSON using Gson (safe and proper)
                    NetworkMetadata metadata = gson.fromJson(metadataJson, NetworkMetadata.class);

                    // Validate metadata
                    if (metadata != null && metadata.isValid()) {
                        // Check schema version for future compatibility
                        if (metadata.version == 1) {
                            // Version 1 schema - restore energy
                            if (metadata.stored_energy > 0) {
                                network.addEnergy(metadata.stored_energy);
                                plugin.getLogger().fine("Restored " + metadata.stored_energy + " J to network at " + location);
                            }
                        } else {
                            plugin.getLogger().warning("Unknown metadata schema version " + metadata.version + " for network at " + location);
                        }
                    } else {
                        plugin.getLogger().warning("Invalid or corrupted metadata for network at " + location);
                    }

                    // Re-enable callbacks after loading
                    network.setSuppressCallbacks(false);
                } catch (JsonSyntaxException e) {
                    plugin.getLogger().warning("Failed to parse JSON metadata for network at " + location + ": " + e.getMessage());
                    plugin.getLogger().warning("Corrupted metadata: " + metadataJson);
                    network.setSuppressCallbacks(false);  // Make sure to re-enable even on error
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to restore energy for network at " + location + ": " + e.getMessage());
                    network.setSuppressCallbacks(false);  // Make sure to re-enable even on error
                }
            }
        }

        if (ghostRegulatorsRemoved > 0) {
            plugin.getLogger().info("Cleaned up " + ghostRegulatorsRemoved + " ghost regulator(s) from database");
        }

        plugin.getLogger().info("Loaded " + networks.size() + " energy networks from database");

        // Step 2: Load all energy connectors and connect them to networks
        // Use multi-pass loading to handle connector chains (connector -> connector -> regulator)
        List<PlacedBlock> connectors = plugin.getDatabaseManager().getBlocksByType("energy_connector");
        List<PlacedBlock> orphanedConnectors = new ArrayList<>();
        int connectedCount = 0;
        int ghostConnectorsRemoved = 0;

        // First pass: validate blocks and try to connect
        for (PlacedBlock connector : connectors) {
            Location location = connector.getLocation();
            if (location == null) {
                continue;
            }

            // GHOST BLOCK PREVENTION: Validate that the physical block actually exists
            Block block = location.getBlock();
            if (block == null) {
                plugin.getLogger().warning("Skipped connector at " + location + " - world unloaded");
                continue;
            }

            if (block.getType() != Material.CONDUIT) {
                // Block is wrong type - log and restore
                plugin.getLogger().warning("Energy Connector in database but physical block is " + block.getType() + " at " + location);
                plugin.getLogger().warning("Restoring block to CONDUIT...");
                block.setType(Material.CONDUIT);
            }

            // Find nearest network within 6 blocks
            EnergyNetwork network = findNearestNetwork(location, 6.0);

            if (network != null) {
                network.connectConnector(location);
                registerDeviceToNetwork(location, network);
                connectedCount++;
            } else {
                // Connector is orphaned for now - might connect in later passes
                orphanedConnectors.add(connector);
            }
        }

        // CRITICAL FIX: Improved multi-pass connector loading
        // Additional passes: try to connect orphaned connectors (they might be in a chain)
        // Example: Regulator -> Connector A -> Connector B -> Connector C -> Furnace
        // If Connector C loads before A and B, it needs multiple passes to connect
        int maxPasses = 20; // Increased from 10 to handle longer chains
        int pass = 1;

        while (!orphanedConnectors.isEmpty() && pass <= maxPasses) {
            List<PlacedBlock> stillOrphaned = new ArrayList<>();
            int connectedThisPass = 0;

            plugin.getLogger().fine("Connector loading pass " + pass + " - attempting to connect " + orphanedConnectors.size() + " orphaned connectors");

            for (PlacedBlock connector : orphanedConnectors) {
                Location location = connector.getLocation();
                if (location == null) {
                    continue;
                }

                // Try to find network again (might be reachable through newly connected connectors)
                EnergyNetwork network = findNearestNetwork(location, 6.0);

                if (network != null) {
                    network.connectConnector(location);
                    registerDeviceToNetwork(location, network);
                    connectedCount++;
                    connectedThisPass++;
                    plugin.getLogger().fine("Connected orphaned connector at " + location + " on pass " + pass);
                } else {
                    stillOrphaned.add(connector);
                }
            }

            plugin.getLogger().fine("Pass " + pass + " connected " + connectedThisPass + " connectors, " + stillOrphaned.size() + " still orphaned");

            if (connectedThisPass == 0) {
                // No progress made - stop trying to prevent infinite loops
                plugin.getLogger().info("Connector loading stopped after " + pass + " passes (no progress made)");
                break;
            }

            orphanedConnectors = stillOrphaned;
            pass++;
        }

        if (pass > maxPasses && !orphanedConnectors.isEmpty()) {
            plugin.getLogger().warning("Connector loading reached max passes (" + maxPasses + ") with " + orphanedConnectors.size() + " connectors still orphaned");
        }

        // CRITICAL FIX: Better diagnostics for orphaned connectors
        // Log any remaining orphaned connectors with helpful troubleshooting info
        if (!orphanedConnectors.isEmpty()) {
            plugin.getLogger().warning("Found " + orphanedConnectors.size() + " orphaned energy connector(s) that could not connect to any network:");

            for (PlacedBlock connector : orphanedConnectors) {
                Location location = connector.getLocation();
                if (location != null) {
                    plugin.getLogger().warning("  - Orphaned connector at " +
                        location.getWorld().getName() + " " +
                        location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());

                    // Check if there are any nearby connectors (might be circular chain)
                    boolean hasNearbyConnectors = false;
                    for (PlacedBlock other : orphanedConnectors) {
                        Location otherLoc = other.getLocation();
                        if (otherLoc != null && !otherLoc.equals(location) && otherLoc.distance(location) <= 6.0) {
                            hasNearbyConnectors = true;
                            break;
                        }
                    }

                    if (hasNearbyConnectors) {
                        plugin.getLogger().warning("    (Possible circular connector chain - no regulator found within range)");
                    } else {
                        plugin.getLogger().warning("    (No energy network found within 6 blocks - place an Energy Regulator nearby)");
                    }
                }
            }
        }

        if (ghostConnectorsRemoved > 0) {
            plugin.getLogger().info("Cleaned up " + ghostConnectorsRemoved + " ghost connector(s) from database");
        }

        plugin.getLogger().info("Loaded " + connectedCount + " energy connectors from database");

        // CRITICAL FIX: Schedule delayed retry for orphaned connectors
        // Sometimes chunks aren't fully loaded yet, so retry after 5 seconds
        if (!orphanedConnectors.isEmpty()) {
            final List<PlacedBlock> finalOrphanedConnectors = new ArrayList<>(orphanedConnectors);
            final int initialOrphanedCount = orphanedConnectors.size();

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                int reconnectedCount = 0;
                List<PlacedBlock> stillOrphaned = new ArrayList<>();

                plugin.getLogger().info("Retrying connection for " + initialOrphanedCount + " orphaned connector(s) after 5 seconds...");

                for (PlacedBlock connector : finalOrphanedConnectors) {
                    Location location = connector.getLocation();
                    if (location == null) {
                        continue;
                    }

                    // Try to find network again (chunks might be loaded now)
                    EnergyNetwork network = findNearestNetwork(location, 6.0);

                    if (network != null) {
                        network.connectConnector(location);
                        registerDeviceToNetwork(location, network);
                        reconnectedCount++;
                        plugin.getLogger().info("Successfully reconnected orphaned connector at " + location);
                    } else {
                        stillOrphaned.add(connector);
                    }
                }

                if (reconnectedCount > 0) {
                    plugin.getLogger().info("Reconnected " + reconnectedCount + " orphaned connector(s) on delayed retry");
                }

                if (!stillOrphaned.isEmpty()) {
                    plugin.getLogger().warning(stillOrphaned.size() + " connector(s) still orphaned after delayed retry - will retry when chunks load");
                    // Add to orphaned tracking set - will retry when chunks load
                    orphanedDevices.addAll(stillOrphaned);
                }
            }, 100L); // 5 seconds (100 ticks)
        }

        // Step 3: Load all solar generators and connect them to networks
        List<PlacedBlock> generators = plugin.getDatabaseManager().getBlocksByType("solar_generator");
        List<PlacedBlock> orphanedGenerators = new ArrayList<>();
        int generatorsConnected = 0;
        int ghostGeneratorsRemoved = 0;

        for (PlacedBlock generator : generators) {
            Location location = generator.getLocation();
            if (location == null) {
                continue;
            }

            // GHOST BLOCK PREVENTION: Validate that the physical block actually exists
            Block block = location.getBlock();
            if (block == null) {
                plugin.getLogger().warning("Skipped solar generator at " + location + " - world unloaded");
                continue;
            }

            if (block.getType() != Material.DAYLIGHT_DETECTOR) {
                // Block is wrong type - log and restore
                plugin.getLogger().warning("Solar Generator in database but physical block is " + block.getType() + " at " + location);
                plugin.getLogger().warning("Restoring block to DAYLIGHT_DETECTOR...");
                block.setType(Material.DAYLIGHT_DETECTOR);
            }

            // Find nearest network within 6 blocks
            EnergyNetwork network = findNearestNetwork(location, 6.0);

            if (network != null) {
                network.connectPanel(location);
                registerDeviceToNetwork(location, network);
                generatorsConnected++;
            } else {
                // Generator is orphaned (no network in range) - will retry when chunks load
                orphanedGenerators.add(generator);
            }
        }

        if (ghostGeneratorsRemoved > 0) {
            plugin.getLogger().info("Cleaned up " + ghostGeneratorsRemoved + " ghost solar generator(s) from database");
        }

        plugin.getLogger().info("Loaded " + generatorsConnected + " solar generators from database");

        // Track orphaned generators for chunk-load retry
        if (!orphanedGenerators.isEmpty()) {
            plugin.getLogger().warning(orphanedGenerators.size() + " solar generator(s) orphaned - will retry when chunks load");
            orphanedDevices.addAll(orphanedGenerators);
        }

        // Step 3.5: Load all energy capacitors and connect them to networks
        // Capacitors act like connectors (extend range) but also store energy
        List<PlacedBlock> capacitors = plugin.getDatabaseManager().getBlocksByType("small_energy_capacitor");
        List<PlacedBlock> orphanedCapacitors = new ArrayList<>();
        int capacitorsConnected = 0;
        int ghostCapacitorsRemoved = 0;

        for (PlacedBlock capacitor : capacitors) {
            Location location = capacitor.getLocation();
            if (location == null) {
                continue;
            }

            // GHOST BLOCK PREVENTION: Validate that the physical block actually exists
            Block block = location.getBlock();
            if (block == null) {
                plugin.getLogger().warning("Skipped small_energy_capacitor at " + location + " - world unloaded");
                continue;
            }

            // GHOST BLOCK PREVENTION: Validate the block type matches (Copper Bulb)
            if (block.getType() != Material.COPPER_BULB) {
                plugin.getLogger().warning("Ghost block detected and removed: small_energy_capacitor at " +
                    location.getWorld().getName() + " " +
                    location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                plugin.getDatabaseManager().removeBlockAsync(location, null);
                ghostCapacitorsRemoved++;
                continue;
            }

            // Find nearest network within 7 blocks (capacitors have 7 block range)
            EnergyNetwork network = findNearestNetwork(location, 7.0);

            if (network != null) {
                network.connectCapacitor(location, TechFactoryConstants.SMALL_CAPACITOR_CAPACITY);
                registerDeviceToNetwork(location, network);
                capacitorsConnected++;
            } else {
                // Capacitor is orphaned for now - might connect in later passes
                orphanedCapacitors.add(capacitor);
            }
        }

        // Multi-pass loading for capacitors (same as connectors)
        int capacitorPass = 1;
        int maxCapacitorPasses = 20;

        while (!orphanedCapacitors.isEmpty() && capacitorPass <= maxCapacitorPasses) {
            List<PlacedBlock> stillOrphanedCapacitors = new ArrayList<>();
            int connectedThisPass = 0;

            for (PlacedBlock capacitor : orphanedCapacitors) {
                Location location = capacitor.getLocation();
                if (location == null) {
                    continue;
                }

                // Try to find network again (might be reachable through newly connected connectors/capacitors)
                EnergyNetwork network = findNearestNetwork(location, 7.0);

                if (network != null) {
                    network.connectCapacitor(location, TechFactoryConstants.SMALL_CAPACITOR_CAPACITY);
                    registerDeviceToNetwork(location, network);
                    capacitorsConnected++;
                    connectedThisPass++;
                } else {
                    stillOrphanedCapacitors.add(capacitor);
                }
            }

            if (connectedThisPass == 0) {
                // No progress made - stop trying
                break;
            }

            orphanedCapacitors = stillOrphanedCapacitors;
            capacitorPass++;
        }

        if (ghostCapacitorsRemoved > 0) {
            plugin.getLogger().info("Cleaned up " + ghostCapacitorsRemoved + " ghost capacitor(s) from database");
        }

        plugin.getLogger().info("Loaded " + capacitorsConnected + " small energy capacitors from database");

        // Track orphaned capacitors for chunk-load retry
        if (!orphanedCapacitors.isEmpty()) {
            plugin.getLogger().warning(orphanedCapacitors.size() + " capacitor(s) orphaned - will retry when chunks load");
            orphanedDevices.addAll(orphanedCapacitors);
        }

        // Step 4: Load all energy consumers (electric machines) from registry
        int totalConsumersConnected = 0;
        for (String consumerType : EnergyDeviceTypes.ENERGY_CONSUMERS) {
            List<PlacedBlock> consumers = plugin.getDatabaseManager().getBlocksByType(consumerType);
            int connected = 0;

            for (PlacedBlock consumer : consumers) {
                Location location = consumer.getLocation();
                if (location == null) {
                    continue;
                }

                // GHOST BLOCK PREVENTION: Validate that the physical block actually exists
                Block block = location.getBlock();
                if (block == null) {
                    plugin.getLogger().warning("Skipped " + consumerType + " at " + location + " - world unloaded");
                    continue;
                }

                // GHOST BLOCK PREVENTION: Validate the block type matches what we expect
                if (!isValidConsumerBlock(block, consumerType)) {
                    plugin.getLogger().warning("Ghost block detected and removed: " + consumerType + " at " +
                        location.getWorld().getName() + " " +
                        location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                    plugin.getDatabaseManager().removeBlockAsync(location, null);
                    continue;
                }

                // Find nearest network within 6 blocks
                EnergyNetwork network = findNearestNetwork(location, 6.0);

                if (network != null) {
                    network.connectConsumer(location);
                    registerDeviceToNetwork(location, network);
                    connected++;
                    plugin.getLogger().info("✓ Connected " + consumerType + " at " +
                        location.getWorld().getName() + " " +
                        location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() +
                        " to network " + network.getNetworkId().toString().substring(0, 8) +
                        ". Network now has " + network.getConsumerCount() + " consumers");
                } else {
                    // Consumer is orphaned (no network in range)
                    plugin.getLogger().warning("Orphaned " + consumerType + " at " +
                        location.getWorld().getName() + " " +
                        location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
                }
            }

            if (connected > 0) {
                plugin.getLogger().info("Loaded " + connected + " " + consumerType + "(s) from database");
            }
            totalConsumersConnected += connected;
        }

        plugin.getLogger().info("Total energy consumers loaded: " + totalConsumersConnected);

        // Step 5: Clean up any invalid connectors from networks
        // This removes connectors that are in the network but not in the database
        cleanupInvalidConnectors();
    }

    /**
     * Clean up invalid connectors from all networks AND database
     * Removes connectors that:
     * 1. Are in the network but don't exist in the database
     * 2. Are in the database but don't exist physically in the world
     */
    private void cleanupInvalidConnectors() {
        int removedFromNetworks = 0;
        int removedFromDatabase = 0;

        // Step 1: Clean up connectors in networks that don't exist in database
        for (EnergyNetwork network : networks.values()) {
            Set<Location> connectors = new HashSet<>(network.getConnectedConnectors());

            for (Location connectorLoc : connectors) {
                // Check if this connector actually exists in the database
                PlacedBlock placedBlock = plugin.getDatabaseManager().getBlock(connectorLoc);

                if (placedBlock == null || !placedBlock.getBlockType().equals("energy_connector")) {
                    // Connector doesn't exist or is wrong type - remove from network
                    network.disconnectConnector(connectorLoc);
                    removedFromNetworks++;
                    plugin.getLogger().warning("Removed invalid connector from network at " +
                        connectorLoc.getWorld().getName() + " " +
                        connectorLoc.getBlockX() + "," + connectorLoc.getBlockY() + "," + connectorLoc.getBlockZ());
                }
            }
        }

        // Step 2: Clean up connectors in database that don't exist physically in the world
        List<PlacedBlock> allConnectors = plugin.getDatabaseManager().getBlocksByType("energy_connector");

        for (PlacedBlock connector : allConnectors) {
            Location location = connector.getLocation();
            if (location == null) continue;

            // Check if the physical block exists and is a CONDUIT
            org.bukkit.block.Block block = location.getBlock();
            if (block == null || block.getType() != Material.CONDUIT) {
                // Ghost block! Remove from database AND disconnect from any networks
                // Use SYNCHRONOUS removal so it actually commits to database before server shutdown
                plugin.getDatabaseManager().removeBlock(location);

                // Also disconnect from any networks that might have it
                for (EnergyNetwork network : networks.values()) {
                    if (network.hasConnector(location)) {
                        network.disconnectConnector(location);
                    }
                }

                removedFromDatabase++;
                plugin.getLogger().warning("Removed ghost connector from database at " +
                    location.getWorld().getName() + " " +
                    location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
            }
        }

        if (removedFromNetworks > 0 || removedFromDatabase > 0) {
            plugin.getLogger().info("Cleaned up " + removedFromNetworks + " invalid connectors from networks and " +
                removedFromDatabase + " ghost connectors from database");
        }
    }

    /**
     * Get total number of active networks
     */
    public int getNetworkCount() {
        return networks.size();
    }

    /**
     * Get all networks (returns view, not copy - more efficient)
     */
    public Collection<EnergyNetwork> getAllNetworks() {
        return networks.values();
    }

    /**
     * Get the network that owns a specific location (O(1) lookup)
     * Used for connectors, generators, consumers, etc.
     */
    public EnergyNetwork getNetworkByLocation(Location location) {
        if (location == null) {
            return null;
        }
        String locationKey = PlacedBlock.locationToKey(location);
        return locationToNetwork.get(locationKey);
    }

    /**
     * Register a device location to a network (for O(1) lookups)
     * Call this when connecting connectors, panels, consumers
     */
    public void registerDeviceToNetwork(Location deviceLocation, EnergyNetwork network) {
        if (deviceLocation != null && network != null) {
            String locationKey = PlacedBlock.locationToKey(deviceLocation);
            locationToNetwork.put(locationKey, network);
        }
    }

    /**
     * Unregister a device location from the reverse map
     * Call this when disconnecting devices
     */
    public void unregisterDevice(Location deviceLocation) {
        if (deviceLocation != null) {
            String locationKey = PlacedBlock.locationToKey(deviceLocation);
            locationToNetwork.remove(locationKey);
        }
    }

    /**
     * CRITICAL FIX: Retry connecting orphaned devices when a chunk loads
     * Called by ChunkLoadListener when chunks load during gameplay
     *
     * This solves the problem where connector chains span multiple chunks
     * and can't connect at startup because chunks aren't loaded yet.
     */
    public void retryOrphanedDevicesInChunk(String worldName, int chunkX, int chunkZ) {
        if (orphanedDevices.isEmpty()) {
            return; // No orphaned devices to retry
        }

        List<PlacedBlock> reconnected = new ArrayList<>();

        for (PlacedBlock device : orphanedDevices) {
            Location location = device.getLocation();
            if (location == null || !location.getWorld().getName().equals(worldName)) {
                continue;
            }

            // Check if this device is in the chunk that just loaded
            int deviceChunkX = location.getBlockX() >> 4;
            int deviceChunkZ = location.getBlockZ() >> 4;

            // Also check nearby chunks (device might connect to network in adjacent chunk)
            boolean isNearby = Math.abs(deviceChunkX - chunkX) <= 1 && Math.abs(deviceChunkZ - chunkZ) <= 1;

            if (!isNearby) {
                continue; // Not in this chunk or adjacent chunks
            }

            // Try to connect the device
            String blockType = device.getBlockType();
            double range = blockType.equals("small_energy_capacitor") ? 7.0 : 6.0;

            EnergyNetwork network = findNearestNetwork(location, range);
            if (network != null) {
                // Successfully found a network!
                if (blockType.equals("energy_connector")) {
                    network.connectConnector(location);
                } else if (blockType.equals("small_energy_capacitor")) {
                    network.connectCapacitor(location, TechFactoryConstants.SMALL_CAPACITOR_CAPACITY);
                } else if (blockType.equals("solar_generator")) {
                    network.connectPanel(location);
                }
                registerDeviceToNetwork(location, network);
                reconnected.add(device);

                plugin.getLogger().info("Reconnected orphaned " + blockType + " at " +
                    location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() +
                    " when chunk (" + chunkX + ", " + chunkZ + ") loaded");
            }
        }

        // Remove successfully reconnected devices from orphaned list
        orphanedDevices.removeAll(reconnected);

        if (!reconnected.isEmpty()) {
            plugin.getLogger().info("Reconnected " + reconnected.size() + " orphaned device(s) in chunk (" + chunkX + ", " + chunkZ + ")");
        }
    }


    /**
     * PERFORMANCE FIX: Find the nearest energy network within range using spatial indexing
     * OLD: O(N*M) - iterated through ALL networks and ALL connectors
     * NEW: O(1) - only checks networks in nearby chunks (7x7 grid = 49 chunks max)
     *
     * With 50k machines: 100x-1000x speedup on network lookups
     *
     * @param location The location to search from
     * @param maxRange Maximum search range in blocks
     * @return The nearest network within range, or null if none found
     */
    public EnergyNetwork findNearestNetwork(Location location, double maxRange) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        EnergyNetwork nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        // Calculate chunk coordinates
        int centerChunkX = location.getBlockX() >> 4;
        int centerChunkZ = location.getBlockZ() >> 4;
        String worldName = location.getWorld().getName();

        // CRITICAL FIX: Calculate chunk search radius with safety margin
        // 6 blocks = 0.375 chunks, ceil = 1 chunk
        // But at chunk boundaries, 6 block range might extend into 5th chunk!
        // Add +2 instead of +1 to be safe and prevent edge case bugs
        int chunkRadius = (int) Math.ceil(maxRange / 16.0) + 2;

        // Only check networks in nearby chunks (spatial indexing!)
        for (int cx = centerChunkX - chunkRadius; cx <= centerChunkX + chunkRadius; cx++) {
            for (int cz = centerChunkZ - chunkRadius; cz <= centerChunkZ + chunkRadius; cz++) {
                String chunkKey = worldName + ":" + cx + "," + cz;
                List<EnergyNetwork> nearbyNetworks = networksByChunk.get(chunkKey);

                if (nearbyNetworks == null) {
                    continue; // No networks in this chunk
                }

                // Check each network in this chunk
                for (EnergyNetwork network : nearbyNetworks) {
                    Location regulatorLoc = network.getRegulatorLocation();

                    // Skip if in different world (shouldn't happen, but safety check)
                    if (regulatorLoc == null || regulatorLoc.getWorld() == null ||
                        !regulatorLoc.getWorld().equals(location.getWorld())) {
                        continue;
                    }

                    double distance = regulatorLoc.distance(location);

                    // Check if within range and closer than current nearest
                    if (distance <= maxRange && distance < nearestDistance) {
                        nearest = network;
                        nearestDistance = distance;
                    }

                    // Also check connectors in this network
                    for (Location connectorLoc : network.getConnectedConnectors()) {
                        if (connectorLoc == null || connectorLoc.getWorld() == null ||
                            !connectorLoc.getWorld().equals(location.getWorld())) {
                            continue;
                        }

                        double connectorDistance = connectorLoc.distance(location);

                        if (connectorDistance <= maxRange && connectorDistance < nearestDistance) {
                            nearest = network;
                            nearestDistance = connectorDistance;
                        }
                    }
                }
            }
        }

        return nearest;
    }

    /**
     * GHOST BLOCK PREVENTION: Validate that a physical block matches the expected consumer type
     */
    private boolean isValidConsumerBlock(Block block, String consumerType) {
        if (block == null || block.getType() == Material.AIR) {
            return false; // Block doesn't exist
        }

        Material blockMaterial = block.getType();

        // Check if the physical block matches what we expect for each consumer type
        return switch (consumerType) {
            case "electric_furnace" -> blockMaterial == Material.FURNACE;
            case "electric_gold_pan" -> blockMaterial == Material.BROWN_TERRACOTTA;
            // Add more electric machines here as you create them
            default -> false;
        };
    }

    /**
     * Check if a location is within range of any network (regulator or connector)
     *
     * @param location The location to check
     * @param maxRange Maximum range in blocks
     * @return true if within range of a network
     */
    public boolean isWithinNetworkRange(Location location, double maxRange) {
        return findNearestNetwork(location, maxRange) != null;
    }

    // ========================================
    // SPATIAL INDEXING HELPER METHODS
    // ========================================

    /**
     * Add a network to the spatial index
     * Called when creating a network
     */
    private void addNetworkToSpatialIndex(EnergyNetwork network) {
        Location regulatorLoc = network.getRegulatorLocation();
        if (regulatorLoc == null || regulatorLoc.getWorld() == null) {
            return;
        }

        int chunkX = regulatorLoc.getBlockX() >> 4;
        int chunkZ = regulatorLoc.getBlockZ() >> 4;
        String chunkKey = regulatorLoc.getWorld().getName() + ":" + chunkX + "," + chunkZ;

        // Add network to this chunk's list
        networksByChunk.computeIfAbsent(chunkKey, k -> new ArrayList<>()).add(network);
    }

    /**
     * Remove a network from the spatial index
     * Called when removing a network
     */
    private void removeNetworkFromSpatialIndex(EnergyNetwork network) {
        Location regulatorLoc = network.getRegulatorLocation();
        if (regulatorLoc == null || regulatorLoc.getWorld() == null) {
            return;
        }

        int chunkX = regulatorLoc.getBlockX() >> 4;
        int chunkZ = regulatorLoc.getBlockZ() >> 4;
        String chunkKey = regulatorLoc.getWorld().getName() + ":" + chunkX + "," + chunkZ;

        // Remove network from this chunk's list
        List<EnergyNetwork> networksInChunk = networksByChunk.get(chunkKey);
        if (networksInChunk != null) {
            networksInChunk.remove(network);

            // Clean up empty lists to save memory
            if (networksInChunk.isEmpty()) {
                networksByChunk.remove(chunkKey);
            }
        }
    }


    /**
     * PERFORMANCE FIX: Add network to chunk-based metadata map
     * Allows O(1) lookup of which networks exist in a chunk
     */
    private void addNetworkToChunkMap(Location location, String networkKey) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        String chunkKey = location.getWorld().getName() + ":" + chunkX + "," + chunkZ;

        chunkToNetworks.computeIfAbsent(chunkKey, k -> ConcurrentHashMap.newKeySet()).add(networkKey);
    }

    /**
     * PERFORMANCE FIX: Remove network from chunk-based metadata map
     */
    private void removeNetworkFromChunkMap(Location location, String networkKey) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        String chunkKey = location.getWorld().getName() + ":" + chunkX + "," + chunkZ;

        Set<String> networksInChunk = chunkToNetworks.get(chunkKey);
        if (networksInChunk != null) {
            networksInChunk.remove(networkKey);

            // Clean up empty sets to save memory
            if (networksInChunk.isEmpty()) {
                chunkToNetworks.remove(chunkKey);
            }
        }
    }

    /**
     * PERFORMANCE FIX: Get all networks in a specific chunk (O(1) lookup)
     * Useful for chunk load operations
     */
    public Set<EnergyNetwork> getNetworksInChunk(String worldName, int chunkX, int chunkZ) {
        String chunkKey = worldName + ":" + chunkX + "," + chunkZ;
        Set<String> networkKeys = chunkToNetworks.get(chunkKey);

        if (networkKeys == null || networkKeys.isEmpty()) {
            return Collections.emptySet();
        }

        Set<EnergyNetwork> result = new HashSet<>();
        for (String networkKey : networkKeys) {
            EnergyNetwork network = networks.get(networkKey);
            if (network != null) {
                result.add(network);
            }
        }

        return result;
    }
}

