package org.ThefryGuy.techFactory.registry.handlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.registry.MultiblockMachine;
import org.ThefryGuy.techFactory.workstations.multiblocks.BasicWorkbenchMachine;

/**
 * Machine handler for the Enhanced Crafting Table multiblock
 * 
 * Structure:
 * [Crafting Table]  ← Both GUI and trigger block (click to craft)
 * [Dispenser]       ← Storage
 */
public class EnhancedCraftingTableMachineHandler implements MultiblockMachine {
    
    private final TechFactory plugin;
    
    public EnhancedCraftingTableMachineHandler(TechFactory plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getMachineType() {
        return "enhanced_crafting_table";
    }
    
    @Override
    public String getDisplayName() {
        return "Enhanced Crafting Table";
    }
    
    @Override
    public Material getGuiBlock() {
        return Material.CRAFTING_TABLE;
    }
    
    @Override
    public Material getTriggerBlock() {
        // Same as GUI block - crafting table handles both
        return Material.CRAFTING_TABLE;
    }
    
    @Override
    public boolean isValidStructure(Block block) {
        return BasicWorkbenchMachine.isValidStructure(block);
    }
    
    @Override
    public void handleGuiInteraction(Block block, Player player) {
        // For Enhanced Crafting Table, clicking the crafting table processes the recipe
        // (It doesn't have a separate GUI - uses the dispenser below for storage)
        Block dispenserBlock = block.getRelative(0, -1, 0);
        if (dispenserBlock.getType() == Material.DISPENSER) {
            org.bukkit.block.Dispenser dispenser = (org.bukkit.block.Dispenser) dispenserBlock.getState();
            BasicWorkbenchMachine.processRecipe(dispenser, player);
        }
    }

    @Override
    public void handleTriggerInteraction(Block block, Player player) {
        // Same as GUI interaction for this machine
        Block dispenserBlock = block.getRelative(0, -1, 0);
        if (dispenserBlock.getType() == Material.DISPENSER) {
            org.bukkit.block.Dispenser dispenser = (org.bukkit.block.Dispenser) dispenserBlock.getState();
            BasicWorkbenchMachine.processRecipe(dispenser, player);
        }
    }
}

