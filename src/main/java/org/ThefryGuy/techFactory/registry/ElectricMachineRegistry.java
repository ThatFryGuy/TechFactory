package org.ThefryGuy.techFactory.registry;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.data.PlacedBlock;

import java.util.*;

/**
 * Central registry for all electric machines
 * 
 * BENEFITS:
 * - No more separate listeners for each machine type
 * - Adding a new machine = 1 line of code
 * - Consistent behavior across all machines
 * - Easy to test and maintain
 * 
 * PATTERN: Similar to MachineRegistry for multiblocks
 * 
 * USAGE:
 * ```java
 * // In TechFactory.onEnable():
 * ElectricMachineRegistry.register(new ElectricFurnaceHandler());
 * ElectricMachineRegistry.register(new ElectricGrinderHandler());
 * 
 * // In ElectricMachineListener:
 * ElectricMachine machine = ElectricMachineRegistry.getMachineForBlock(block.getType());
 * if (machine != null) {
 *     machine.handlePlacement(block, player, plugin);
 * }
 * ```
 */
public class ElectricMachineRegistry {
    
    // Map of machine type -> machine handler
    private static final Map<String, ElectricMachine> machines = new LinkedHashMap<>();
    
    // Map of block material -> machine handler
    // This allows fast lookups when player places/clicks a block
    private static final Map<Material, ElectricMachine> blockToMachine = new HashMap<>();
    
    /**
     * Register an electric machine
     * 
     * @param machine The machine to register
     */
    public static void register(ElectricMachine machine) {
        String type = machine.getMachineType();
        Material material = machine.getBlockMaterial();
        
        if (machines.containsKey(type)) {
            throw new IllegalArgumentException("Electric machine type already registered: " + type);
        }
        
        if (blockToMachine.containsKey(material)) {
            throw new IllegalArgumentException("Block material already registered to another machine: " + material);
        }
        
        machines.put(type, machine);
        blockToMachine.put(material, machine);
    }
    
    /**
     * Get a machine by its type identifier
     * 
     * @param machineType The machine type (e.g., "electric_furnace")
     * @return The machine, or null if not found
     */
    public static ElectricMachine getMachine(String machineType) {
        return machines.get(machineType);
    }
    
    /**
     * Get a machine by its block material
     * Used when player places/clicks a block
     * 
     * @param material The block material
     * @return The machine, or null if not registered
     */
    public static ElectricMachine getMachineForBlock(Material material) {
        return blockToMachine.get(material);
    }
    
    /**
     * Check if a block is a registered electric machine
     * 
     * @param block The block to check
     * @param plugin The plugin instance
     * @return The machine if it's a placed electric machine, null otherwise
     */
    public static ElectricMachine getPlacedMachine(Block block, TechFactory plugin) {
        if (block == null) {
            return null;
        }
        
        // Check if this block is in the database as a placed machine
        PlacedBlock placedBlock = plugin.getDatabaseManager().getBlock(block.getLocation());
        if (placedBlock == null) {
            return null; // Not a placed machine
        }
        
        // Get the machine handler for this type
        return getMachine(placedBlock.getBlockType());
    }
    
    /**
     * Handle machine placement
     * 
     * @param block The block that was placed
     * @param player The player who placed it
     * @param plugin The plugin instance
     * @return true if a machine handled the placement
     */
    public static boolean handlePlacement(Block block, Player player, TechFactory plugin) {
        ElectricMachine machine = getMachineForBlock(block.getType());
        
        if (machine != null) {
            machine.handlePlacement(block, player, plugin);
            return true;
        }
        
        return false;
    }
    
    /**
     * Handle machine interaction (right-click)
     * 
     * @param block The block that was clicked
     * @param player The player who clicked
     * @param plugin The plugin instance
     * @return true if a machine handled the interaction
     */
    public static boolean handleInteraction(Block block, Player player, TechFactory plugin) {
        ElectricMachine machine = getPlacedMachine(block, plugin);
        
        if (machine != null) {
            machine.handleInteraction(block, player, plugin);
            return true;
        }
        
        return false;
    }
    
    /**
     * Handle machine breaking
     * 
     * @param block The block being broken
     * @param player The player who broke it
     * @param plugin The plugin instance
     * @return true if a machine handled the break
     */
    public static boolean handleBreak(Block block, Player player, TechFactory plugin) {
        ElectricMachine machine = getPlacedMachine(block, plugin);
        
        if (machine != null) {
            machine.handleBreak(block, player, plugin);
            return true;
        }
        
        return false;
    }
    
    /**
     * Get all registered machines
     * 
     * @return Collection of all machines
     */
    public static Collection<ElectricMachine> getAllMachines() {
        return Collections.unmodifiableCollection(machines.values());
    }
    
    /**
     * Initialize the registry (called at plugin startup)
     * 
     * @param logger The plugin logger for logging
     */
    public static void initialize(java.util.logging.Logger logger) {
        if (logger != null) {
            logger.info("ElectricMachineRegistry: Registered " + machines.size() + " electric machines");
        }
    }
}

