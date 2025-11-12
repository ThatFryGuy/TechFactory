package org.ThefryGuy.techFactory.workstations.multiblocks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.TechFactoryConstants;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.RecipeRegistry;
import org.ThefryGuy.techFactory.util.VanillaItemRegistry;
import org.ThefryGuy.techFactory.util.ItemUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Smelter Machine - Handles the actual functionality with timed smelting
 *
 * Structure:
 *   [Iron Bars]           ← On top of Blast Furnace
 *   [Brick] [Blast Furnace] [Brick]  ← Right-click Blast Furnace
 *   [Brick] [Campfire] [Brick]       ← Bottom layer
 */
public class SmelterMachine {

    // Track which player is viewing which smelter
    private static final Map<UUID, Location> PLAYER_VIEWING = new HashMap<>();

    // Store inventories for each Smelter location (for Slimefun-style crafting)
    private static final Map<Location, Inventory> SMELTER_INVENTORIES = new HashMap<>();

    // PERFORMANCE FIX: Dirty-flag system to batch database saves
    // Instead of saving on every click, mark dirty and save periodically
    private static final Map<Location, Long> SMELTER_DIRTY_FLAGS = new ConcurrentHashMap<>();

    // REFACTORED: Recipes now centralized in RecipeRegistry
    // Kept dust-to-ingot map for simple 1:1 conversions (not worth centralizing)
    private static final Map<String, ItemStack> DUST_TO_INGOT = new HashMap<>();

    static {
        // Initialize the dust-to-ingot conversion map (from Basic Smelter)
        DUST_TO_INGOT.put("iron_dust", new org.ThefryGuy.techFactory.recipes.ingots.IronIngot().getItemStack());
        DUST_TO_INGOT.put("gold_dust", new org.ThefryGuy.techFactory.recipes.ingots.GoldIngot().getItemStack());
        DUST_TO_INGOT.put("copper_dust", new org.ThefryGuy.techFactory.recipes.ingots.CopperIngot().getItemStack());
        DUST_TO_INGOT.put("tin_dust", new org.ThefryGuy.techFactory.recipes.ingots.TinIngot().getItemStack());
        DUST_TO_INGOT.put("silver_dust", new org.ThefryGuy.techFactory.recipes.ingots.SilverIngot().getItemStack());
        DUST_TO_INGOT.put("lead_dust", new org.ThefryGuy.techFactory.recipes.ingots.LeadIngot().getItemStack());
        DUST_TO_INGOT.put("aluminum_dust", new org.ThefryGuy.techFactory.recipes.ingots.AluminumIngot().getItemStack());
        DUST_TO_INGOT.put("zinc_dust", new org.ThefryGuy.techFactory.recipes.ingots.ZincIngot().getItemStack());
        DUST_TO_INGOT.put("magnesium_dust", new org.ThefryGuy.techFactory.recipes.ingots.MagnesiumIngot().getItemStack());
    }

    /**
     * Check if a block is part of a valid Smelter structure.
     * Called when player right-clicks a Blast Furnace.
     *
     * @param blastFurnaceBlock The blast furnace block that was clicked
     * @return true if this is a valid Smelter
     */
    public static boolean isValidStructure(Block blastFurnaceBlock) {
        if (blastFurnaceBlock.getType() != Material.BLAST_FURNACE) {
            return false;
        }

        // Check iron bars above
        Block ironBars = blastFurnaceBlock.getRelative(0, 1, 0);
        if (ironBars.getType() != Material.IRON_BARS) {
            return false;
        }

        // Check campfire below
        Block campfire = blastFurnaceBlock.getRelative(0, -1, 0);
        if (campfire.getType() != Material.CAMPFIRE && campfire.getType() != Material.SOUL_CAMPFIRE) {
            return false;
        }

        // Check for bricks on both sides (X-axis)
        Block side1 = blastFurnaceBlock.getRelative(1, 0, 0);
        Block side2 = blastFurnaceBlock.getRelative(-1, 0, 0);
        Block bottom1 = campfire.getRelative(1, 0, 0);
        Block bottom2 = campfire.getRelative(-1, 0, 0);
        boolean xAxisValid = side1.getType() == Material.BRICKS && side2.getType() == Material.BRICKS
                && bottom1.getType() == Material.BRICKS && bottom2.getType() == Material.BRICKS;

        // Check for bricks on both sides (Z-axis)
        Block side3 = blastFurnaceBlock.getRelative(0, 0, 1);
        Block side4 = blastFurnaceBlock.getRelative(0, 0, -1);
        Block bottom3 = campfire.getRelative(0, 0, 1);
        Block bottom4 = campfire.getRelative(0, 0, -1);
        boolean zAxisValid = side3.getType() == Material.BRICKS && side4.getType() == Material.BRICKS
                && bottom3.getType() == Material.BRICKS && bottom4.getType() == Material.BRICKS;

        // Must have bricks on one axis (either X or Z)
        return xAxisValid || zAxisValid;
    }

