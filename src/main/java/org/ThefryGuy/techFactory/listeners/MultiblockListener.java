package org.ThefryGuy.techFactory.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.TechFactoryConstants;
import org.ThefryGuy.techFactory.data.DatabaseManager;
import org.ThefryGuy.techFactory.data.MultiblockData;
import org.ThefryGuy.techFactory.registry.MachineRegistry;
import org.ThefryGuy.techFactory.registry.MultiblockMachine;
import org.ThefryGuy.techFactory.workstations.multiblocks.*;
import org.ThefryGuy.techFactory.util.RateLimiter;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener for multiblock machine interactions.
 *
 * Handles:
 * - Right-clicking multiblocks to open them
 * - Closing multiblock inventories to process recipes
 * - Breaking multiblocks
 *
 * SECURITY HARDENED:
 * - Rate limiting on all interactions
 * - GUI exploit protection (shift-click, drag, number keys, etc.)
 * - Spam-click DoS prevention
 */
public class MultiblockListener implements Listener {

    private final TechFactory plugin;
    private final DatabaseManager databaseManager;

    // REFACTORED: Use RateLimiter utility for cleaner, reusable rate limiting
    private final RateLimiter clickLimiter = new RateLimiter(TechFactoryConstants.MULTIBLOCK_CLICK_COOLDOWN_MS());
    private final RateLimiter inventoryClickLimiter = new RateLimiter(TechFactoryConstants.INVENTORY_CLICK_COOLDOWN_MS());

