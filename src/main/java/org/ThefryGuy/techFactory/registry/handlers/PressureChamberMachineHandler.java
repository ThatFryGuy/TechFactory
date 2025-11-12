package org.ThefryGuy.techFactory.registry.handlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.registry.MultiblockMachine;
import org.ThefryGuy.techFactory.workstations.multiblocks.PressureChamberMachine;

/**
 * Machine handler for the Pressure Chamber multiblock
 * 
 * Structure:
 * [Dispenser ↓]  ← GUI block (click to open inventory)
 * [Glass]        ← Trigger block (click to craft)
 * [Cauldron]     ← Base
 */
public class PressureChamberMachineHandler implements MultiblockMachine {
    
    private final TechFactory plugin;
    
    public PressureChamberMachineHandler(TechFactory plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getMachineType() {
        return "pressure_chamber";
    }
    
    @Override
    public String getDisplayName() {
        return "Pressure Chamber";
    }
    
    @Override
    public Material getGuiBlock() {
        return Material.DISPENSER;
    }
    
    @Override
    public Material getTriggerBlock() {
        return Material.GLASS;
    }
    
    @Override
    public boolean isValidStructure(Block block) {
        // If clicking glass, check the dispenser above
        if (block.getType() == Material.GLASS) {
            Block dispenser = block.getRelative(0, 1, 0);
            return PressureChamberMachine.isValidStructure(dispenser);
        }
        
        // If clicking dispenser directly
        return PressureChamberMachine.isValidStructure(block);
    }
    
    @Override
    public void handleGuiInteraction(Block block, Player player) {
        // Player clicked the dispenser - open its inventory
        if (block.getState() instanceof Dispenser) {
            Dispenser dispenser = (Dispenser) block.getState();
            player.openInventory(dispenser.getInventory());
        }
    }
    
    @Override
    public void handleTriggerInteraction(Block block, Player player) {
        // Player clicked the glass - process crafting
        Block dispenserBlock = block.getRelative(0, 1, 0);
        
        if (dispenserBlock.getState() instanceof Dispenser) {
            Dispenser dispenser = (Dispenser) dispenserBlock.getState();
            PressureChamberMachine.processRecipe(dispenser, player);
        }
    }
}

