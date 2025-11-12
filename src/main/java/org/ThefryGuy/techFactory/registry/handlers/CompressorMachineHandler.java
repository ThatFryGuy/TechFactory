package org.ThefryGuy.techFactory.registry.handlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.registry.MultiblockMachine;
import org.ThefryGuy.techFactory.util.ItemUtils;
import org.ThefryGuy.techFactory.workstations.multiblocks.CompressorMachine;

/**
 * Machine handler for the Compressor multiblock
 * 
 * Structure:
 * [Dispenser]  ← GUI block (click to open inventory)
 * [Fence]      ← Trigger block (click to craft)
 * [Cauldron]   ← Base
 */
public class CompressorMachineHandler implements MultiblockMachine {
    
    private final TechFactory plugin;
    
    public CompressorMachineHandler(TechFactory plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getMachineType() {
        return "compressor";
    }
    
    @Override
    public String getDisplayName() {
        return "Compressor";
    }
    
    @Override
    public Material getGuiBlock() {
        return Material.DISPENSER;
    }
    
    @Override
    public Material getTriggerBlock() {
        // Note: CompressorMachine accepts any fence type
        // We return OAK_FENCE as the primary, but canHandle() will check all fence types
        return Material.OAK_FENCE;
    }
    
    @Override
    public boolean isValidStructure(Block block) {
        // If clicking fence, pass the fence to isValidStructure
        if (ItemUtils.isFenceType(block.getType())) {
            return CompressorMachine.isValidStructure(block);
        }

        // If clicking dispenser directly, check the fence above
        if (block.getType() == Material.DISPENSER) {
            Block fence = block.getRelative(0, 1, 0);
            return CompressorMachine.isValidStructure(fence);
        }

        return false;
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
        // Dispenser is BELOW the fence
        Block dispenserBlock = block.getRelative(0, -1, 0);

        if (dispenserBlock.getState() instanceof Dispenser) {
            Dispenser dispenser = (Dispenser) dispenserBlock.getState();
            CompressorMachine.processRecipe(dispenser, player);
        }
    }
    
    @Override
    public boolean canHandle(Material material) {
        // Override to handle all fence types
        return material == getGuiBlock() || ItemUtils.isFenceType(material);
    }
}