    /**
     * Open the custom Smelter GUI when player right-clicks the blast furnace.
     *
     * @param blastFurnaceBlock The blast furnace block
     * @param player The player who clicked
     */
    public static void openInventory(Block blastFurnaceBlock, Player player) {
        Location loc = blastFurnaceBlock.getLocation();
        TechFactory plugin = (TechFactory) JavaPlugin.getProvidingPlugin(SmelterMachine.class);

        // Get or create inventory for this smelter
        Inventory inv = SMELTER_INVENTORIES.get(loc);
        if (inv == null) {
            // Create custom inventory (27 slots = 3 rows)
            inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Smelter");
            // Add decorative items and labels FIRST
            setupGUI(inv);

            // Load inventory from database if it exists
            ItemStack[] savedInventory = plugin.getDatabaseManager().loadMultiblockInventory(loc);
            if (savedInventory != null && savedInventory.length > 0) {
                // Only restore items from INPUT slots (1,2,3, 10,11,12, 19,20,21) and OUTPUT slot (22)
                // This prevents old decorative items from overwriting the new GUI layout
                int[] validSlots = {1, 2, 3, 10, 11, 12, 19, 20, 21, 22};
                for (int slot : validSlots) {
                    if (slot < savedInventory.length && savedInventory[slot] != null) {
                        // Only restore if it's NOT a decorative item
                        Material mat = savedInventory[slot].getType();
                        if (mat != Material.GRAY_STAINED_GLASS_PANE && mat != Material.FLINT_AND_STEEL) {
                            inv.setItem(slot, savedInventory[slot]);
                        }
                    }
                }
            }

            SMELTER_INVENTORIES.put(loc, inv);
        }

        player.openInventory(inv);
        // Message is now shown in MultiblockListener for consistency

        // Store which smelter this player is viewing
        PLAYER_VIEWING.put(player.getUniqueId(), loc);
    }

