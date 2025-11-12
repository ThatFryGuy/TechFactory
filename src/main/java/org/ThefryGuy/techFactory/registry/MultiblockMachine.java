package org.ThefryGuy.techFactory.registry;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Interface for all multiblock machines in TechFactory
 * 
 * This allows the MultiblockListener to handle all machines generically
 * instead of hardcoded if/if/if chains for each machine type.
 * 
 * BENEFITS:
 * - Adding a new machine = 1 line of code in TechFactory.onEnable()
 * - No need to edit MultiblockListener for new machines
 * - Consistent behavior across all machines
 * - Easy to test and maintain
 * 
 * PATTERN: Similar to Slimefun's SlimefunItem system
 */
public interface MultiblockMachine {
    
    /**
     * Get the unique machine type identifier
     * Used for database storage and registry lookups
     * 
     * Examples: "smelter", "ore_crusher", "pressure_chamber"
     */
    String getMachineType();
    
    /**
     * Get the display name shown to players
     * 
     * Examples: "Smelter", "Ore Crusher", "Pressure Chamber"
     */
    String getDisplayName();
    
    /**
     * Get the block type that opens the GUI
     * Player right-clicks this block to access the machine's inventory
     * 
     * Examples: BLAST_FURNACE (Smelter), DISPENSER (Ore Crusher), CAULDRON (Panning)
     */
    Material getGuiBlock();
    
    /**
     * Get the block type that triggers crafting
     * Player right-clicks this block to process items (Slimefun style)
     * 
     * Examples: IRON_BARS (Smelter), NETHER_BRICK_FENCE (Ore Crusher), TRAPDOOR (Panning)
     * 
     * Can return null if machine doesn't have a separate trigger block
     */
    Material getTriggerBlock();
    
    /**
     * Check if the multiblock structure is valid
     * Called when player right-clicks a block
     * 
     * @param block The block that was clicked (GUI block or trigger block)
     * @return true if this is a valid multiblock structure
     */
    boolean isValidStructure(Block block);
    
    /**
     * Handle GUI block interaction
     * Called when player right-clicks the GUI block
     * 
     * @param block The GUI block that was clicked
     * @param player The player who clicked
     */
    void handleGuiInteraction(Block block, Player player);
    
    /**
     * Handle trigger block interaction
     * Called when player right-clicks the trigger block
     * 
     * @param block The trigger block that was clicked
     * @param player The player who clicked
     */
    void handleTriggerInteraction(Block block, Player player);
    
    /**
     * Check if this machine can handle the given block type
     * Used for quick filtering before structure validation
     * 
     * @param material The block material
     * @return true if this machine uses this block type
     */
    default boolean canHandle(Material material) {
        return material == getGuiBlock() || material == getTriggerBlock();
    }
}

