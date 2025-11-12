package org.ThefryGuy.techFactory.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.TechFactoryConstants;
import org.ThefryGuy.techFactory.data.DatabaseManager;
import org.ThefryGuy.techFactory.data.PlacedBlock;
import org.ThefryGuy.techFactory.energy.EnergyDeviceTypes;
import org.ThefryGuy.techFactory.energy.EnergyManager;
import org.ThefryGuy.techFactory.energy.EnergyNetwork;
import org.ThefryGuy.techFactory.energy.EnergyRegulator;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

/**
 * Handles placement, breaking, and interaction with energy blocks
 */
public class EnergyBlockListener implements Listener {

    private final TechFactory plugin;
    private final DatabaseManager databaseManager;
    private final EnergyManager energyManager;

    public EnergyBlockListener(TechFactory plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.energyManager = plugin.getEnergyManager();
    }
    
    /**
     * Handle placing energy blocks in the world
     * ASYNC OPTIMIZED: Database save happens in background
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        Block block = event.getBlockPlaced();

        // Check if this is a TechFactory energy item
        String itemId = RecipeItem.getItemId(item);
        if (itemId == null) {
            return; // Not a TechFactory item
        }

        // Handle Energy Regulator placement
        if (itemId.equals("energy_regulator")) {
            handleRegulatorPlacement(player, block, itemId, event);
            return;
        }

        // Handle Energy Connector placement
        if (itemId.equals("energy_connector")) {
            handleConnectorPlacement(player, block, itemId, event);
            return;
        }

        // Handle Solar Generator placement
        if (itemId.equals("solar_generator")) {
            handleSolarGeneratorPlacement(player, block, itemId, event);
            return;
        }

        // Handle Small Energy Capacitor placement
        if (itemId.equals("small_energy_capacitor")) {
            handleCapacitorPlacement(player, block, itemId, event);
            return;
        }
    }

    /**
     * Handle Energy Regulator placement
     * Prevents placing regulators too close to each other (only 1 regulator per network)
     */
    private void handleRegulatorPlacement(Player player, Block block, String itemId, BlockPlaceEvent event) {
        Location location = block.getLocation();

        // GHOST BLOCK PREVENTION: Check if there's a ghost network at this exact location
        // This can happen if the database removal was async and didn't complete
        if (energyManager.hasNetwork(location)) {
            // Ghost network detected! Clean it up
            PlacedBlock placedBlock = databaseManager.getBlock(location);

            // If there's a network but no database entry, it's a ghost
            if (placedBlock == null) {
                player.sendMessage(ChatColor.YELLOW + "Cleaning up ghost network...");
                energyManager.removeNetwork(location);
            }
        }

        // Check if there's already a network within 6 blocks (but not at this exact location)
        // This prevents multiple regulators in the same network (Slimefun-style)
        EnergyNetwork nearbyNetwork = energyManager.findNearestNetwork(location, 6.0);

        if (nearbyNetwork != null) {
            // Check if it's at the exact same location (ghost) or nearby (real conflict)
            Location regulatorLoc = nearbyNetwork.getRegulatorLocation();

            if (regulatorLoc != null && regulatorLoc.equals(location)) {
                // Same location - this is a ghost network, already cleaned up above
                // Allow placement to continue
            } else {
                // Different location - another regulator is too close!
                player.sendMessage(ChatColor.RED + "✗ Cannot place Energy Regulator here!");
                player.sendMessage(ChatColor.GRAY + "Another Energy Regulator is within 6 blocks");
                player.sendMessage(ChatColor.GRAY + "Only one regulator per network is allowed");

                // Cancel the event - this prevents the block from being placed
                // and automatically returns the item to the player
                event.setCancelled(true);

                return;
            }
        }

        // Create a PlacedBlock entry
        PlacedBlock placedBlock = new PlacedBlock(
            location,
            itemId,
            player.getUniqueId()
        );

        // Create energy network immediately (instant feedback)
        EnergyNetwork network = energyManager.createNetwork(location);

        // AUTO-RECONNECT: Scan for nearby orphaned connectors and generators
        int reconnectedConnectors = 0;
        int reconnectedGenerators = 0;

        if (network != null) {
            // PART 1: Scan all existing networks for devices within 6 blocks (steal from other networks)
            for (EnergyNetwork otherNetwork : energyManager.getAllNetworks()) {
                // Skip the network we just created
                if (otherNetwork.equals(network)) {
                    continue;
                }

                // Check connectors from other networks
                for (Location connectorLoc : otherNetwork.getConnectedConnectors()) {
                    if (connectorLoc != null && connectorLoc.getWorld() != null &&
                        connectorLoc.getWorld().equals(location.getWorld())) {
                        double distance = connectorLoc.distance(location);
                        if (distance <= 6.0) {
                            // This connector is closer to the new regulator - reconnect it
                            otherNetwork.disconnectConnector(connectorLoc);
                            energyManager.unregisterDevice(connectorLoc);
                            network.connectConnector(connectorLoc);
                            energyManager.registerDeviceToNetwork(connectorLoc, network);
                            reconnectedConnectors++;
                        }
                    }
                }

                // Check generators from other networks
                for (Location generatorLoc : otherNetwork.getConnectedPanels()) {
                    if (generatorLoc != null && generatorLoc.getWorld() != null &&
                        generatorLoc.getWorld().equals(location.getWorld())) {
                        double distance = generatorLoc.distance(location);
                        if (distance <= 6.0) {
                            // This generator is closer to the new regulator - reconnect it
                            otherNetwork.disconnectPanel(generatorLoc);
                            energyManager.unregisterDevice(generatorLoc);
                            network.connectPanel(generatorLoc);
                            energyManager.registerDeviceToNetwork(generatorLoc, network);
                            reconnectedGenerators++;
                        }
                    }
                }
            }

            // PART 2: Scan for truly orphaned devices (not in any network) - ASYNC to avoid lag
            // This handles devices left behind when a regulator was removed
            // Run asynchronously to avoid blocking the main thread
            final Location finalLocation = location;
            final EnergyNetwork finalNetwork = network;

            org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    int asyncReconnectedConnectors = 0;
                    int asyncReconnectedGenerators = 0;

                    // Query database for all connectors (async - won't block server)
                    java.util.List<org.ThefryGuy.techFactory.data.PlacedBlock> allConnectors =
                        databaseManager.getBlocksByType("energy_connector");

                    for (org.ThefryGuy.techFactory.data.PlacedBlock connectorBlock : allConnectors) {
                        Location connectorLoc = connectorBlock.getLocation();
                        if (connectorLoc == null || !connectorLoc.getWorld().equals(finalLocation.getWorld())) {
                            continue;
                        }

                        // Check if this connector is already in a network
                        if (energyManager.getNetworkByLocation(connectorLoc) != null) {
                            continue; // Already connected, skip
                        }

                        // Check distance
                        double distance = connectorLoc.distance(finalLocation);
                        if (distance <= 6.0) {
                            // Orphaned connector found - connect it on main thread!
                            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                                finalNetwork.connectConnector(connectorLoc);
                                energyManager.registerDeviceToNetwork(connectorLoc, finalNetwork);
                            });
                            asyncReconnectedConnectors++;
                        }
                    }