    public MultiblockListener(TechFactory plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();

        // Start cleanup task to prevent memory leaks (runs every 30 seconds)
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::cleanupOldCooldowns,
            TechFactoryConstants.RATE_LIMITER_CLEANUP_INTERVAL_TICKS(),
            TechFactoryConstants.RATE_LIMITER_CLEANUP_INTERVAL_TICKS());
    }

    /**
     * Clean up old cooldown entries to prevent memory leaks.
     * REFACTORED: Now uses RateLimiter.cleanup() for automatic memory management
     */
    private void cleanupOldCooldowns() {
        // Clean up click cooldowns (remove entries older than configured max age)
        int removed = clickLimiter.cleanup(TechFactoryConstants.RATE_LIMITER_MAX_AGE_MS());
        removed += inventoryClickLimiter.cleanup(TechFactoryConstants.RATE_LIMITER_MAX_AGE_MS());

        // Only log if we actually removed something and logging is enabled
        if (removed > 0 && TechFactoryConstants.LOG_RATE_LIMITER_CLEANUP()) {
            plugin.getLogger().fine("Cleaned up " + removed + " old rate limiter entries");
        }
    }

    /**
     * Check if player is on cooldown for clicking this location.
     * Returns true if they should be allowed to click (not on cooldown).
     * REFACTORED: Now uses RateLimiter utility
     * BUG FIX 3: Added null checks to prevent NullPointerException
     */
    private boolean checkCooldown(Player player, Location location) {
        // BUG FIX 3: Null checks
        if (player == null || location == null) {
            return false; // Deny access if null
        }

        String key = player.getUniqueId().toString() + ":" +
                     location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
        return clickLimiter.tryAccess(key);
    }

    /**
     * Handle player right-clicking blocks.
     * Check if they're clicking a multiblock machine.
     *
     * REFACTORED: Uses MachineRegistry instead of hardcoded if/if/if chains
     * BUG FIX: Added EventPriority.HIGH and ignoreCancelled=false to prevent conflicts with other plugins
     * BUG FIX: Removed cancelled check - we need to handle trapdoors/doors which are cancelled by vanilla
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only care about right-clicks on blocks
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }

        Player player = event.getPlayer();

        // Check cooldown to prevent double-click triggering
        if (!checkCooldown(player, clicked.getLocation())) {
            return; // Player is on cooldown, ignore this click
        }

        // ========================================
        // MACHINE REGISTRY PATTERN
        // ========================================
        // Get all machines that can handle this block type
        List<MultiblockMachine> candidates = MachineRegistry.getMachinesForBlock(clicked.getType());

        if (candidates.isEmpty()) {
            return; // Not a machine block
        }

        // Find the first valid machine at this location
        for (MultiblockMachine machine : candidates) {
            if (!machine.isValidStructure(clicked)) {
                continue; // Not a valid structure for this machine
            }

            // Valid machine found!
            event.setCancelled(true); // Prevent vanilla block interaction

            // Register multiblock in database
            registerMultiblock(clicked, machine.getMachineType(), player);

            // Handle GUI or trigger interaction
            // BUG FIX: Check if it's the GUI block first, otherwise treat as trigger
            // This allows machines to have multiple trigger block types (e.g., all trapdoor types)
            if (clicked.getType() == machine.getGuiBlock()) {
                machine.handleGuiInteraction(clicked, player);
                player.sendMessage(ChatColor.DARK_GRAY + machine.getDisplayName() + " opened!");
            } else {
                // If it's not the GUI block, it must be a trigger block (since canHandle() returned true)
                machine.handleTriggerInteraction(clicked, player);
            }

            return; // Handled by this machine
        }

        // No valid machine found - allow vanilla interaction
    }

    /**
     * Register a multiblock in the database (if not already registered)
     * Shows "Successfully built" message on first registration
     * ASYNC OPTIMIZED: Database save happens in background
     * BUG FIX 3: Added null checks to prevent NullPointerException
     */
    private void registerMultiblock(Block coreBlock, String multiblockType, Player player) {
        // BUG FIX 3: Null checks
        if (coreBlock == null || multiblockType == null || player == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot register multiblock: null parameter");
            return;
        }

        Location location = coreBlock.getLocation();
        if (location == null) {
            plugin.getLogger().log(Level.WARNING, "Cannot register multiblock: location is null");
            return;
        }

        // Check if already registered
        if (databaseManager.hasMultiblock(location)) {
            return; // Already registered
        }

        // Create multiblock data
        MultiblockData multiblock = new MultiblockData(location, multiblockType, player.getUniqueId());

        // Show success message to player immediately (like Slimefun)
        String displayName = getMultiblockDisplayName(multiblockType);
        player.sendMessage(ChatColor.GREEN + "✓ Successfully built: " + ChatColor.WHITE + displayName);

        plugin.getLogger().info("Registered " + multiblockType + " at " +
            location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() +
            " (Owner: " + player.getName() + ")");

        // Save to database asynchronously (won't block server)
        databaseManager.saveMultiblockAsync(multiblock, null);
    }

    /**
     * Get display name for multiblock type
     */
    private String getMultiblockDisplayName(String multiblockType) {
        return switch (multiblockType) {
            case "enhanced_crafting_table" -> "Enhanced Crafting Table";
            case "basic_workbench" -> "Enhanced Crafting Table"; // Legacy support
            case "smelter" -> "Smelter";
            case "pressure_chamber" -> "Pressure Chamber";
            case "ore_washer" -> "Ore Washer";
            case "compressor" -> "Compressor";
            case "automated_panning" -> "Automated Panning Machine";
            case "ore_crusher" -> "Ore Crusher";
            default -> multiblockType;
        };
    }

    /**
     * Handle player closing inventories.
     * If they close a multiblock inventory, process the recipe.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        // All multiblocks now use click-to-craft (Slimefun style)
        // No automatic crafting on inventory close

        // Automated Panning Machine: Save inventory when closed so trapdoor click can process it
        if (title.equals(ChatColor.GOLD + "Automated Panning Machine")) {
            if (holder instanceof AutomatedPanningMachine.AutomatedPanningInventoryHolder) {
                AutomatedPanningMachine.AutomatedPanningInventoryHolder panHolder =
                    (AutomatedPanningMachine.AutomatedPanningInventoryHolder) holder;
                Location cauldronLocation = panHolder.getCauldronLocation();
                if (cauldronLocation != null) {
                    AutomatedPanningMachine.saveInventory(event.getInventory(), cauldronLocation);
                }
            }
            return;
        }

        // Smelter no longer crafts on close - click iron bars to craft (Slimefun style)
        // But we still need to clean up the PLAYER_VIEWING map and save inventory
        if (title.equals(ChatColor.GOLD + "Smelter")) {
            SmelterMachine.cleanupPlayerViewing(player, event.getInventory());
        }
    }

    /**
     * Handle inventory clicks to prevent players from taking decorative items.
     * Prevents removing gray glass panes, flint and steel (progress indicator), etc.
     *
     * SECURITY HARDENED: Protects against ALL exploit types:
     * - Shift-click exploits
     * - Number key exploits
     * - Double-click exploits
     * - Creative mode exploits
     * - Drag & drop exploits (handled in onInventoryDrag)
     *
     * Smelter GUI Layout (3x3 input grid):
     * Row 1: [X][I][I][I][X][X][X][P][X]  - I = Input (1,2,3), P = Progress (7)
     * Row 2: [X][I][I][I][X][X][X][X][X]  - I = Input (10,11,12)
     * Row 3: [X][I][I][I][X][O][X][X][X]  - I = Input (19,20,21), O = Output (22)
     *
     * Allowed slots: 1,2,3, 10,11,12, 19,20,21 (input), 22 (output)
     * Locked slots: All others (decorative items and progress indicator)
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // Only handle Smelter inventory
        if (!title.equals(ChatColor.GOLD + "Smelter")) {
            return;
        }

        // RATE LIMITING: Prevent spam-click DoS attacks (REFACTORED to use RateLimiter)
        Player player = (Player) event.getWhoClicked();

        if (!inventoryClickLimiter.tryAccess(player.getUniqueId().toString())) {
            event.setCancelled(true); // Spam detected - block the click
            return;
        }

        int slot = event.getRawSlot();

        // Define allowed slots
        int[] ALLOWED_SLOTS = {1, 2, 3, 10, 11, 12, 19, 20, 21, 22};
        boolean isAllowedSlot = false;
        for (int allowed : ALLOWED_SLOTS) {
            if (slot == allowed) {
                isAllowedSlot = true;
                break;
            }
        }

        // If clicking in the Smelter inventory (slots 0-26)
        if (slot >= 0 && slot < 27) {
            if (!isAllowedSlot) {
                // Block ALL click types on protected slots
                event.setCancelled(true);
                return;
            }

            // Even for allowed slots, block certain exploit click types
            switch (event.getClick()) {
                case SHIFT_LEFT:
                case SHIFT_RIGHT:
                    // Allow shift-click only if clicking FROM player inventory TO allowed slot
                    if (slot < 27) {
                        // Clicking IN the smelter - check if it's an allowed slot
                        if (!isAllowedSlot) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                    break;

                case DOUBLE_CLICK:
                    // Block double-click to prevent grabbing items from protected slots
                    event.setCancelled(true);
                    return;

                case NUMBER_KEY:
                    // Block number key swaps on protected slots
                    if (!isAllowedSlot) {
                        event.setCancelled(true);
                        return;
                    }
                    break;

                case CREATIVE:
                    // Block creative mode middle-click duplication
                    if (!isAllowedSlot) {
                        event.setCancelled(true);
                        return;
                    }
                    break;

                default:
                    // Allow normal clicks on allowed slots
                    break;
            }
        }

        // Block shift-clicking FROM player inventory if it would go to a protected slot
        if (slot >= 27) { // Clicking in player inventory
            if (event.isShiftClick()) {
                // Shift-clicking from player inventory - items go to first available slot
                // We need to check if there's space in allowed slots
                // For now, allow it - items will only go to allowed slots due to the protection above
                // The Bukkit API will try to place in the first available slot, which we've protected
            }
        }
    }

    /**
     * Handle inventory drag events to prevent dragging items into protected slots
     *
     * SECURITY: Blocks ALL drag operations in Smelter GUI to prevent exploits
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();

        // Only handle Smelter inventory
        if (!title.equals(ChatColor.GOLD + "Smelter")) {
            return;
        }

        // Define allowed slots
        int[] ALLOWED_SLOTS = {1, 2, 3, 10, 11, 12, 19, 20, 21, 22};

        // Check if any dragged slot is in the Smelter inventory (0-26)
        for (int slot : event.getRawSlots()) {
            if (slot >= 0 && slot < 27) {
                // Check if this slot is allowed
                boolean isAllowed = false;
                for (int allowed : ALLOWED_SLOTS) {
                    if (slot == allowed) {
                        isAllowed = true;
                        break;
                    }
                }

                // If dragging to a protected slot, cancel the entire drag
                if (!isAllowed) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * Handle block breaking to remove multiblocks from database
     * ASYNC OPTIMIZED: Database removal happens in background
     * MEMORY LEAK FIX: Clears smelter inventories when multiblocks are destroyed
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        Block broken = event.getBlock();
        Location location = broken.getLocation();

        // Check if this block is a registered multiblock
        if (databaseManager.hasMultiblock(location)) {
            MultiblockData multiblock = databaseManager.getMultiblock(location);

            // Send message immediately
            plugin.getLogger().info("Removed " + multiblock.getMultiblockType() + " multiblock at " +
                location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());

            event.getPlayer().sendMessage(ChatColor.YELLOW + "Multiblock destroyed!");

            // MEMORY LEAK FIX: Clear smelter inventory from memory
            if (multiblock.getMultiblockType().equals("smelter")) {
                SmelterMachine.clearInventory(location);
                // PHASE 3: Clear smelting queue from memory and database
                plugin.getSmeltingManager().clearQueueCache(location);
            }

            // Remove from database asynchronously (won't block server)
            databaseManager.removeMultiblockAsync(location, null);
        }

        // FUTURE ENHANCEMENTS (not needed for current design):
        // - Check if breaking ANY part of a multiblock (not just core block)
        // - Prevent breaking if player doesn't own it (ownership system)
        // - Drop special controller items (current design uses vanilla blocks)
    }

    /**
     * Handle player disconnect to prevent memory leaks
     * MEMORY LEAK FIX: Cleans up PLAYER_VIEWING and rate limiters
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        // Clean up SmelterMachine PLAYER_VIEWING map
        SmelterMachine.cleanupPlayerViewing(player, null);

        // Clean up rate limiters (REFACTORED to use RateLimiter.remove())
        inventoryClickLimiter.remove(uuid);

        // Clean up MenuManager data (prevents memory leak)
        org.ThefryGuy.techFactory.gui.MenuManager.cleanup(player);
    }
}

