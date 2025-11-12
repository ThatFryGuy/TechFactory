package org.ThefryGuy.techFactory.registry.handlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.registry.MultiblockMachine;
import org.ThefryGuy.techFactory.util.ItemUtils;
import org.ThefryGuy.techFactory.workstations.multiblocks.OreWasherMachine;

/**
 * Machine handler for the Ore Washer multiblock
 * 
 * Structure:
 * [Dispenser]  ← GUI block (click to open inventory)
 * [Fence]      ← Trigger block (click to craft)
 * [Cauldron]   ← Base
 */
public class OreWasherMachineHandler implements MultiblockMachine {
    
    private final TechFactory plugin;
    
    public OreWasherMachineHandler(TechFactory plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getMachineType() {
        return "ore_washer";
    }
    
    @Override
    public String getDisplayName() {
        return "Ore Washer";
    }
    
    @Override
    public Material getGuiBlock() {
        return Material.DISPENSER;
    }
    
    @Override
    public Material getTriggerBlock() {
        // Note: OreWasherMachine accepts any fence type
        // We return OAK_FENCE as the primary, but canHandle() will check all fence types
        return Material.OAK_FENCE;
    }
    
    @Override
    public boolean isValidStructure(Block block) {
        // If clicking fence, check the dispenser above
        if (ItemUtils.isFenceType(block.getType())) {
            Block dispenser = block.getRelative(0, 1, 0);
            return OreWasherMachine.isValidStructure(dispenser);
        }

        // If clicking dispenser directly
        return OreWasherMachine.isValidStructure(block);
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
        // Player clicked the fence - process crafting
        Block dispenserBlock = block.getRelative(0, 1, 0);
        
        if (dispenserBlock.getState() instanceof Dispenser) {
            Dispenser dispenser = (Dispenser) dispenserBlock.getState();
            OreWasherMachine.processRecipe(dispenser, player);
        }
    }
    
    @Override
    public boolean canHandle(Material material) {
        // Override to handle all fence types
        return material == getGuiBlock() || ItemUtils.isFenceType(material);
    }
}

