package org.ThefryGuy.techFactory.machines.electric;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.TechFactoryConstants;
import org.ThefryGuy.techFactory.data.PlacedBlock;
import org.ThefryGuy.techFactory.energy.EnergyManager;
import org.ThefryGuy.techFactory.energy.EnergyNetwork;
import org.ThefryGuy.techFactory.recipes.dusts.SiftedOreDust;

import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Electric Gold Pan Machine - Automated Panning
 * 
 * Processes:
 * - Gravel → Iron Nugget (40%), Sifted Ore Dust (35%), Clay Ball (15%), Flint (10%)
 * - Soul Sand → Nether Quartz, Nether Wart, Blaze Powder, Gold Nugget, Glowstone Dust, Ghast Tear (equal probability)
 * - Soul Soil → Same as Soul Sand
 * 
 * GUI Layout (5 rows x 9 columns = 45 slots):
 * Row 1: G G G G G G G G G  (Gray border)
 * Row 2: B B B B G O O O O  (B=Blue input, O=Orange output)
 * Row 3: B _ _ B P O _ _ O  (P=Processing indicator, _=Empty slot)
 * Row 4: B B B B G O O O O
 * Row 5: G G G G G G G G G  (Gray border)
 *
 * Input slots: 10, 11, 19, 20 (2x2 grid - accepts gravel, soul sand, soul soil)
 * Output slots: 14, 15, 23, 24 (2x2 grid - panned items)
 * Processing indicator: slot 22
 *
 * Power: 4 J per operation (same as Electric Furnace)
 * Speed: 1.0x (10 seconds per item, same as vanilla furnace)
 */
public class ElectricGoldPanMachine {

    private static final Random RANDOM = new Random();

    /**
     * Machine state
     */
    private static class MachineState {
        Inventory inventory;
        int processingProgress;
        ItemStack currentProcessing;
        ItemStack pendingOutput; // Output waiting to be added to output slots
        // CRITICAL FIX: Use SoftReference instead of WeakReference
        // SoftReference survives GC longer (only cleared when memory is critical)
        // Prevents random machine failures when GC runs during processing
        SoftReference<EnergyNetwork> cachedNetwork;
        long lastNetworkLookup;
        int ticksSinceActivity;

        MachineState(Inventory inventory) {
            this.inventory = inventory;
            this.processingProgress = 0;
            this.currentProcessing = null;
            this.pendingOutput = null;
            this.cachedNetwork = null;
            this.lastNetworkLookup = 0;
            this.ticksSinceActivity = 0;
        }
    }

    // State maps
    private static final Map<Location, MachineState> MACHINE_STATES = new ConcurrentHashMap<>();
    private static final Map<Player, Location> PLAYER_VIEWING = new ConcurrentHashMap<>();
    private static final Set<Location> ACTIVE_MACHINES = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // SCALABILITY: Queue for round-robin processing (Slimefun-style)
    // Instead of processing ALL machines every tick, process a subset
    private static final java.util.Queue<Location> PROCESSING_QUEUE = new java.util.concurrent.ConcurrentLinkedQueue<>();

    // Global processing task
    private static BukkitTask globalProcessingTask = null;

    // Constants
    private static final long NETWORK_CACHE_TTL_MS = 5000;
    private static final int[] INPUT_SLOTS = {10, 11, 19, 20};
    private static final int[] OUTPUT_SLOTS = {24, 25}; // Only the actual empty slots in row 3
    private static final int PROCESSING_TIME_TICKS = 200; // 10 seconds per item (same as vanilla furnace)
    private static final int PROCESSING_SLOT = 22;
    private static final int ENERGY_PER_PROCESS = 4; // 4 J per item (same as Electric Furnace)

    /**
     * Initialize the machine (no longer starts task)
     * NOTE: Global task is now managed by ElectricMachineProcessor (consolidated)
     */
    public static void initialize(TechFactory plugin) {
        plugin.getLogger().info("Electric Gold Pan initialized (task managed by ElectricMachineProcessor)");
    }

