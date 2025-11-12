package org.ThefryGuy.techFactory.registry.handlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.registry.MultiblockMachine;
import org.ThefryGuy.techFactory.workstations.multiblocks.SmelterMachine;

/**
 * Machine handler for the Smelter multiblock
 * 
 * Structure:
 * [Iron Bars]      ← Trigger block (click to craft)
 * [Blast Furnace]  ← GUI block (click to open inventory)
 * [Campfire]       ← Base
 */
public class SmelterMachineHandler implements MultiblockMachine {
    
    private final TechFactory plugin;
    
    public SmelterMachineHandler(TechFactory plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getMachineType() {
        return "smelter";
    }
    
    @Override
    public String getDisplayName() {
        return "Smelter";
    }
    
    @Override
    public Material getGuiBlock() {
        return Material.BLAST_FURNACE;
    }
    
    @Override
    public Material getTriggerBlock() {
        return Material.IRON_BARS;
    }
    
    @Override
    public boolean isValidStructure(Block block) {
        // If clicking iron bars, check the blast furnace below
        if (block.getType() == Material.IRON_BARS) {
            Block blastFurnace = block.getRelative(0, -1, 0);
            return SmelterMachine.isValidStructure(blastFurnace);
        }
        
        // If clicking blast furnace directly
        return SmelterMachine.isValidStructure(block);
    }
    
    @Override
    public void handleGuiInteraction(Block block, Player player) {
        // Player clicked the blast furnace - open GUI
        SmelterMachine.openInventory(block, player);
    }
    
    @Override
    public void handleTriggerInteraction(Block block, Player player) {
        // Player clicked the iron bars - process crafting
        Block blastFurnace = block.getRelative(0, -1, 0);
        SmelterMachine.processCraft(player, blastFurnace, plugin);
    }
}

