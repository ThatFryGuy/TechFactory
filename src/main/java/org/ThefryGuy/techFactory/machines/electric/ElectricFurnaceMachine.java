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

import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Electric Furnace Machine - Tier 1 Electric Machine
 * 
 * Smelts items using electricity instead of fuel.
 * 
 * GUI Layout (5 rows x 9 columns = 45 slots):
 * Row 1: G G G G G G G G G  (Gray border)
 * Row 2: B B B B G O O O O  (B=Blue input, O=Orange output)
 * Row 3: B _ _ B P O _ _ O  (P=Processing indicator, _=Empty slot)
 * Row 4: B B B B G O O O O
 * Row 5: G G G G G G G G G  (Gray border)
 * 
 * Input slots: 10, 11, 19, 20 (2x2 grid in blue frame)
 * Output slots: 14, 15, 23, 24 (2x2 grid in orange frame)
 * Processing indicator: slot 22
 * 
 * Power: 4 J/SF (Joules per Smelt Furnace tick)
 * Speed: 1.0x (same as vanilla furnace)
 */
public class ElectricFurnaceMachine {

    /**
     * Consolidated furnace state to reduce memory overhead
     * Replaces 4 separate maps with single object
     */
    private static class FurnaceState {
        Inventory inventory;
        int smeltingProgress;
        ItemStack currentSmelting;
        // CRITICAL FIX: Use SoftReference instead of WeakReference
        // SoftReference survives GC longer (only cleared when memory is critical)
        // Prevents random furnace failures when GC runs during processing
        SoftReference<EnergyNetwork> cachedNetwork;
        long lastNetworkLookup;
        int ticksSinceActivity;

        FurnaceState(Inventory inventory) {
            this.inventory = inventory;
            this.smeltingProgress = 0;
            this.currentSmelting = null;
            this.cachedNetwork = null;
            this.lastNetworkLookup = 0;
            this.ticksSinceActivity = 0;
        }
    }

    // Consolidated state map (replaces 4 separate maps)
    private static final Map<Location, FurnaceState> FURNACE_STATES = new ConcurrentHashMap<>();

    // Track which player is viewing which furnace
    private static final Map<Player, Location> PLAYER_VIEWING = new ConcurrentHashMap<>();

    // Active furnaces that need processing (for global task)
    private static final Set<Location> ACTIVE_FURNACES = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // SCALABILITY: Queue for round-robin processing (Slimefun-style)
    // Instead of processing ALL furnaces every tick, process a subset
    private static final java.util.Queue<Location> PROCESSING_QUEUE = new java.util.concurrent.ConcurrentLinkedQueue<>();

    // Recipe cache for O(1) lookups (replaces expensive iteration)
    private static final Map<Material, ItemStack> FURNACE_RECIPE_CACHE = new ConcurrentHashMap<>();

    // Global smelting task (single task for all furnaces)
    private static BukkitTask globalSmeltingTask = null;

    // Network cache TTL in milliseconds (5 seconds)
    private static final long NETWORK_CACHE_TTL_MS = 5000;

    // Input slots (2x2 grid in blue frame)
    private static final int[] INPUT_SLOTS = {10, 11, 19, 20};

    // Output slots (2x2 grid in orange frame)
    private static final int[] OUTPUT_SLOTS = {14, 15, 23, 24};

    // Smelting time in ticks (200 ticks = 10 seconds, same as vanilla furnace)
    private static final int SMELT_TIME_TICKS = 200;

    // Processing indicator slot
    private static final int PROCESSING_SLOT = 22;

    // Energy cost per smelt operation
    private static final int ENERGY_PER_SMELT = 4;

    /**
     * Initialize the recipe cache
     * Call this once during plugin startup
     *
     * NOTE: Global task is now managed by ElectricMachineProcessor (consolidated)
     */
    public static void initialize(TechFactory plugin) {
        // Build recipe cache once at startup
        buildRecipeCache();
        plugin.getLogger().info("Cached " + FURNACE_RECIPE_CACHE.size() + " furnace recipes for Electric Furnace");
    }

    /**
     * Shutdown method
     * NOTE: Global task is now managed by ElectricMachineProcessor
     */
    public static void shutdown() {
        // Task is managed by ElectricMachineProcessor, just clear data
        ACTIVE_FURNACES.clear();
        FURNACE_STATES.clear();
        PLAYER_VIEWING.clear();
        FURNACE_RECIPE_CACHE.clear();
    }