    /**
     * Shutdown method
     * NOTE: Global task is now managed by ElectricMachineProcessor
     */
    public static void shutdown() {
        // Task is managed by ElectricMachineProcessor, just clear data
        ACTIVE_MACHINES.clear();
        MACHINE_STATES.clear();
        PLAYER_VIEWING.clear();
    }

    /**
     * SCALABILITY: Queue-based round-robin processing (Slimefun-style)
     *
     * Instead of processing ALL machines every tick:
     * - Process up to maxPerTick machines from queue
     * - Re-add processed machines to end of queue
     * - Result: Each machine processed every N ticks instead of every tick
     *
     * Example with 1000 machines and maxPerTick=100:
     * - Each machine processed every 10 ticks instead of every tick
     * - 90% CPU reduction for large farms
     *
     * Package-private so ElectricMachineProcessor can call it
     */
    static void processQueuedMachines(TechFactory plugin, int maxPerTick) {
        // Sync queue with active set (add new machines to queue)
        for (Location loc : ACTIVE_MACHINES) {
            if (!PROCESSING_QUEUE.contains(loc)) {
                PROCESSING_QUEUE.add(loc);
            }
        }

        // Process up to maxPerTick machines from queue
        int processed = 0;
        while (processed < maxPerTick && !PROCESSING_QUEUE.isEmpty()) {
            Location loc = PROCESSING_QUEUE.poll();
            if (loc == null) break;

            // Check if still active
            if (!ACTIVE_MACHINES.contains(loc)) {
                continue; // Machine was removed, don't re-add to queue
            }

            MachineState state = MACHINE_STATES.get(loc);
            if (state == null) {
                ACTIVE_MACHINES.remove(loc); // Cleanup
                continue;
            }

            // Process item (energy removed atomically inside)
            processItem(loc, state, plugin);

            // Check if machine should be removed from active set (idle detection handled in processItem)
            if (!ACTIVE_MACHINES.contains(loc)) {
                continue; // Don't re-add to queue
            }

            // Re-add to end of queue for next cycle (round-robin)
            PROCESSING_QUEUE.add(loc);
            processed++;
        }
    }

    /**
     * Process all active machines
     * Called by ElectricMachineProcessor every 2 ticks
     * CRITICAL FIX: Removed batch energy operations to prevent race conditions
     *
     * @deprecated Use processQueuedMachines() for better scalability
     */
    @Deprecated
    static void processAllMachines(TechFactory plugin) {
        Iterator<Location> iterator = ACTIVE_MACHINES.iterator();
        while (iterator.hasNext()) {
            Location loc = iterator.next();
            MachineState state = MACHINE_STATES.get(loc);

            if (state == null) {
                iterator.remove();
                continue;
            }

            // Process item (energy removed atomically inside)
            processItem(loc, state, plugin);
        }
    }

    /**
     * Get network with caching
     */
    private static EnergyNetwork getNetworkCached(Location loc, MachineState state, TechFactory plugin) {
        long now = System.currentTimeMillis();

        if (state.cachedNetwork != null && (now - state.lastNetworkLookup) < NETWORK_CACHE_TTL_MS) {
            EnergyNetwork network = state.cachedNetwork.get();
            if (network != null) {
                return network;
            }
        }

        EnergyManager energyManager = plugin.getEnergyManager();
        EnergyNetwork network = energyManager.getNetworkByLocation(loc);

        if (network != null) {
            state.cachedNetwork = new SoftReference<>(network);
            state.lastNetworkLookup = now;
        }

        return network;
    }

    /**
     * CRITICAL FIX: Consume energy using transaction pattern
     * Prevents energy loss if output is full
     * @return true if energy was consumed, false if failed (will rollback)
     */
    private static boolean consumeEnergyWithTransaction(Location loc, MachineState state, TechFactory plugin, Runnable onSuccess) {
        EnergyNetwork network = getNetworkCached(loc, state, plugin);
        if (network != null) {
            org.ThefryGuy.techFactory.energy.EnergyTransaction tx =
                new org.ThefryGuy.techFactory.energy.EnergyTransaction(network);

            if (tx.tryRemove(ENERGY_PER_PROCESS)) {
                try {
                    // Execute the success callback (e.g., save inventory)
                    onSuccess.run();

                    // Commit transaction - keep energy removed
                    tx.commit();
                    return true;
                } catch (Exception e) {
                    // Operation failed - rollback transaction
                    tx.rollback();
                    plugin.getLogger().warning("Electric Gold Pan at " + loc + " failed to complete processing: " + e.getMessage());
                    return false;
                }
            } else {
                // Not enough energy
                plugin.getLogger().warning("Electric Gold Pan at " + loc + " finished processing but network had insufficient energy!");
                return false;
            }
        }

        // No network - allow operation without energy
        onSuccess.run();
        return true;
    }

