package org.ThefryGuy.techFactory.registry.handlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.registry.MultiblockMachine;
import org.ThefryGuy.techFactory.workstations.multiblocks.OreCrusherMachine;

/**
 * Machine handler for the Ore Crusher multiblock
 * 
 * Structure:
 * [Nether Brick Fence]  ← Trigger block (click to craft)
 * [Dispenser ↑]         ← GUI block (click to open inventory)
 * [Iron Bars] [Iron Bars] ← Sides (X or Z axis)
 */
public class OreCrusherMachineHandler implements MultiblockMachine {
    
    private final TechFactory plugin;
    
    public OreCrusherMachineHandler(TechFactory plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getMachineType() {
        return "ore_crusher";
    }
    
    @Override
    public String getDisplayName() {
        return "Ore Crusher";
    }
    
    @Override
    public Material getGuiBlock() {
        return Material.DISPENSER;
    }
    
    @Override
    public Material getTriggerBlock() {
        return Material.NETHER_BRICK_FENCE;
    }
    
    @Override
    public boolean isValidStructure(Block block) {
        // If clicking nether brick fence, check the dispenser below
        if (block.getType() == Material.NETHER_BRICK_FENCE) {
            Block dispenser = block.getRelative(0, -1, 0);
            return OreCrusherMachine.isValidStructure(dispenser);
        }
        
        // If clicking dispenser directly
        return OreCrusherMachine.isValidStructure(block);
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
        // Player clicked the nether brick fence - process crafting
        Block dispenserBlock = block.getRelative(0, -1, 0);
        Dispenser dispenser = OreCrusherMachine.getDispenser(dispenserBlock);
        
        if (dispenser != null) {
            OreCrusherMachine.processRecipe(dispenser, player);
        }
    }
}