    /**
     * Build the furnace recipe cache for O(1) lookups
     * Replaces expensive iteration through all recipes
     */
    private static void buildRecipeCache() {
        FURNACE_RECIPE_CACHE.clear();
        Iterator<org.bukkit.inventory.Recipe> iterator = Bukkit.recipeIterator();
        int count = 0;
        while (iterator.hasNext()) {
            org.bukkit.inventory.Recipe recipe = iterator.next();
            if (recipe instanceof org.bukkit.inventory.FurnaceRecipe) {
                org.bukkit.inventory.FurnaceRecipe furnaceRecipe = (org.bukkit.inventory.FurnaceRecipe) recipe;
                FURNACE_RECIPE_CACHE.put(furnaceRecipe.getInput().getType(), furnaceRecipe.getResult().clone());
                count++;
            }
        }
        Bukkit.getLogger().info("Cached " + count + " furnace recipes for Electric Furnace");
    }

    /**
     * SCALABILITY: Queue-based round-robin processing (Slimefun-style)
     *
     * Instead of processing ALL furnaces every tick:
     * - Process up to maxPerTick furnaces from queue
     * - Re-add processed furnaces to end of queue
     * - Result: Each furnace processed every N ticks instead of every tick
     *
     * Example with 1000 furnaces and maxPerTick=100:
     * - Each furnace processed every 10 ticks instead of every tick
     * - 90% CPU reduction for large farms
     *
     * Package-private so ElectricMachineProcessor can call it
     */
    static void processQueuedFurnaces(TechFactory plugin, int maxPerTick) {
        // Sync queue with active set (add new furnaces to queue)
        for (Location loc : ACTIVE_FURNACES) {
            if (!PROCESSING_QUEUE.contains(loc)) {
                PROCESSING_QUEUE.add(loc);
            }
        }

        // Process up to maxPerTick furnaces from queue
        int processed = 0;
        while (processed < maxPerTick && !PROCESSING_QUEUE.isEmpty()) {
            Location loc = PROCESSING_QUEUE.poll();
            if (loc == null) break;

            // Check if still active
            if (!ACTIVE_FURNACES.contains(loc)) {
                continue; // Furnace was removed, don't re-add to queue
            }

            FurnaceState state = FURNACE_STATES.get(loc);
            if (state == null) {
                ACTIVE_FURNACES.remove(loc); // Cleanup
                continue;
            }

            // Process this furnace (energy removed atomically inside)
            processSmelting(loc, state, plugin);

            // PERFORMANCE: Check if furnace is idle and should be removed from active set
            // Uses configurable threshold (default 20 ticks = 1 second)
            if (state.smeltingProgress == 0 && !hasSmeltableItems(state.inventory)) {
                state.ticksSinceActivity++;
                if (state.ticksSinceActivity > TechFactoryConstants.ELECTRIC_MACHINE_IDLE_THRESHOLD_TICKS()) {
                    ACTIVE_FURNACES.remove(loc); // Stop processing idle furnace
                    continue; // Don't re-add to queue
                }
            } else {
                state.ticksSinceActivity = 0; // Reset idle counter
            }

            // Re-add to end of queue for next cycle (round-robin)
            PROCESSING_QUEUE.add(loc);
            processed++;
        }
    }

    /**
     * Process all active furnaces in a single batch
     * Replaces per-furnace tasks
     * Package-private so ElectricMachineProcessor can call it
     *
     * @deprecated Use processQueuedFurnaces() for better scalability
     */
    @Deprecated
    static void processAllFurnaces(TechFactory plugin) {
        // CRITICAL FIX: Removed batch energy operations to prevent race conditions
        // Energy is now removed atomically when consumed (in finishSmelting)
        // This prevents negative energy and race conditions with multiple furnaces

        // Process each active furnace
        Iterator<Location> iterator = ACTIVE_FURNACES.iterator();
        while (iterator.hasNext()) {
            Location loc = iterator.next();
            FurnaceState state = FURNACE_STATES.get(loc);

            if (state == null) {
                iterator.remove(); // Furnace was removed
                continue;
            }

            // Process this furnace (energy removed atomically inside)
            processSmelting(loc, state, plugin);

            // PERFORMANCE: Check if furnace is idle and should be removed from active set
            // Uses configurable threshold (default 20 ticks = 1 second)
            if (state.smeltingProgress == 0 && !hasSmeltableItems(state.inventory)) {
                state.ticksSinceActivity++;
                if (state.ticksSinceActivity > TechFactoryConstants.ELECTRIC_MACHINE_IDLE_THRESHOLD_TICKS()) {
                    iterator.remove(); // Stop processing idle furnace
                }
            } else {
                state.ticksSinceActivity = 0; // Reset idle counter
            }
        }
    }