    /**
     * Process a single item
     * CRITICAL FIX: Uses transaction pattern to prevent energy loss on failure
     */
    private static void processItem(Location loc, MachineState state, TechFactory plugin) {
        // If we have pending output waiting to be added, try to add it first
        if (state.pendingOutput != null) {
            if (addToOutputSlots(state.inventory, state.pendingOutput)) {
                // Success - clear pending output and reset
                state.pendingOutput = null;
                state.currentProcessing = null;
                state.processingProgress = 0;
                updateProcessingIndicator(state.inventory, false, 0);

                // CRITICAL FIX: Use transaction pattern - only consume energy if save succeeds
                consumeEnergyWithTransaction(loc, state, plugin, () -> {
                    saveInventory(loc, state.inventory, plugin);
                });
            } else {
                // Output still full - wait (don't consume energy)
                updateProcessingIndicator(state.inventory, false, 0);
                state.ticksSinceActivity++;
            }
            return;
        }

        // If currently processing, continue
        if (state.currentProcessing != null) {
            state.processingProgress++;
            state.ticksSinceActivity = 0;

            // Update progress indicator
            int progressPercent = (state.processingProgress * 100) / PROCESSING_TIME_TICKS;
            updateProcessingIndicator(state.inventory, true, progressPercent);

            // Check if done
            if (state.processingProgress >= PROCESSING_TIME_TICKS) {
                // Generate output ONCE and store it
                state.pendingOutput = generateOutput(state.currentProcessing.getType());

                // Try to add to output slots
                if (addToOutputSlots(state.inventory, state.pendingOutput)) {
                    // Success - clear pending output and reset
                    state.pendingOutput = null;
                    state.currentProcessing = null;
                    state.processingProgress = 0;
                    updateProcessingIndicator(state.inventory, false, 0);

                    // CRITICAL FIX: Use transaction pattern - only consume energy if save succeeds
                    consumeEnergyWithTransaction(loc, state, plugin, () -> {
                        saveInventory(loc, state.inventory, plugin);
                    });
                } else {
                    // Output full - keep pending output for next tick (don't consume energy)
                    updateProcessingIndicator(state.inventory, false, 0);
                }
            }
            return;
        }

        // Try to start new processing
        startNewProcessing(loc, state, plugin);
    }

    /**
     * Try to start processing a new item
     */
    private static void startNewProcessing(Location loc, MachineState state, TechFactory plugin) {
        Inventory inv = state.inventory;

        // Check energy network
        EnergyNetwork network = getNetworkCached(loc, state, plugin);

        if (network == null) {
            updateProcessingIndicator(inv, false, 0);
            state.ticksSinceActivity++;
            return;
        }

        // Check if network has energy
        if (!network.hasEnergy(ENERGY_PER_PROCESS)) {
            updateProcessingIndicator(inv, false, 0);
            state.ticksSinceActivity++;
            return;
        }

        // Find valid input
        ItemStack input = findValidInput(inv);
        if (input == null) {
            updateProcessingIndicator(inv, false, 0);
            state.ticksSinceActivity++;

            // PERFORMANCE: Remove from active set if idle too long
            // Uses configurable threshold (default 20 ticks = 1 second)
            if (state.ticksSinceActivity > TechFactoryConstants.ELECTRIC_MACHINE_IDLE_THRESHOLD_TICKS()) {
                ACTIVE_MACHINES.remove(loc);
            }
            return;
        }

        // Start processing
        state.currentProcessing = input.clone();
        state.currentProcessing.setAmount(1);
        state.processingProgress = 0;
        state.ticksSinceActivity = 0;

        // Consume one input item
        input.setAmount(input.getAmount() - 1);
        saveInventory(loc, inv, plugin);
    }

