package org.ThefryGuy.techFactory.registry;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Logger;

/**
 * Central registry for all multiblock machines
 * 
 * BENEFITS:
 * - No more hardcoded if/if/if chains in MultiblockListener
 * - Adding a new machine = 1 line of code
 * - Consistent behavior across all machines
 * - Easy to test and maintain
 * 
 * PATTERN: Similar to Slimefun's SlimefunItem registry
 * 
 * USAGE:
 * ```java
 * // In TechFactory.onEnable():
 * MachineRegistry.register(new SmelterMachineHandler());
 * MachineRegistry.register(new OreCrusherMachineHandler());
 * 
 * // In MultiblockListener:
 * MultiblockMachine machine = MachineRegistry.getMachineForBlock(clickedBlock.getType());
 * if (machine != null && machine.isValidStructure(clickedBlock)) {
 *     machine.handleGuiInteraction(clickedBlock, player);
 * }
 * ```
 */
public class MachineRegistry {
    
    // Map of machine type -> machine handler
    private static final Map<String, MultiblockMachine> machines = new LinkedHashMap<>();
    
    // Map of block material -> list of machines that use it
    // This allows fast lookups when player clicks a block
    private static final Map<Material, List<MultiblockMachine>> blockToMachines = new HashMap<>();
    
    /**
     * Register a multiblock machine
     *
     * BUG FIX: Now registers ALL materials that the machine can handle,
     * not just the primary GUI and trigger blocks. This fixes issues with
     * machines that accept multiple block types (e.g., all trapdoor types).
     *
     * @param machine The machine to register
     */
    public static void register(MultiblockMachine machine) {
        String type = machine.getMachineType();

        if (machines.containsKey(type)) {
            throw new IllegalArgumentException("Machine type already registered: " + type);
        }

        machines.put(type, machine);

        // Register ALL materials that this machine can handle
        // This is important for machines that accept multiple block types
        // (e.g., Automated Panning Machine accepts all trapdoor types)
        for (Material material : Material.values()) {
            if (machine.canHandle(material)) {
                blockToMachines.computeIfAbsent(material, k -> new ArrayList<>()).add(machine);
            }
        }
    }
    
    /**
     * Get a machine by its type identifier
     * 
     * @param machineType The machine type (e.g., "smelter")
     * @return The machine, or null if not found
     */
    public static MultiblockMachine getMachine(String machineType) {
        return machines.get(machineType);
    }
    
    /**
     * Get all machines that can handle a specific block type
     * Used for quick filtering when player clicks a block
     * 
     * @param material The block material
     * @return List of machines that use this block, or empty list
     */
    public static List<MultiblockMachine> getMachinesForBlock(Material material) {
        return blockToMachines.getOrDefault(material, Collections.emptyList());
    }
    
    /**
     * Find the first valid machine at a block location
     * Checks all machines that can handle this block type
     * 
     * @param block The block that was clicked
     * @return The first valid machine, or null if none found
     */
    public static MultiblockMachine findValidMachine(Block block) {
        List<MultiblockMachine> candidates = getMachinesForBlock(block.getType());
        
        for (MultiblockMachine machine : candidates) {
            if (machine.isValidStructure(block)) {
                return machine;
            }
        }
        
        return null;
    }
    
    /**
     * Handle GUI interaction for a block
     * Finds the valid machine and calls its GUI handler
     * 
     * @param block The block that was clicked
     * @param player The player who clicked
     * @return true if a machine handled the interaction
     */
    public static boolean handleGuiInteraction(Block block, Player player) {
        MultiblockMachine machine = findValidMachine(block);
        
        if (machine != null && block.getType() == machine.getGuiBlock()) {
            machine.handleGuiInteraction(block, player);
            return true;
        }
        
        return false;
    }
    
    /**
     * Handle trigger interaction for a block
     * Finds the valid machine and calls its trigger handler
     * 
     * @param block The block that was clicked
     * @param player The player who clicked
     * @return true if a machine handled the interaction
     */
    public static boolean handleTriggerInteraction(Block block, Player player) {
        MultiblockMachine machine = findValidMachine(block);
        
        if (machine != null && block.getType() == machine.getTriggerBlock()) {
            machine.handleTriggerInteraction(block, player);
            return true;
        }
        
        return false;
    }
    
    /**
     * Get all registered machines
     * 
     * @return Collection of all machines
     */
    public static Collection<MultiblockMachine> getAllMachines() {
        return Collections.unmodifiableCollection(machines.values());
    }
    
    /**
     * Get count of registered machines
     * 
     * @return Number of registered machines
     */
    public static int getMachineCount() {
        return machines.size();
    }
    
    /**
     * Initialize the machine registry
     * Logs all registered machines
     * 
     * @param logger The logger to use
     */
    public static void initialize(Logger logger) {
        logger.info("Machine Registry initialized with " + machines.size() + " machines:");
        for (MultiblockMachine machine : machines.values()) {
            logger.info("  - " + machine.getDisplayName() + " (" + machine.getMachineType() + ")");
        }
    }
    
    /**
     * Clear all registered machines
     * Used for testing or plugin reload
     */
    public static void clear() {
        machines.clear();
        blockToMachines.clear();
    }
}