    /**
     * Get cached energy network for a furnace location
     * Uses TTL-based cache to avoid expensive proximity searches every tick
     */
    private static EnergyNetwork getNetworkCached(Location loc, FurnaceState state, TechFactory plugin) {
        long now = System.currentTimeMillis();

        // Check if cache is still valid
        if (state.cachedNetwork != null && state.cachedNetwork.get() != null) {
            if (now - state.lastNetworkLookup < NETWORK_CACHE_TTL_MS) {
                return state.cachedNetwork.get();
            }
        }

        // Cache expired or invalid, do fresh lookup
        EnergyNetwork network = plugin.getEnergyManager().findNearestNetwork(loc, 6.0);
        if (network != null) {
            state.cachedNetwork = new SoftReference<>(network);
            state.lastNetworkLookup = now;
        } else {
            state.cachedNetwork = null;
            state.lastNetworkLookup = now;
        }
        return network;
    }

    /**
     * Check if inventory has any smeltable items
     */
    private static boolean hasSmeltableItems(Inventory inv) {
        for (int slot : INPUT_SLOTS) {
            ItemStack input = inv.getItem(slot);
            if (input != null && input.getType() != Material.AIR) {
                if (getSmeltingResult(input) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Open the Electric Furnace GUI
     */
    public static void openInventory(Block furnaceBlock, Player player, TechFactory plugin) {
        Location loc = furnaceBlock.getLocation();

        // Get or create furnace state
        FurnaceState state = FURNACE_STATES.get(loc);
        if (state == null) {
            // Check if connected to energy network
            EnergyManager energyManager = plugin.getEnergyManager();
            EnergyNetwork network = energyManager.findNearestNetwork(loc, 6.0);

            if (network == null) {
                player.sendMessage(ChatColor.RED + "✗ Electric Furnace not connected to energy network!");
                player.sendMessage(ChatColor.GRAY + "Place an Energy Regulator or Connector within 6 blocks");
                return;
            }

            // Create custom inventory (45 slots = 5 rows)
            Inventory inv = Bukkit.createInventory(null, 45, ChatColor.YELLOW + "Electric Furnace");
            setupGUI(inv);

            // Load saved inventory from database
            loadInventory(loc, inv, plugin);

            // Create new state
            state = new FurnaceState(inv);
            state.cachedNetwork = new SoftReference<>(network);
            state.lastNetworkLookup = System.currentTimeMillis();
            FURNACE_STATES.put(loc, state);

            // Add to active furnaces set
            ACTIVE_FURNACES.add(loc);
        }

        // Track which furnace this player is viewing
        PLAYER_VIEWING.put(player, loc);

        // Open the inventory
        player.openInventory(state.inventory);
        player.sendMessage(ChatColor.GREEN + "✓ Electric Furnace opened!");
    }

    /**
     * Process smelting for a furnace (called every tick by global task)
     * CRITICAL FIX: Energy is now removed atomically inside finishSmelting()
     */
    private static void processSmelting(Location loc, FurnaceState state, TechFactory plugin) {
        Inventory inv = state.inventory;

        // Check if currently smelting
        if (state.smeltingProgress > 0) {
            // Continue smelting
            state.smeltingProgress--;
            updateProcessingIndicator(inv, true, state.smeltingProgress);
            return;
        }

        // If we just finished smelting, output the result
        if (state.smeltingProgress == 0 && state.currentSmelting != null) {
            finishSmelting(loc, state, plugin);
            state.currentSmelting = null;
            return;
        }

        // Try to start new smelting
        startNewSmelting(loc, state, plugin);
    }

    /**
     * Try to start smelting a new item
     */
    private static void startNewSmelting(Location loc, FurnaceState state, TechFactory plugin) {
        Inventory inv = state.inventory;

        // Check energy network (using cache)
        EnergyNetwork network = getNetworkCached(loc, state, plugin);

        if (network == null) {
            updateProcessingIndicator(inv, false, 0);
            return; // Not connected
        }

        // Check if network has energy
        if (!network.hasEnergy(ENERGY_PER_SMELT)) {
            updateProcessingIndicator(inv, false, 0);
            return; // Not enough energy
        }

        // Find first smeltable item in input slots
        for (int slot : INPUT_SLOTS) {
            ItemStack input = inv.getItem(slot);
            if (input == null || input.getType() == Material.AIR) {
                continue;
            }

            // Check if this item can be smelted (using cache)
            ItemStack result = getSmeltingResult(input);
            if (result == null) {
                continue; // Can't smelt this item
            }

            // Check if there's space in output
            if (!canAddToOutput(inv, result)) {
                continue; // Output full
            }

            // Start smelting!
            state.smeltingProgress = SMELT_TIME_TICKS;
            state.currentSmelting = input.clone();

            // Remove one item from input
            if (input.getAmount() > 1) {
                input.setAmount(input.getAmount() - 1);
            } else {
                inv.setItem(slot, null);
            }

            updateProcessingIndicator(inv, true, SMELT_TIME_TICKS);
            return;
        }

        // Nothing to smelt
        updateProcessingIndicator(inv, false, 0);
    }

    /**
     * Finish smelting and output the result
     * CRITICAL FIX: Uses transaction pattern to prevent energy loss on failure
     */
    private static void finishSmelting(Location loc, FurnaceState state, TechFactory plugin) {
        Inventory inv = state.inventory;
        ItemStack input = state.currentSmelting;
        if (input == null) {
            return;
        }

        // Get result (using cache)
        ItemStack result = getSmeltingResult(input);
        if (result == null) {
            return;
        }

        // CRITICAL FIX: Use transaction pattern to prevent energy loss
        // If output is full or operation fails, energy is restored
        EnergyNetwork network = getNetworkCached(loc, state, plugin);
        if (network != null) {
            org.ThefryGuy.techFactory.energy.EnergyTransaction tx =
                new org.ThefryGuy.techFactory.energy.EnergyTransaction(network);

            if (tx.tryRemove(ENERGY_PER_SMELT)) {
                try {
                    // Try to add to output
                    boolean success = addToOutput(inv, result.clone());

                    if (success) {
                        // Success - commit transaction (keep energy removed)
                        tx.commit();
                        updateProcessingIndicator(inv, false, 0);
                    } else {
                        // Output full - rollback transaction (restore energy)
                        tx.rollback();
                        // Don't clear processing indicator - will retry next tick
                    }
                } catch (Exception e) {
                    // Operation failed - rollback transaction
                    tx.rollback();
                    plugin.getLogger().warning("Electric Furnace at " + loc + " failed to complete smelting: " + e.getMessage());
                }
            } else {
                // Not enough energy - this shouldn't happen but handle gracefully
                plugin.getLogger().warning("Electric Furnace at " + loc + " finished smelting but network had insufficient energy!");
            }
        } else {
            // No network - just output without consuming energy
            addToOutput(inv, result.clone());
            updateProcessingIndicator(inv, false, 0);
        }
    }
    
    /**
     * Setup the GUI with decorative borders and frames
     *
     * Layout:
     * Row 1 (0-8):   G G G G G G G G G
     * Row 2 (9-17):  B B B B G O O O O
     * Row 3 (18-26): B _ _ B P O _ _ O
     * Row 4 (27-35): B B B B G O O O O
     * Row 5 (36-44): G G G G G G G G G
     */
    private static void setupGUI(Inventory inv) {
        // Gray stained glass for border
        ItemStack grayGlass = createGlassPane(Material.GRAY_STAINED_GLASS_PANE, " ");

        // Blue stained glass for input frame
        ItemStack blueGlass = createGlassPane(Material.BLUE_STAINED_GLASS_PANE, ChatColor.BLUE + "Input");

        // Orange stained glass for output frame
        ItemStack orangeGlass = createGlassPane(Material.ORANGE_STAINED_GLASS_PANE, ChatColor.GOLD + "Output");

        // Row 1 (slots 0-8): All gray border
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, grayGlass);
        }

        // Row 2 (slots 9-17): B B B B G O O O O
        inv.setItem(9, blueGlass);
        inv.setItem(10, blueGlass);
        inv.setItem(11, blueGlass);
        inv.setItem(12, blueGlass);
        inv.setItem(13, grayGlass);
        inv.setItem(14, orangeGlass);
        inv.setItem(15, orangeGlass);
        inv.setItem(16, orangeGlass);
        inv.setItem(17, orangeGlass);

        // Row 3 (slots 18-26): B _ _ B P O _ _ O
        inv.setItem(18, blueGlass);
        // 19, 20 are empty (input slots)
        inv.setItem(21, blueGlass);
        // 22 is processing indicator
        inv.setItem(23, orangeGlass);
        // 24, 25 are empty (output slots)
        inv.setItem(26, orangeGlass);

        // Row 4 (slots 27-35): B B B B G O O O O
        inv.setItem(27, blueGlass);
        inv.setItem(28, blueGlass);
        inv.setItem(29, blueGlass);
        inv.setItem(30, blueGlass);
        inv.setItem(31, grayGlass);
        inv.setItem(32, orangeGlass);
        inv.setItem(33, orangeGlass);
        inv.setItem(34, orangeGlass);
        inv.setItem(35, orangeGlass);

        // Row 5 (slots 36-44): All gray border
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, grayGlass);
        }

        // Set processing indicator (initially empty/waiting)
        updateProcessingIndicator(inv, false, 0);
    }
    