    /**
     * Setup the GUI with decorative items and slots
     *
     * Layout (27 slots, 3 rows) - 3x3 input grid:
     * Row 1 (0-8):   [X][I][I][I][X][X][X][P][X]  - I = Input (1,2,3), P = Progress (7)
     * Row 2 (9-17):  [X][I][I][I][X][X][X][X][X]  - I = Input (10,11,12)
     * Row 3 (18-26): [X][I][I][I][X][O][X][X][X]  - I = Input (19,20,21), O = Output (22)
     * X = Gray glass pane (decorative)
     *
     * Total: 9 input slots arranged in 3x3 grid (supports up to 9-item recipes!)
     */
    private static void setupGUI(Inventory inv) {
        // Create barrier blocks for decoration
        ItemStack barrier = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta barrierMeta = barrier.getItemMeta();
        barrierMeta.setDisplayName(" ");
        barrier.setItemMeta(barrierMeta);

        // Fill all slots with barriers first
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, barrier);
        }

        // Clear input slots - 3x3 grid (slots 1,2,3, 10,11,12, 19,20,21)
        // Row 1
        inv.setItem(1, null);
        inv.setItem(2, null);
        inv.setItem(3, null);
        // Row 2
        inv.setItem(10, null);
        inv.setItem(11, null);
        inv.setItem(12, null);
        // Row 3
        inv.setItem(19, null);
        inv.setItem(20, null);
        inv.setItem(21, null);

        // Clear output slot (22)
        inv.setItem(22, null);

        // Add progress indicator in slot 7 (top row, right side before corner)
        ItemStack progress = new ItemStack(Material.FLINT_AND_STEEL);
        ItemMeta progressMeta = progress.getItemMeta();
        progressMeta.setDisplayName(ChatColor.YELLOW + "Progress");
        progressMeta.setLore(List.of(ChatColor.GRAY + "Waiting for items..."));
        progress.setItemMeta(progressMeta);
        inv.setItem(7, progress);
    }

    /**
     * Match input items to an alloy recipe (supports up to 9 items)
     * Returns the recipe match with quantity information
     */
    private static RecipeRegistry.RecipeMatch matchRecipe(ItemStack... inputs) {
        // Build map of available items and quantities
        Map<String, Integer> availableItems = new HashMap<>();

        for (ItemStack input : inputs) {
            if (input != null) {
                String id = ItemUtils.getItemId(input);
                if (id == null) {
                    // Check for vanilla items
                    id = VanillaItemRegistry.getVanillaItemId(input);
                }

                if (id != null) {
                    availableItems.put(id, availableItems.getOrDefault(id, 0) + input.getAmount());
                }
            }
        }

        if (availableItems.isEmpty()) {
            return null;
        }

        // REFACTORED: Use RecipeRegistry with "at least" quantity matching
        // This allows players to put stacks and craft multiple times (Slimefun-style)
        return RecipeRegistry.findRecipeWithQuantities("Smelter", availableItems);
    }

    /**
     * Get vanilla item ID for recipe matching
     */
    /**
     * REFACTORED: Now uses centralized VanillaItemRegistry
     * @deprecated Use VanillaItemRegistry.getVanillaItemId() directly
     */
    @Deprecated
    private static String getVanillaItemId(ItemStack item) {
        return VanillaItemRegistry.getVanillaItemId(item);
    }

    /**
     * Output item to the Smelter GUI output slot (slot 22).
     * Returns true if successful, false if output slot is full.
     */
    public static boolean outputToGUI(Location smelterLoc, ItemStack output) {
        Inventory inv = SMELTER_INVENTORIES.get(smelterLoc);
        if (inv == null) {
            return false; // No GUI exists
        }

        ItemStack currentOutput = inv.getItem(22);
        if (currentOutput == null || currentOutput.getType() == Material.AIR) {
            // Output slot is empty, place item
            inv.setItem(22, output);

            // PERFORMANCE FIX: Mark dirty instead of immediate save
            markDirty(smelterLoc);

            return true;
        } else if (currentOutput.isSimilar(output)) {
            // Same item type, try to stack
            int newAmount = currentOutput.getAmount() + output.getAmount();
            if (newAmount <= currentOutput.getMaxStackSize()) {
                currentOutput.setAmount(newAmount);

                // PERFORMANCE FIX: Mark dirty instead of immediate save
                markDirty(smelterLoc);

                return true;
            }
        }

        return false; // Output slot is full or can't stack
    }

    /**
     * Process recipe when player clicks iron bars (Slimefun style).
     * Now accepts smelterLoc as parameter instead of looking it up from PLAYER_VIEWING.
     *
     * REFACTORED: Split into smaller helper methods to reduce complexity
     */
    public static void onClose(Player player, Inventory inv, TechFactory plugin, Location smelterLoc) {
        if (smelterLoc == null) {
            return;
        }

        // Get all input items from the 3x3 grid
        ItemStack[] inputs = getInputItems(inv);

        // Check if inventory is empty
        if (areAllInputsEmpty(inputs)) {
            return; // No items to smelt
        }

        // Try dust-to-ingot conversion first (single item only)
        if (tryDustToIngotConversion(player, inv, plugin, smelterLoc, inputs)) {
            return; // Successfully started dust conversion
        }

        // Check if already smelting
        if (plugin.getSmeltingManager().isSmelting(smelterLoc)) {
            player.sendMessage(ChatColor.RED + "This Smelter is already smelting!");
            player.sendMessage(ChatColor.GRAY + "Hint: Items remain in the Smelter. Wait for current operation to finish.");
            return;
        }

        // Try alloy recipe
        processAlloyRecipe(player, inv, plugin, smelterLoc, inputs);
    }

    /**
     * Get all input items from the smelter's 3x3 grid
     */
    private static ItemStack[] getInputItems(Inventory inv) {
        return new ItemStack[] {
            inv.getItem(1), inv.getItem(2), inv.getItem(3),
            inv.getItem(10), inv.getItem(11), inv.getItem(12),
            inv.getItem(19), inv.getItem(20), inv.getItem(21)
        };
    }

    /**
     * Check if all input slots are empty
     */
    private static boolean areAllInputsEmpty(ItemStack[] inputs) {
        for (ItemStack input : inputs) {
            if (input != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Try to convert a single dust item to ingot
     * @return true if conversion was started, false otherwise
     */
    private static boolean tryDustToIngotConversion(Player player, Inventory inv, TechFactory plugin,
                                                     Location smelterLoc, ItemStack[] inputs) {
        // Count non-null items and find the single input
        int nonNullCount = 0;
        ItemStack singleInput = null;
        int singleInputSlot = -1;
        int[] slotIndices = {1, 2, 3, 10, 11, 12, 19, 20, 21};

        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] != null) {
                nonNullCount++;
                singleInput = inputs[i];
                singleInputSlot = slotIndices[i];
            }
        }

        // Only process if exactly one item
        if (nonNullCount != 1 || singleInput == null) {
            return false;
        }

        // Check if it's a dust
        String dustId = ItemUtils.getItemId(singleInput);
        ItemStack ingotTemplate = (dustId != null) ? DUST_TO_INGOT.get(dustId) : null;

        if (ingotTemplate == null) {
            return false; // Not a dust
        }

        // Convert dust to ingot
        int amount = singleInput.getAmount();
        ItemStack ingot = ingotTemplate.clone();
        ingot.setAmount(amount);

        // Clear the input slot (consume the dust)
        inv.setItem(singleInputSlot, null);
        markDirty(smelterLoc);

        // Create recipe item for the ingot
        RecipeItem ingotRecipe = createIngotRecipe(dustId, ingot);

        // Start smelting
        plugin.getSmeltingManager().startSmelting(smelterLoc, ingotRecipe, TechFactoryConstants.SMELTING_DURATION_MS());
        player.sendMessage(ItemUtils.createProgressMessage(ingot, "Smelting"));
        player.sendMessage(ChatColor.GRAY + "Time: 1.3 seconds");

        return true;
    }

    /**
     * Create a RecipeItem for ingot smelting
     */
    private static RecipeItem createIngotRecipe(String dustId, ItemStack ingot) {
        String ingotDisplayName = ItemUtils.getSafeDisplayName(ingot);

        return new RecipeItem() {
            @Override
            public String getId() { return dustId.replace("_dust", "_ingot"); }
            @Override
            public String getDisplayName() { return ingotDisplayName; }
            @Override
            public ChatColor getColor() { return ChatColor.WHITE; }
            @Override
            public Material getMaterial() { return ingot.getType(); }
            @Override
            public List<String> getLore() { return List.of(); }
            @Override
            public ItemStack[] getRecipe() { return new ItemStack[9]; }
            @Override
            public String getMachineType() { return "Smelter"; }
            @Override
            public ItemStack getItemStack() { return ingot; }
        };
    }

    /**
     * Process an alloy recipe (multiple items)
     */
    private static void processAlloyRecipe(Player player, Inventory inv, TechFactory plugin,
                                            Location smelterLoc, ItemStack[] inputs) {
        // Check if items match an alloy recipe
        RecipeRegistry.RecipeMatch match = matchRecipe(inputs[0], inputs[1], inputs[2], inputs[3],
                                        inputs[4], inputs[5], inputs[6], inputs[7], inputs[8]);

        if (match == null) {
            player.sendMessage(ChatColor.RED + "Invalid alloy recipe!");
            player.sendMessage(ChatColor.GRAY + "Hint: Items remain in the Smelter. Open the GUI to retrieve them.");
            return;
        }

        RecipeItem output = match.output;

        // Consume only the required quantities (not the entire stack!)
        int[] inputSlots = {1, 2, 3, 10, 11, 12, 19, 20, 21};
        for (Map.Entry<String, Integer> required : match.requiredQuantities.entrySet()) {
            String itemId = required.getKey();
            int qtyToRemove = required.getValue();

            // Remove items from input slots
            for (int slot : inputSlots) {
                if (qtyToRemove <= 0) break;

                ItemStack item = inv.getItem(slot);
                if (item != null) {
                    String currentItemId = ItemUtils.getItemId(item);
                    if (currentItemId == null) {
                        currentItemId = VanillaItemRegistry.getVanillaItemId(item);
                    }

                    if (itemId.equals(currentItemId)) {
                        int amountInSlot = item.getAmount();
                        if (amountInSlot <= qtyToRemove) {
                            // Remove entire stack
                            inv.setItem(slot, null);
                            qtyToRemove -= amountInSlot;
                        } else {
                            // Remove partial stack
                            item.setAmount(amountInSlot - qtyToRemove);
                            qtyToRemove = 0;
                        }
                    }
                }
            }
        }

        markDirty(smelterLoc);

        // Start the smelting operation
        plugin.getSmeltingManager().startSmelting(smelterLoc, output, TechFactoryConstants.SMELTING_DURATION_MS());

        player.sendMessage(ChatColor.YELLOW + "Smelting " + output.getColor() + output.getDisplayName() + ChatColor.YELLOW + "...");
        player.sendMessage(ChatColor.GRAY + "Time: 1.3 seconds");
    }

    /**
     * Clear all input slots in the smelter inventory
     */
    private static void clearInputSlots(Inventory inv) {
        int[] inputSlots = {1, 2, 3, 10, 11, 12, 19, 20, 21};
        for (int slot : inputSlots) {
            inv.setItem(slot, null);
        }
    }

    /**
     * Process crafting when player clicks iron bars (Slimefun style).
     * This is called when the player clicks the iron bars to trigger crafting.
     * The GUI should be CLOSED when clicking iron bars.
     */
    public static void processCraft(Player player, Block blastFurnaceBlock, TechFactory plugin) {
        Location smelterLoc = blastFurnaceBlock.getLocation();

        // Get the stored inventory for this smelter
        Inventory storedInv = SMELTER_INVENTORIES.get(smelterLoc);
        if (storedInv == null) {
            player.sendMessage(ChatColor.RED + "The Smelter is empty!");
            player.sendMessage(ChatColor.GRAY + "Hint: Click the Blast Furnace to open the GUI and add items first.");
            return;
        }

        // Process the recipe using the stored inventory, passing the smelter location
        onClose(player, storedInv, plugin, smelterLoc);
    }

    /**
     * Clean up the PLAYER_VIEWING map when player closes the inventory.
     * Also saves the inventory to database for persistence.
     */
    public static void cleanupPlayerViewing(Player player, Inventory inventory) {
        Location smelterLoc = PLAYER_VIEWING.remove(player.getUniqueId());

        if (smelterLoc != null && inventory != null) {
            // Save inventory to database (async to avoid lag)
            TechFactory plugin = (TechFactory) JavaPlugin.getProvidingPlugin(SmelterMachine.class);
            ItemStack[] contents = inventory.getContents();

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.getDatabaseManager().saveMultiblockInventory(smelterLoc, contents);
            });
        }
    }

    /**
     * Clear smelter inventory from memory (called when multiblock is destroyed)
     * MEMORY LEAK FIX: Prevents SMELTER_INVENTORIES from growing unbounded
     */
    public static void clearInventory(Location location) {
        SMELTER_INVENTORIES.remove(location);
        SMELTER_DIRTY_FLAGS.remove(location); // Also clear dirty flag
    }

    /**
     * Mark a smelter as dirty (needs database save)
     * PERFORMANCE FIX: Instead of saving immediately, mark dirty and save periodically
     */
    public static void markDirty(Location loc) {
        SMELTER_DIRTY_FLAGS.put(loc, System.currentTimeMillis());
    }

    /**
     * Start auto-save task for dirty smelters
     * PERFORMANCE FIX: Batches database saves instead of saving on every click
     * Saves every 5 seconds if inventory has been modified
     */
    public static void startAutoSaveTask(TechFactory plugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();

            // Check each dirty smelter
            SMELTER_DIRTY_FLAGS.entrySet().removeIf(entry -> {
                Location loc = entry.getKey();
                long dirtyTime = entry.getValue();

                // If dirty for 5+ seconds, save it
                if (now - dirtyTime >= TechFactoryConstants.SMELTER_SAVE_INTERVAL_MS()) {
                    Inventory inv = SMELTER_INVENTORIES.get(loc);
                    if (inv != null) {
                        plugin.getDatabaseManager().saveMultiblockInventory(loc, inv.getContents());
                    }
                    return true; // Remove from dirty flags (saved)
                }
                return false; // Keep in dirty flags (not ready to save yet)
            });
        }, 0L, TechFactoryConstants.SMELTER_DIRTY_CHECK_INTERVAL_TICKS()); // Check every 5 seconds
    }

}

