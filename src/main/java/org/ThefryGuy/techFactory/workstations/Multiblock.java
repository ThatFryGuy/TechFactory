package org.ThefryGuy.techFactory.workstations;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Base interface for all multiblock workstations.
 * 
 * A multiblock is a structure built from multiple blocks in the world.
 * Examples: Basic Workbench (Crafting Table on Dispenser), Sifter, Blast Furnace, etc.
 * 
 * This interface defines the common functionality all multiblocks need:
 * - Structure validation (is it built correctly?)
 * - Player interaction (what happens when clicked?)
 * - Breaking (what happens when destroyed?)
 */
public interface Multiblock {

    /**
     * Get the unique ID for this multiblock type.
     * Should match the RecipeItem ID.
     * 
     * @return The multiblock ID (e.g. "basic_workbench", "sifter")
     */
    String getId();

    /**
     * Check if a valid multiblock structure exists at this location.
     * 
     * @param centerBlock The center/controller block
     * @return true if the structure is valid
     */
    boolean isValidStructure(Block centerBlock);

    /**
     * Handle player interaction with the multiblock.
     * Called when a player right-clicks any part of the multiblock.
     * 
     * @param player The player who interacted
     * @param clickedBlock The block that was clicked
     * @param centerLocation The center/controller location
     */
    void onInteract(Player player, Block clickedBlock, Location centerLocation);

    /**
     * Handle multiblock being broken.
     * Called when any part of the multiblock is destroyed.
     * 
     * @param centerLocation The center/controller location
     * @param player The player who broke it (can be null)
     */
    void onBreak(Location centerLocation, Player player);

    /**
     * Get the display name for this multiblock.
     * 
     * @return The display name (e.g. "Basic Workbench")
     */
    String getDisplayName();
}

