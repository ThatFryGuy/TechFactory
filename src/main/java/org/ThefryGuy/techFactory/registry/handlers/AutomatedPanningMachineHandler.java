package org.ThefryGuy.techFactory.registry.handlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.registry.MultiblockMachine;
import org.ThefryGuy.techFactory.workstations.multiblocks.AutomatedPanningMachine;

/**
 * Machine handler for the Automated Panning Machine multiblock
 * 
 * Structure:
 * [Trapdoor]  ← Trigger block (click to craft)
 * [Cauldron]  ← GUI block (click to open inventory)
 */
public class AutomatedPanningMachineHandler implements MultiblockMachine {
    
    private final TechFactory plugin;
    
    public AutomatedPanningMachineHandler(TechFactory plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getMachineType() {
        return "automated_panning";
    }
    
    @Override
    public String getDisplayName() {
        return "Automated Panning Machine";
    }
    
    @Override
    public Material getGuiBlock() {
        return Material.CAULDRON;
    }
    
    @Override
    public Material getTriggerBlock() {
        // Note: AutomatedPanningMachine accepts any trapdoor type
        // We return OAK_TRAPDOOR as the primary, but canHandle() will check all trapdoor types
        return Material.OAK_TRAPDOOR;
    }
    
    @Override
    public boolean isValidStructure(Block block) {
        // If clicking trapdoor, check the cauldron below
        if (isTrapdoor(block.getType())) {
            Block cauldron = block.getRelative(0, -1, 0);
            return AutomatedPanningMachine.isValidStructure(cauldron);
        }
        
        // If clicking cauldron directly
        return AutomatedPanningMachine.isValidStructure(block);
    }
    
    @Override
    public void handleGuiInteraction(Block block, Player player) {
        // Player clicked the cauldron - open GUI
        AutomatedPanningMachine.openInventory(block, player);
    }
    
    @Override
    public void handleTriggerInteraction(Block block, Player player) {
        // Player clicked the trapdoor - process crafting
        Block cauldron = block.getRelative(0, -1, 0);
        AutomatedPanningMachine.processCraft(player, cauldron);
    }
    
    @Override
    public boolean canHandle(Material material) {
        // Override to handle all trapdoor types
        return material == getGuiBlock() || isTrapdoor(material);
    }
    
    /**
     * Check if a material is a trapdoor type
     */
    private boolean isTrapdoor(Material material) {
        return material == Material.OAK_TRAPDOOR
            || material == Material.SPRUCE_TRAPDOOR
            || material == Material.BIRCH_TRAPDOOR
            || material == Material.JUNGLE_TRAPDOOR
            || material == Material.ACACIA_TRAPDOOR
            || material == Material.DARK_OAK_TRAPDOOR
            || material == Material.CRIMSON_TRAPDOOR
            || material == Material.WARPED_TRAPDOOR
            || material == Material.IRON_TRAPDOOR;
    }
}