    /**
     * Find valid input material (gravel, soul sand, soul soil)
     */
    private static ItemStack findValidInput(Inventory inv) {
        for (int slot : INPUT_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item != null && isValidInput(item.getType())) {
                return item;
            }
        }
        return null;
    }

    /**
     * Check if material is valid input
     */
    private static boolean isValidInput(Material material) {
        return material == Material.GRAVEL || 
               material == Material.SOUL_SAND || 
               material == Material.SOUL_SOIL;
    }

    /**
     * Generate output based on input material
     */
    private static ItemStack generateOutput(Material input) {
        if (input == Material.GRAVEL) {
            return generateGravelOutput();
        } else {
            return generateSoulOutput();
        }
    }

    /**
     * Generate output from gravel (Gold Pan drops)
     */
    private static ItemStack generateGravelOutput() {
        int roll = RANDOM.nextInt(100);

        if (roll < 40) {
            return new ItemStack(Material.IRON_NUGGET);
        } else if (roll < 75) {
            SiftedOreDust dust = new SiftedOreDust();
            return dust.getItemStack();
        } else if (roll < 90) {
            return new ItemStack(Material.CLAY_BALL);
        } else {
            return new ItemStack(Material.FLINT);
        }
    }

    /**
     * Generate output from soul sand/soil (Nether Gold Pan drops)
     */
    private static ItemStack generateSoulOutput() {
        int roll = RANDOM.nextInt(6);

        return switch (roll) {
            case 0 -> new ItemStack(Material.QUARTZ);
            case 1 -> new ItemStack(Material.NETHER_WART);
            case 2 -> new ItemStack(Material.BLAZE_POWDER);
            case 3 -> new ItemStack(Material.GOLD_NUGGET);
            case 4 -> new ItemStack(Material.GLOWSTONE_DUST);
            default -> new ItemStack(Material.GHAST_TEAR);
        };
    }

    /**
     * Add item to output slots
     */
    private static boolean addToOutputSlots(Inventory inv, ItemStack item) {
        for (int slot : OUTPUT_SLOTS) {
            ItemStack existing = inv.getItem(slot);

            // Check if slot is empty
            if (existing == null || existing.getType() == Material.AIR) {
                inv.setItem(slot, item);
                return true;
            } else if (existing.isSimilar(item) && existing.getAmount() < existing.getMaxStackSize()) {
                existing.setAmount(existing.getAmount() + item.getAmount());
                return true;
            }
        }
        return false;
    }

    /**
     * Update processing indicator
     */
    private static void updateProcessingIndicator(Inventory inv, boolean processing, int progressPercent) {
        ItemStack indicator;

        if (processing) {
            indicator = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = indicator.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "Processing: " + progressPercent + "%");
                indicator.setItemMeta(meta);
            }
        } else {
            indicator = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta meta = indicator.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RED + "Idle");
                indicator.setItemMeta(meta);
            }
        }

        inv.setItem(PROCESSING_SLOT, indicator);
    }

    /**
     * Setup GUI decorations
     */
    private static void setupGUI(Inventory inv) {
        // Gray border
        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayMeta = grayPane.getItemMeta();
        if (grayMeta != null) {
            grayMeta.setDisplayName(" ");
            grayPane.setItemMeta(grayMeta);
        }

        // Blue input frame
        ItemStack bluePane = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        ItemMeta blueMeta = bluePane.getItemMeta();
        if (blueMeta != null) {
            blueMeta.setDisplayName(ChatColor.BLUE + "Input");
            bluePane.setItemMeta(blueMeta);
        }

        // Orange output frame
        ItemStack orangePane = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta orangeMeta = orangePane.getItemMeta();
        if (orangeMeta != null) {
            orangeMeta.setDisplayName(ChatColor.GOLD + "Output");
            orangePane.setItemMeta(orangeMeta);
        }

        // Row 1 (slots 0-8): All gray border
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, grayPane);
        }

        // Row 2 (slots 9-17): B B B B G O O O O
        inv.setItem(9, bluePane);
        inv.setItem(10, bluePane);
        inv.setItem(11, bluePane);
        inv.setItem(12, bluePane);
        inv.setItem(13, grayPane);
        inv.setItem(14, orangePane);
        inv.setItem(15, orangePane);
        inv.setItem(16, orangePane);
        inv.setItem(17, orangePane);

        // Row 3 (slots 18-26): B _ _ B P O _ _ O
        inv.setItem(18, bluePane);
        // 19, 20 are empty (input slots)
        inv.setItem(21, bluePane);
        // 22 is processing indicator
        inv.setItem(23, orangePane);
        // 24, 25 are empty (output slots)
        inv.setItem(26, orangePane);

        // Row 4 (slots 27-35): B B B B G O O O O
        inv.setItem(27, bluePane);
        inv.setItem(28, bluePane);
        inv.setItem(29, bluePane);
        inv.setItem(30, bluePane);
        inv.setItem(31, grayPane);
        inv.setItem(32, orangePane);
        inv.setItem(33, orangePane);
        inv.setItem(34, orangePane);
        inv.setItem(35, orangePane);

        // Row 5 (slots 36-44): All gray border
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, grayPane);
        }

        // Processing indicator
        updateProcessingIndicator(inv, false, 0);
    }

    /**
     * Open the Electric Gold Pan GUI
     */
    public static void openInventory(Block machineBlock, Player player, TechFactory plugin) {
        Location loc = machineBlock.getLocation();

        // Get or create machine state
        MachineState state = MACHINE_STATES.get(loc);
        if (state == null) {
            // Check if connected to energy network
            EnergyManager energyManager = plugin.getEnergyManager();
            EnergyNetwork network = energyManager.findNearestNetwork(loc, 6.0);

            if (network == null) {
                player.sendMessage(ChatColor.RED + "✗ Electric Gold Pan not connected to energy network!");
                player.sendMessage(ChatColor.GRAY + "Place an Energy Regulator or Connector within 6 blocks");
                return;
            }

            // Create custom inventory
            Inventory inv = Bukkit.createInventory(null, 45, ChatColor.GOLD + "Electric Gold Pan");
            setupGUI(inv);

            // Load saved inventory from database
            loadInventory(loc, inv, plugin);

            // Create new state
            state = new MachineState(inv);
            state.cachedNetwork = new SoftReference<>(network);
            state.lastNetworkLookup = System.currentTimeMillis();
            MACHINE_STATES.put(loc, state);

            // Add to active machines set
            ACTIVE_MACHINES.add(loc);
        }

        // Track which machine this player is viewing
        PLAYER_VIEWING.put(player, loc);

        // Open the inventory
        player.openInventory(state.inventory);
        player.sendMessage(ChatColor.GREEN + "✓ Electric Gold Pan opened!");
    }

    /**
     * Handle inventory close
     */
    public static void onClose(Player player, Inventory inv, TechFactory plugin, Location machineLoc) {
        // Save inventory to database
        saveInventory(machineLoc, inv, plugin);
    }

    /**
     * Save inventory to database
     */
    private static void saveInventory(Location location, Inventory inv, TechFactory plugin) {
        if (location == null || inv == null) {
            return;
        }

        // Get only the input and output slots (not decorative items)
        ItemStack[] contents = inv.getContents();
        ItemStack[] toSave = new ItemStack[45];

        // Save input slots
        for (int slot : INPUT_SLOTS) {
            toSave[slot] = contents[slot];
        }

        // Save output slots
        for (int slot : OUTPUT_SLOTS) {
            toSave[slot] = contents[slot];
        }

        // Save to database async
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String serialized = plugin.getDatabaseManager().serializeInventoryPublic(toSave);
                PlacedBlock placedBlock = plugin.getDatabaseManager().getBlock(location);
                if (placedBlock != null) {
                    placedBlock.setMetadata(serialized);
                    plugin.getDatabaseManager().saveBlock(placedBlock);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save electric gold pan inventory: " + e.getMessage());
            }
        });
    }

    /**
     * Load inventory from database
     */
    private static void loadInventory(Location location, Inventory inv, TechFactory plugin) {
        if (location == null || inv == null) {
            return;
        }

        PlacedBlock placedBlock = plugin.getDatabaseManager().getBlock(location);
        if (placedBlock == null || placedBlock.getMetadata() == null || placedBlock.getMetadata().equals("{}")) {
            return; // No saved inventory
        }

        try {
            ItemStack[] loaded = plugin.getDatabaseManager().deserializeInventoryPublic(placedBlock.getMetadata());
            if (loaded != null && loaded.length > 0) {
                // Restore input slots
                for (int slot : INPUT_SLOTS) {
                    if (slot < loaded.length && loaded[slot] != null) {
                        inv.setItem(slot, loaded[slot]);
                    }
                }

                // Restore output slots
                for (int slot : OUTPUT_SLOTS) {
                    if (slot < loaded.length && loaded[slot] != null) {
                        inv.setItem(slot, loaded[slot]);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load electric gold pan inventory: " + e.getMessage());
        }
    }

    /**
     * Drop inventory items when machine is broken
     */
    public static void dropInventoryItems(Location location, TechFactory plugin) {
        MachineState state = MACHINE_STATES.get(location);

        // If inventory is in memory, drop from there
        if (state != null && state.inventory != null) {
            dropItemsFromInventory(location, state.inventory);
            return;
        }

        // Otherwise, load from database and drop
        PlacedBlock placedBlock = plugin.getDatabaseManager().getBlock(location);
        if (placedBlock == null || placedBlock.getMetadata() == null || placedBlock.getMetadata().equals("{}")) {
            return; // No saved inventory
        }

        try {
            ItemStack[] loaded = plugin.getDatabaseManager().deserializeInventoryPublic(placedBlock.getMetadata());
            if (loaded != null && loaded.length > 0) {
                // Drop input items
                for (int slot : INPUT_SLOTS) {
                    if (slot < loaded.length && loaded[slot] != null) {
                        location.getWorld().dropItemNaturally(location, loaded[slot]);
                    }
                }

                // Drop output items
                for (int slot : OUTPUT_SLOTS) {
                    if (slot < loaded.length && loaded[slot] != null) {
                        location.getWorld().dropItemNaturally(location, loaded[slot]);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to drop electric gold pan items: " + e.getMessage());
        }
    }

    /**
     * Drop items from an inventory (only actual items, not decorative glass)
     */
    private static void dropItemsFromInventory(Location location, Inventory inv) {
        // Drop input items
        for (int slot : INPUT_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.AIR && !isDecorativeItem(item)) {
                location.getWorld().dropItemNaturally(location, item);
            }
        }

        // Drop output items
        for (int slot : OUTPUT_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.AIR && !isDecorativeItem(item)) {
                location.getWorld().dropItemNaturally(location, item);
            }
        }
    }

    /**
     * Check if an item is a decorative GUI item (glass panes)
     */
    private static boolean isDecorativeItem(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type == Material.GRAY_STAINED_GLASS_PANE ||
               type == Material.BLUE_STAINED_GLASS_PANE ||
               type == Material.ORANGE_STAINED_GLASS_PANE;
    }

    /**
     * Remove machine
     */
    public static void removeMachine(Location location) {
        MACHINE_STATES.remove(location);
        ACTIVE_MACHINES.remove(location);
    }

    /**
     * Get the location a player is viewing
     */
    public static Location getViewingLocation(Player player) {
        return PLAYER_VIEWING.get(player);
    }

    /**
     * Stop viewing
     */
    public static void stopViewing(Player player) {
        PLAYER_VIEWING.remove(player);
    }

    /**
     * Close all viewers of a specific machine (called when machine is broken)
     */
    public static void closeAllViewers(Location location) {
        // Find all players viewing this machine and close their inventories
        PLAYER_VIEWING.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(location)) {
                Player viewer = entry.getKey();
                viewer.closeInventory();
                return true; // Remove from map
            }
            return false;
        });
    }
}


