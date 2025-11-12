package org.ThefryGuy.techFactory.registry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.ThefryGuy.techFactory.TechFactory;

/**
 * Interface for all electric machines in TechFactory
 * 
 * This allows the ElectricMachineListener to handle all machines generically
 * instead of having separate listeners for each machine type.
 * 
 * BENEFITS:
 * - Adding a new electric machine = 1 line of code in TechFactory.onEnable()
 * - No need to create a new listener for each machine
 * - Consistent behavior across all machines
 * - Easy to test and maintain
 * 
 * PATTERN: Similar to MultiblockMachine interface
 */
public interface ElectricMachine {
    
    /**
     * Get the unique machine type identifier
     * Used for database storage and registry lookups
     * 
     * Examples: "electric_furnace", "electric_grinder", "electric_compressor"
     */
    String getMachineType();
    
    /**
     * Get the display name shown to players
     * 
     * Examples: "Electric Furnace", "Electric Grinder", "Electric Compressor"
     */
    String getDisplayName();
    
    /**
     * Get the block material used for this machine
     * 
     * Examples: FURNACE, BLAST_FURNACE, SMOKER, etc.
     */
    Material getBlockMaterial();
    
    /**
     * Get the energy cost per operation
     * 
     * @return Energy cost in Joules
     */
    int getEnergyCost();
    
    /**
     * Get the processing time per operation (in ticks)
     * 
     * @return Processing time in ticks (20 ticks = 1 second)
     */
    int getProcessingTime();
    
    /**
     * Handle machine placement
     * Called when player places this machine block
     * 
     * @param block The block that was placed
     * @param player The player who placed it
     * @param plugin The plugin instance
     */
    void handlePlacement(Block block, Player player, TechFactory plugin);
    
    /**
     * Handle machine interaction (right-click)
     * Called when player right-clicks the machine
     * 
     * @param block The block that was clicked
     * @param player The player who clicked
     * @param plugin The plugin instance
     */
    void handleInteraction(Block block, Player player, TechFactory plugin);
    
    /**
     * Handle machine breaking
     * Called when player breaks this machine block
     * 
     * @param block The block being broken
     * @param player The player who broke it
     * @param plugin The plugin instance
     */
    void handleBreak(Block block, Player player, TechFactory plugin);
    
    /**
     * Drop inventory items when machine is broken
     * 
     * @param location The location of the machine
     * @param plugin The plugin instance
     */
    void dropInventoryItems(Location location, TechFactory plugin);
    
    /**
     * Get the recipe item for this machine (for crafting/giving)
     * 
     * @return The RecipeItem instance
     */
    org.ThefryGuy.techFactory.recipes.RecipeItem getRecipeItem();
}