                    // Same for generators
                    java.util.List<org.ThefryGuy.techFactory.data.PlacedBlock> allGenerators =
                        databaseManager.getBlocksByType("solar_generator");

                    for (org.ThefryGuy.techFactory.data.PlacedBlock generatorBlock : allGenerators) {
                        Location generatorLoc = generatorBlock.getLocation();
                        if (generatorLoc == null || !generatorLoc.getWorld().equals(finalLocation.getWorld())) {
                            continue;
                        }

                        // Check if this generator is already in a network
                        if (energyManager.getNetworkByLocation(generatorLoc) != null) {
                            continue; // Already connected, skip
                        }

                        // Check distance
                        double distance = generatorLoc.distance(finalLocation);
                        if (distance <= 6.0) {
                            // Orphaned generator found - connect it on main thread!
                            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                                finalNetwork.connectPanel(generatorLoc);
                                energyManager.registerDeviceToNetwork(generatorLoc, finalNetwork);
                            });
                            asyncReconnectedGenerators++;
                        }
                    }

                    // Log results if any orphaned devices were found
                    if (asyncReconnectedConnectors > 0 || asyncReconnectedGenerators > 0) {
                        plugin.getLogger().info("Reconnected " + asyncReconnectedConnectors + " orphaned connector(s) and " +
                            asyncReconnectedGenerators + " orphaned generator(s) to new regulator");
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error reconnecting orphaned devices", e);
                }
            });
        }

        // Send success messages immediately
        player.sendMessage(ChatColor.GREEN + "Energy Regulator placed successfully!");
        player.sendMessage(ChatColor.GRAY + "Right-click to configure");
        player.sendMessage(ChatColor.AQUA + "Energy network created!");

        // Show reconnection info
        if (reconnectedConnectors > 0) {
            player.sendMessage(ChatColor.GREEN + "✓ Reconnected " + reconnectedConnectors + " energy connector(s)");
        }
        if (reconnectedGenerators > 0) {
            player.sendMessage(ChatColor.GREEN + "✓ Reconnected " + reconnectedGenerators + " solar generator(s)");
        }

        // Save to database asynchronously (won't block server)
        databaseManager.saveBlockAsync(placedBlock, null);
    }

    /**
     * Handle Energy Connector placement
     * Connectors extend the network range by 6 blocks
     */
    private void handleConnectorPlacement(Player player, Block block, String itemId, BlockPlaceEvent event) {
        Location location = block.getLocation();

        // Find nearest network within 6 blocks
        EnergyNetwork network = energyManager.findNearestNetwork(location, 6.0);

        // Create a PlacedBlock entry (always save, even if not connected)
        PlacedBlock placedBlock = new PlacedBlock(
            location,
            itemId,
            player.getUniqueId()
        );

        if (network == null) {
            // No network in range - allow placement but show warning
            player.sendMessage(ChatColor.YELLOW + "Energy Connector placed!");
            player.sendMessage(ChatColor.RED + "⚠ Not connected to any network!");
            player.sendMessage(ChatColor.GRAY + "Place within 6 blocks of an Energy Regulator or Connector");
        } else {
            // Connect to the network
            network.connectConnector(location);
            energyManager.registerDeviceToNetwork(location, network);

            // Visual feedback: Spawn particles showing connection
            spawnConnectionParticles(location, network.getRegulatorLocation());

            // Audio feedback: Play connection sound
            if (location.getWorld() != null) {
                location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f);
            }

            // Send success messages
            player.sendMessage(ChatColor.GREEN + "Energy Connector placed successfully!");
            player.sendMessage(ChatColor.AQUA + "Connected to energy network!");
            player.sendMessage(ChatColor.GRAY + "Range extended by 6 blocks");
        }

        // Save to database asynchronously
        databaseManager.saveBlockAsync(placedBlock, null);
    }

    /**
     * Handle Solar Generator placement
     * Solar Generators connect to nearby networks and generate power
     */
    private void handleSolarGeneratorPlacement(Player player, Block block, String itemId, BlockPlaceEvent event) {
        Location location = block.getLocation();

        // Find nearest network within 6 blocks
        EnergyNetwork network = energyManager.findNearestNetwork(location, 6.0);

        // Create a PlacedBlock entry (always save, even if not connected)
        PlacedBlock placedBlock = new PlacedBlock(
            location,
            itemId,
            player.getUniqueId()
        );

        if (network == null) {
            // No network in range - allow placement but show warning
            player.sendMessage(ChatColor.YELLOW + "Solar Generator placed!");
            player.sendMessage(ChatColor.RED + "⚠ Not connected to any network!");
            player.sendMessage(ChatColor.GRAY + "Place within 6 blocks of an Energy Regulator or Connector");
        } else {
            // Connect to the network as a panel (generator)
            network.connectPanel(location);
            energyManager.registerDeviceToNetwork(location, network);

            // Visual feedback: Spawn particles showing connection
            spawnConnectionParticles(location, network.getRegulatorLocation());

            // Audio feedback: Play connection sound
            if (location.getWorld() != null) {
                location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f);
            }

            // Send success messages
            player.sendMessage(ChatColor.GREEN + "✓ Solar Generator placed!");
            player.sendMessage(ChatColor.AQUA + "✓ Successfully connected to energy network!");
            player.sendMessage(ChatColor.GRAY + "Generating: 4 J/s (when in sunlight)");
        }

        // Save to database asynchronously
        databaseManager.saveBlockAsync(placedBlock, null);
    }

    /**
     * Handle Small Energy Capacitor placement
     * Capacitors store energy and extend network range by 7 blocks
     */
    private void handleCapacitorPlacement(Player player, Block block, String itemId, BlockPlaceEvent event) {
        Location location = block.getLocation();

        // Find nearest network within 7 blocks
        EnergyNetwork network = energyManager.findNearestNetwork(location, 7.0);

        // Create a PlacedBlock entry (always save, even if not connected)
        PlacedBlock placedBlock = new PlacedBlock(
            location,
            itemId,
            player.getUniqueId()
        );

        if (network == null) {
            // No network in range - allow placement but show warning
            player.sendMessage(ChatColor.YELLOW + "Small Energy Capacitor placed!");
            player.sendMessage(ChatColor.RED + "⚠ Not connected to any network!");
            player.sendMessage(ChatColor.GRAY + "Place within 7 blocks of an Energy Regulator, Connector, or Machine");
        } else {
            // Connect to the network as a capacitor (extends range AND adds capacity)
            network.connectCapacitor(location, TechFactoryConstants.SMALL_CAPACITOR_CAPACITY);
            energyManager.registerDeviceToNetwork(location, network);

            // Visual feedback: Spawn particles showing connection
            spawnConnectionParticles(location, network.getRegulatorLocation());

            // Audio feedback: Play connection sound
            if (location.getWorld() != null) {
                location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f);
            }

            player.sendMessage(ChatColor.GREEN + "✓ Small Energy Capacitor placed and connected!");
            player.sendMessage(ChatColor.GRAY + "Capacity: +128 J | Range: 7 blocks");
            player.sendMessage(ChatColor.AQUA + "Network capacity: " + network.getMaxCapacity() + " J");
        }

        // Save to database asynchronously
        databaseManager.saveBlockAsync(placedBlock, null);
    }

    /**
     * Spawn particles showing connection between connector and network
     */
    private void spawnConnectionParticles(Location connectorLoc, Location networkLoc) {
        if (connectorLoc.getWorld() == null || networkLoc.getWorld() == null) {
            return;
        }

        // Spawn particles at connector location
        connectorLoc.getWorld().spawnParticle(
            Particle.ELECTRIC_SPARK,
            connectorLoc.clone().add(0.5, 0.5, 0.5),
            20,  // count
            0.3, // offsetX
            0.3, // offsetY
            0.3, // offsetZ
            0.1  // speed
        );

        // Spawn particles at network location
        networkLoc.getWorld().spawnParticle(
            Particle.ELECTRIC_SPARK,
            networkLoc.clone().add(0.5, 0.5, 0.5),
            20,
            0.3,
            0.3,
            0.3,
            0.1
        );
    }
    
    /**
     * Handle breaking energy blocks
     * ASYNC OPTIMIZED: Database removal happens in background
     */
    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        // Check if this location has a placed energy block
        PlacedBlock placedBlock = databaseManager.getBlock(location);
        if (placedBlock == null) {
            return; // Not an energy block
        }

        String blockType = placedBlock.getBlockType();

        // Handle Energy Regulator breaking
        if (blockType != null && blockType.equals("energy_regulator")) {
            handleRegulatorBreak(player, block, location, event);
            return;
        }

        // Handle Energy Connector breaking
        if (blockType != null && blockType.equals("energy_connector")) {
            handleConnectorBreak(player, block, location, event);
            return;
        }

        // Handle Solar Generator breaking
        if (blockType != null && blockType.equals("solar_generator")) {
            handleSolarGeneratorBreak(player, block, location, event);
            return;
        }

        // Handle Small Energy Capacitor breaking
        if (blockType != null && blockType.equals("small_energy_capacitor")) {
            handleCapacitorBreak(player, block, location, event);
            return;
        }
    }

    /**
     * Handle Energy Regulator breaking
     */
    private void handleRegulatorBreak(Player player, Block block, Location location, BlockBreakEvent event) {
        // Cancel default drops FIRST (before anything else)
        event.setDropItems(false);

        // Remove energy network immediately (instant feedback)
        energyManager.removeNetwork(location);

        // Create and drop the energy regulator item
        RecipeItem energyRegulator = new org.ThefryGuy.techFactory.recipes.energy.EnergyRegulator();
        ItemStack drop = energyRegulator.getItemStack();

        // Drop our custom item
        if (block.getWorld() != null) {
            block.getWorld().dropItemNaturally(location, drop);
        } else {
            // Extremely unlikely, but defensive programming
            player.sendMessage(ChatColor.RED + "Error: Could not drop item (world unloaded)");
        }

        // Send messages immediately
        player.sendMessage(ChatColor.YELLOW + "Energy Regulator removed!");
        player.sendMessage(ChatColor.GRAY + "Energy network destroyed");

        // Remove from database asynchronously (won't block server)
        databaseManager.removeBlockAsync(location, null);
    }

    /**
     * Handle Energy Connector breaking
     */
    private void handleConnectorBreak(Player player, Block block, Location location, BlockBreakEvent event) {
        // Cancel default drops FIRST (before anything else)
        event.setDropItems(false);

        // Find the network this connector belongs to (O(1) lookup)
        EnergyNetwork network = energyManager.getNetworkByLocation(location);
        if (network != null) {
            network.disconnectConnector(location);
            energyManager.unregisterDevice(location);

            // CRITICAL: Re-validate all remaining connectors to check if they're still reachable
            // This prevents "chain" connectors from staying connected when a middle connector is removed
            java.util.Set<Location> orphanedConnectors = network.validateConnectorConnectivity(6.0);

            // Unregister orphaned connectors from the reverse map
            for (Location orphaned : orphanedConnectors) {
                energyManager.unregisterDevice(orphaned);
            }

            // Notify player if other connectors were orphaned
            if (!orphanedConnectors.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "⚠ " + orphanedConnectors.size() + " connector(s) orphaned (out of range)");
            }
        }

        // Create and drop the energy connector item (only 1, not 8!)
        RecipeItem energyConnector = new org.ThefryGuy.techFactory.recipes.energy.EnergyConnector();
        ItemStack drop = energyConnector.getItemStack();
        drop.setAmount(1); // Override the amount to 1 (getItemStack() returns 8 for crafting)

        // Drop our custom item
        if (block.getWorld() != null) {
            block.getWorld().dropItemNaturally(location, drop);
        } else {
            player.sendMessage(ChatColor.RED + "Error: Could not drop item (world unloaded)");
        }

        // Send messages
        player.sendMessage(ChatColor.YELLOW + "Energy Connector removed!");
        player.sendMessage(ChatColor.GRAY + "Disconnected from network");

        // Remove from database asynchronously
        databaseManager.removeBlockAsync(location, null);
    }

    /**
     * Handle Solar Generator breaking
     */
    private void handleSolarGeneratorBreak(Player player, Block block, Location location, BlockBreakEvent event) {
        // Cancel default drops FIRST (before anything else)
        event.setDropItems(false);

        // Find the network this generator belongs to and disconnect (O(1) lookup)
        EnergyNetwork network = energyManager.getNetworkByLocation(location);
        if (network != null) {
            network.disconnectPanel(location);
            energyManager.unregisterDevice(location);
        }

        // Create and drop the solar generator item
        RecipeItem solarGenerator = new org.ThefryGuy.techFactory.recipes.energy.SolarGenerator();
        ItemStack drop = solarGenerator.getItemStack();

        // Drop our custom item
        if (block.getWorld() != null) {
            block.getWorld().dropItemNaturally(location, drop);
        } else {
            player.sendMessage(ChatColor.RED + "Error: Could not drop item (world unloaded)");
        }

        // Send messages
        player.sendMessage(ChatColor.YELLOW + "Solar Generator removed!");
        player.sendMessage(ChatColor.GRAY + "Disconnected from network");

        // Remove from database asynchronously
        databaseManager.removeBlockAsync(location, null);
    }

    /**
     * Handle Small Energy Capacitor breaking
     */
    private void handleCapacitorBreak(Player player, Block block, Location location, BlockBreakEvent event) {
        // Cancel default drops FIRST (before anything else)
        event.setDropItems(false);

        // Find the network this capacitor belongs to (O(1) lookup)
        EnergyNetwork network = energyManager.getNetworkByLocation(location);
        if (network != null) {
            // Disconnect from network (remove capacity bonus)
            network.disconnectCapacitor(location, TechFactoryConstants.SMALL_CAPACITOR_CAPACITY);
            energyManager.unregisterDevice(location);

            player.sendMessage(ChatColor.YELLOW + "Small Energy Capacitor removed!");
            player.sendMessage(ChatColor.GRAY + "Disconnected from energy network");
            player.sendMessage(ChatColor.AQUA + "Network capacity: " + network.getMaxCapacity() + " J");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Small Energy Capacitor removed!");
        }

        // Create and drop the capacitor item
        RecipeItem capacitor = new org.ThefryGuy.techFactory.recipes.energy.SmallEnergyCapacitor();
        ItemStack drop = capacitor.getItemStack();

        // Drop our custom item
        if (block.getWorld() != null) {
            block.getWorld().dropItemNaturally(location, drop);
        } else {
            player.sendMessage(ChatColor.RED + "Error: Could not drop item (world unloaded)");
        }

        // Remove from database asynchronously (won't block server)
        databaseManager.removeBlockAsync(location, null);
    }

    /**
     * Handle right-clicking energy blocks to open GUI
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if player right-clicked a block
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        
        Location location = block.getLocation();
        Player player = event.getPlayer();
        
        // Check if this location has a placed energy block
        PlacedBlock placedBlock = databaseManager.getBlock(location);
        if (placedBlock == null) {
            return; // Not an energy block
        }

        // GHOST BLOCK PREVENTION: Validate the physical block still exists
        if (!isValidEnergyBlock(block, placedBlock.getBlockType())) {
            // Ghost block detected! Clean it up
            player.sendMessage(ChatColor.RED + "Ghost block detected and removed!");
            cleanupGhostBlock(location, placedBlock.getBlockType());
            return;
        }

        // Check block type
        String blockType = placedBlock.getBlockType();

        // Energy consumers (electric machines) are handled by their own listeners, not here
        if (blockType != null && EnergyDeviceTypes.isConsumer(blockType)) {
            return; // Let the machine's listener handle it
        }

        // Cancel the event to prevent other interactions (for energy blocks only)
        event.setCancelled(true);

        // Handle Energy Regulator - open GUI
        if (blockType != null && blockType.equals("energy_regulator")) {
            openEnergyRegulatorGUI(player, placedBlock);
            return;
        }

        // Handle Energy Connector - show connection status
        if (blockType != null && blockType.equals("energy_connector")) {
            showConnectorStatus(player, location);
            return;
        }

        // Handle Solar Generator - show generation status
        if (blockType != null && blockType.equals("solar_generator")) {
            showSolarGeneratorStatus(player, location);
            return;
        }

        // Handle Small Energy Capacitor - show connection status
        if (blockType != null && blockType.equals("small_energy_capacitor")) {
            showCapacitorStatus(player, location);
            return;
        }
    }
    
    /**
     * Open the Energy Regulator GUI
     */
    private void openEnergyRegulatorGUI(Player player, PlacedBlock placedBlock) {
        // Get the energy network
        EnergyNetwork network = energyManager.getNetwork(placedBlock.getLocation());

        if (network == null) {
            player.sendMessage(ChatColor.RED + "Error: Energy network not found!");
            return;
        }

        // OPTIMIZATION: Force-update hologram to show real-time data when GUI is opened
        // This ensures the player sees accurate energy values even though holograms only update every 3 seconds
        energyManager.updateHologramNow(placedBlock.getLocation());

        // Use the EnergyRegulator class to open the GUI
        Block block = placedBlock.getLocation().getBlock();
        EnergyRegulator.openGUI(player, block, network);
    }

    /**
     * Show Energy Connector connection status
     */
    private void showConnectorStatus(Player player, Location connectorLocation) {
        // PERFORMANCE: O(1) lookup using reverse map instead of O(n) iteration
        EnergyNetwork connectedNetwork = energyManager.getNetworkByLocation(connectorLocation);

        // Display status
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage(ChatColor.YELLOW + "⚡ Energy Connector Status");
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (connectedNetwork != null) {
            // Connected - show network info
            Location regulatorLoc = connectedNetwork.getRegulatorLocation();
            double distance = connectorLocation.distance(regulatorLoc);

            player.sendMessage(ChatColor.GREEN + "✓ Connected to Energy Network");
            player.sendMessage(ChatColor.GRAY + "Network ID: " + ChatColor.WHITE + connectedNetwork.getNetworkId().toString().substring(0, 8) + "...");
            player.sendMessage(ChatColor.GRAY + "Regulator Location: " + ChatColor.WHITE +
                regulatorLoc.getBlockX() + ", " + regulatorLoc.getBlockY() + ", " + regulatorLoc.getBlockZ());
            player.sendMessage(ChatColor.GRAY + "Distance: " + ChatColor.WHITE + String.format("%.1f", distance) + " blocks");
            player.sendMessage(ChatColor.GRAY + "Energy: " + ChatColor.AQUA + connectedNetwork.getStoredEnergy() + "/" + connectedNetwork.getMaxCapacity() + " J");
            player.sendMessage(ChatColor.GRAY + "Network Size: " + ChatColor.WHITE +
                connectedNetwork.getConnectorCount() + " connectors, " +
                connectedNetwork.getPanelCount() + " panels, " +
                connectedNetwork.getConsumerCount() + " consumers");
        } else {
            // Not connected - check for nearby networks
            EnergyNetwork nearestNetwork = energyManager.findNearestNetwork(connectorLocation, 6.0);

            if (nearestNetwork != null) {
                player.sendMessage(ChatColor.YELLOW + "⚠ Network found but not connected!");
                player.sendMessage(ChatColor.GRAY + "Try breaking and replacing this connector");
            } else {
                player.sendMessage(ChatColor.RED + "✗ Not connected to any network");
                player.sendMessage(ChatColor.GRAY + "No energy network within 6 blocks");
                player.sendMessage(ChatColor.GRAY + "Place an Energy Regulator or Connector nearby");
            }
        }

        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * Show Solar Generator status
     */
    private void showSolarGeneratorStatus(Player player, Location generatorLocation) {
        // Check if this generator is connected to any network
        EnergyNetwork connectedNetwork = null;

        // PERFORMANCE: O(1) lookup using reverse map instead of O(n) iteration
        connectedNetwork = energyManager.getNetworkByLocation(generatorLocation);

        // Check if generator has direct sunlight access
        Block block = generatorLocation.getBlock();
        boolean hasSunlight = false;
        if (block.getWorld() != null) {
            // Check if it's daytime and has sky access
            long time = block.getWorld().getTime();
            boolean isDaytime = time < 12300 || time > 23850; // Minecraft day cycle
            boolean hasSkyAccess = block.getLightFromSky() == 15;
            hasSunlight = isDaytime && hasSkyAccess;
        }

        // Display status
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage(ChatColor.YELLOW + "☀ Solar Generator Status");
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (connectedNetwork != null) {
            player.sendMessage(ChatColor.GREEN + "✓ Connected to energy network");
            player.sendMessage(ChatColor.GRAY + "Network: " +
                connectedNetwork.getConnectorCount() + " connectors, " +
                connectedNetwork.getPanelCount() + " panels, " +
                connectedNetwork.getConsumerCount() + " consumers");

            // Show generation status
            if (hasSunlight) {
                player.sendMessage(ChatColor.AQUA + "⚡ Generating: 4 J/s");
                player.sendMessage(ChatColor.GREEN + "☀ Has direct sunlight access");
            } else {
                player.sendMessage(ChatColor.RED + "⚡ Not generating (no sunlight)");
                player.sendMessage(ChatColor.GRAY + "Needs direct sky access during daytime");
            }
        } else {
            // Not connected - check for nearby networks
            EnergyNetwork nearestNetwork = energyManager.findNearestNetwork(generatorLocation, 6.0);

            if (nearestNetwork != null) {
                player.sendMessage(ChatColor.YELLOW + "⚠ Network found but not connected!");
                player.sendMessage(ChatColor.GRAY + "Try breaking and replacing this generator");
            } else {
                player.sendMessage(ChatColor.RED + "✗ Not connected to any network");
                player.sendMessage(ChatColor.GRAY + "No energy network within 6 blocks");
                player.sendMessage(ChatColor.GRAY + "Place an Energy Regulator or Connector nearby");
            }
        }

        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * Show Small Energy Capacitor status
     */
    private void showCapacitorStatus(Player player, Location capacitorLocation) {
        // Check if this capacitor is connected to any network (O(1) lookup)
        EnergyNetwork connectedNetwork = energyManager.getNetworkByLocation(capacitorLocation);

        // Display status
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage(ChatColor.AQUA + "⚡ Small Energy Capacitor");
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        if (connectedNetwork != null) {
            player.sendMessage(ChatColor.GREEN + "✓ Connected to energy network");
            player.sendMessage(ChatColor.GRAY + "Capacity Bonus: " + ChatColor.AQUA + "+128 J");
            player.sendMessage(ChatColor.GRAY + "Connection Range: " + ChatColor.AQUA + "7 blocks");
            player.sendMessage("");
            player.sendMessage(ChatColor.GRAY + "Network Stats:");
            player.sendMessage(ChatColor.AQUA + "  Energy: " + connectedNetwork.getStoredEnergy() + "/" + connectedNetwork.getMaxCapacity() + " J");
            player.sendMessage(ChatColor.GRAY + "  Connectors: " + connectedNetwork.getConnectorCount());
            player.sendMessage(ChatColor.GRAY + "  Capacitors: " + connectedNetwork.getCapacitorCount());
            player.sendMessage(ChatColor.GRAY + "  Generators: " + connectedNetwork.getPanelCount());
            player.sendMessage(ChatColor.GRAY + "  Consumers: " + connectedNetwork.getConsumerCount());
        } else {
            // Not connected - check for nearby networks
            EnergyNetwork nearestNetwork = energyManager.findNearestNetwork(capacitorLocation, 7.0);

            if (nearestNetwork != null) {
                player.sendMessage(ChatColor.YELLOW + "⚠ Network found but not connected!");
                player.sendMessage(ChatColor.GRAY + "Try breaking and replacing this capacitor");
            } else {
                player.sendMessage(ChatColor.RED + "✗ Not connected to any network");
                player.sendMessage(ChatColor.GRAY + "No energy network within 7 blocks");
                player.sendMessage(ChatColor.GRAY + "Place an Energy Regulator, Connector, or Machine nearby");
            }
        }

        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * Prevent players from taking items out of the Energy Regulator GUI
     * The GUI is display-only (no interaction allowed)
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // Check if this is an Energy Regulator GUI
        if (!title.equals(ChatColor.AQUA + "⚡ Energy Regulator")) {
            return; // Not our GUI
        }

        // Cancel ALL clicks to prevent taking items
        event.setCancelled(true);
    }

    /**
     * GHOST BLOCK PREVENTION: Validate that a physical block matches the expected energy block type
     */
    private boolean isValidEnergyBlock(Block block, String expectedType) {
        if (block == null || block.getType() == Material.AIR) {
            return false; // Block doesn't exist
        }

        Material blockMaterial = block.getType();

        // Check if the physical block matches what we expect
        if (expectedType.equals("energy_regulator")) {
            return blockMaterial == Material.LIGHTNING_ROD; // Energy Regulator uses LIGHTNING_ROD
        }

        if (expectedType.equals("energy_connector")) {
            return blockMaterial == Material.CONDUIT; // Energy Connector uses CONDUIT
        }

        if (expectedType.equals("solar_generator")) {
            return blockMaterial == Material.DAYLIGHT_DETECTOR; // Solar Generator uses DAYLIGHT_DETECTOR
        }

        if (expectedType.equals("small_energy_capacitor")) {
            return blockMaterial == Material.COPPER_BULB; // Small Energy Capacitor uses COPPER_BULB
        }

        if (expectedType.equals("electric_furnace")) {
            return blockMaterial == Material.FURNACE; // Electric Furnace uses FURNACE
        }

        if (expectedType.equals("electric_gold_pan")) {
            return blockMaterial == Material.BROWN_TERRACOTTA; // Electric Gold Pan uses BROWN_TERRACOTTA
        }

        return false;
    }

    /**
     * GHOST BLOCK PREVENTION: Clean up a ghost block from database and memory
     */
    private void cleanupGhostBlock(Location location, String blockType) {
        plugin.getLogger().warning("Ghost block detected and cleaned up: " + blockType + " at " +
            location.getWorld().getName() + " " +
            location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());

        // Clean up based on type
        if (blockType.equals("energy_regulator")) {
            energyManager.removeNetwork(location);
        } else if (blockType.equals("energy_connector") || blockType.equals("solar_generator") || blockType.equals("small_energy_capacitor")) {
            // PERFORMANCE: O(1) lookup using reverse map instead of O(n) iteration
            EnergyNetwork network = energyManager.getNetworkByLocation(location);
            if (network != null) {
                if (blockType.equals("energy_connector") || blockType.equals("small_energy_capacitor")) {
                    network.disconnectConnector(location);  // Capacitors act like connectors
                } else {
                    network.disconnectPanel(location);
                }
                energyManager.unregisterDevice(location);
            }
        }

        // Remove from database
        databaseManager.removeBlockAsync(location, null);
    }
}