    /**
     * Create a glass pane with a custom name
     */
    private static ItemStack createGlassPane(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Update the processing indicator
     */
    private static void updateProcessingIndicator(Inventory inv, boolean processing, int ticksRemaining) {
        ItemStack indicator;
        if (processing) {
            indicator = new ItemStack(Material.FIRE_CHARGE);
            ItemMeta meta = indicator.getItemMeta();
            if (meta != null) {
                int secondsRemaining = ticksRemaining / 20;
                int progress = ((SMELT_TIME_TICKS - ticksRemaining) * 100) / SMELT_TIME_TICKS;
                meta.setDisplayName(ChatColor.RED + "⚡ Smelting...");
                java.util.List<String> lore = new java.util.ArrayList<>();
                lore.add(ChatColor.GRAY + "Progress: " + ChatColor.YELLOW + progress + "%");
                lore.add(ChatColor.GRAY + "Time: " + ChatColor.YELLOW + secondsRemaining + "s");
                meta.setLore(lore);
                indicator.setItemMeta(meta);
            }
        } else {
            indicator = new ItemStack(Material.GUNPOWDER);
            ItemMeta meta = indicator.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GRAY + "⚡ Idle");
                indicator.setItemMeta(meta);
            }
        }
        inv.setItem(PROCESSING_SLOT, indicator);
    }
    
    // Pending saves for debouncing (location -> last save time)
    private static final Map<Location, Long> PENDING_SAVES = new ConcurrentHashMap<>();
    private static final long SAVE_DEBOUNCE_MS = 1000; // 1 second debounce

    /**
     * Called when GUI is closed - save inventory to database
     */
    public static void onClose(Player player, Inventory inv, TechFactory plugin, Location furnaceLoc) {
        if (furnaceLoc == null) {
            return;
        }

        // Save inventory to database (with debouncing)
        saveInventory(furnaceLoc, inv, plugin);
    }

    /**
     * Save furnace inventory to database with debouncing
     */
    private static void saveInventory(Location location, Inventory inv, TechFactory plugin) {
        if (location == null || inv == null) {
            return;
        }

        long now = System.currentTimeMillis();
        Long lastSave = PENDING_SAVES.get(location);

        // Debounce: skip if saved recently
        if (lastSave != null && (now - lastSave) < SAVE_DEBOUNCE_MS) {
            return;
        }

        PENDING_SAVES.put(location, now);

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
        org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String serialized = plugin.getDatabaseManager().serializeInventoryPublic(toSave);
                PlacedBlock placedBlock = plugin.getDatabaseManager().getBlock(location);
                if (placedBlock != null) {
                    placedBlock.setMetadata(serialized);
                    plugin.getDatabaseManager().saveBlock(placedBlock);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save electric furnace inventory: " + e.getMessage());
            }
        });
    }

    /**
     * Load furnace inventory from database
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
            plugin.getLogger().warning("Failed to load electric furnace inventory: " + e.getMessage());
        }
    }
    
    /**
     * Get input items from the input slots
     */
    private static ItemStack[] getInputItems(Inventory inv) {
        ItemStack[] inputs = new ItemStack[INPUT_SLOTS.length];
        for (int i = 0; i < INPUT_SLOTS.length; i++) {
            inputs[i] = inv.getItem(INPUT_SLOTS[i]);
        }
        return inputs;
    }
    
    /**
     * Check if all inputs are empty
     */
    private static boolean areAllInputsEmpty(ItemStack[] inputs) {
        for (ItemStack input : inputs) {
            if (input != null && input.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get the smelting result for an item (uses cached recipes for O(1) lookup)
     */
    private static ItemStack getSmeltingResult(ItemStack input) {
        if (input == null || input.getType() == Material.AIR) {
            return null;
        }

        // O(1) lookup from cache instead of iterating all recipes
        ItemStack result = FURNACE_RECIPE_CACHE.get(input.getType());
        return result != null ? result.clone() : null;
    }
    
    /**
     * Check if an item can be added to output slots
     */
    private static boolean canAddToOutput(Inventory inv, ItemStack item) {
        if (item == null) {
            return false;
        }

        // Check if there's space in existing stacks
        for (int slot : OUTPUT_SLOTS) {
            ItemStack existing = inv.getItem(slot);
            if (existing == null || existing.getType() == Material.AIR) {
                return true; // Empty slot found
            }
            if (existing.isSimilar(item)) {
                int space = existing.getMaxStackSize() - existing.getAmount();
                if (space > 0) {
                    return true; // Can stack
                }
            }
        }

        return false; // No space
    }

    /**
     * Add item to output slots (tries to stack with existing items)
     * CRITICAL FIX: Now returns boolean to indicate success/failure
     * @return true if item was added, false if output is full
     */
    private static boolean addToOutput(Inventory inv, ItemStack item) {
        // Try to add to existing stacks first
        for (int slot : OUTPUT_SLOTS) {
            ItemStack existing = inv.getItem(slot);
            if (existing != null && existing.isSimilar(item)) {
                int space = existing.getMaxStackSize() - existing.getAmount();
                if (space > 0) {
                    int toAdd = Math.min(space, item.getAmount());
                    existing.setAmount(existing.getAmount() + toAdd);
                    item.setAmount(item.getAmount() - toAdd);
                    if (item.getAmount() <= 0) {
                        return true; // All items added
                    }
                }
            }
        }

        // Add to empty slots
        for (int slot : OUTPUT_SLOTS) {
            ItemStack existing = inv.getItem(slot);
            if (existing == null || existing.getType() == Material.AIR) {
                inv.setItem(slot, item.clone());
                return true;
            }
        }

        // Output is full
        return false;
    }
    
    /**
     * Get the furnace location that a player is viewing
     */
    public static Location getViewingLocation(Player player) {
        return PLAYER_VIEWING.get(player);
    }
    
    /**
     * Remove player from viewing map
     */
    public static void stopViewing(Player player) {
        PLAYER_VIEWING.remove(player);
    }
    
    /**
     * Drop all items from furnace inventory when broken
     */
    public static void dropInventoryItems(Location location, TechFactory plugin) {
        FurnaceState state = FURNACE_STATES.get(location);

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
                // Drop input and output items
                for (int slot : INPUT_SLOTS) {
                    if (slot < loaded.length && loaded[slot] != null) {
                        location.getWorld().dropItemNaturally(location, loaded[slot]);
                    }
                }
                for (int slot : OUTPUT_SLOTS) {
                    if (slot < loaded.length && loaded[slot] != null) {
                        location.getWorld().dropItemNaturally(location, loaded[slot]);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to drop electric furnace items: " + e.getMessage());
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
     * Remove furnace inventory when furnace is broken
     */
    public static void removeFurnace(Location location) {
        FURNACE_STATES.remove(location);
        ACTIVE_FURNACES.remove(location);
        PENDING_SAVES.remove(location);
    }
}

