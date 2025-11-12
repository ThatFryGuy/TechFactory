package org.ThefryGuy.techFactory.registry.handlers;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.data.PlacedBlock;
import org.ThefryGuy.techFactory.energy.EnergyManager;
import org.ThefryGuy.techFactory.energy.EnergyNetwork;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.registry.ElectricMachine;
import org.ThefryGuy.techFactory.machines.electric.ElectricGoldPanMachine;

/**
 * Electric Gold Pan handler - Registry pattern
 *
 * This is a small handler class that implements the ElectricMachine interface.
 * All the actual implementation logic is in ElectricGoldPanMachine.java
 *
 * Functionality:
 * - Automated panning of gravel, soul sand, and soul soil
 * - Combines Gold Pan and Nether Gold Pan functionality
 * - Speed: 1.0x (10 seconds per item, same as vanilla furnace)
 * - Energy cost: 4 J per item (same as Electric Furnace)
 * - Must be connected to energy network within 6 blocks
 */
public class ElectricGoldPanHandler implements ElectricMachine {

    private static final int ENERGY_PER_ITEM = 4;
    private static final int PROCESSING_TIME_TICKS = 200; // 10 seconds (200 ticks)
    
    @Override
    public String getMachineType() {
        return "electric_gold_pan";
    }
    
    @Override
    public String getDisplayName() {
        return "Electric Gold Pan";
    }
    
    @Override
    public Material getBlockMaterial() {
        return Material.BROWN_TERRACOTTA;
    }
    
    @Override
    public int getEnergyCost() {
        return ENERGY_PER_ITEM;
    }
    
    @Override
    public int getProcessingTime() {
        return PROCESSING_TIME_TICKS;
    }
    
    @Override
    public void handlePlacement(Block block, Player player, TechFactory plugin) {
        Location location = block.getLocation();
        
        // Save to database (synchronous to ensure it's available immediately)
        PlacedBlock placedBlock = new PlacedBlock(location, getMachineType(), player.getUniqueId());
        plugin.getDatabaseManager().saveBlockAsync(placedBlock, null);
        
        // Success message
        player.sendMessage(ChatColor.GREEN + "✓ " + getDisplayName() + " placed!");
        
        // Check if connected to energy network
        EnergyManager energyManager = plugin.getEnergyManager();
        EnergyNetwork network = energyManager.findNearestNetwork(location, 6.0);
        
        if (network == null) {
            player.sendMessage(ChatColor.YELLOW + "⚠ Not connected to energy network!");
            player.sendMessage(ChatColor.GRAY + "Place an Energy Regulator or Connector within 6 blocks");
        } else {
            // Connect to network as a consumer
            network.connectConsumer(location);
            energyManager.registerDeviceToNetwork(location, network);
            
            player.sendMessage(ChatColor.GREEN + "✓ Connected to energy network!");
            player.sendMessage(ChatColor.GRAY + "Network: " + network.getStoredEnergy() + " / " +
                             network.getMaxCapacity() + " J (" + network.getFillPercentage() + "%)");
            
            // Play connection sound
            if (location.getWorld() != null) {
                location.getWorld().playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.5f);
            }
        }
    }
    
    @Override
    public void handleInteraction(Block block, Player player, TechFactory plugin) {
        // Open Electric Gold Pan GUI
        ElectricGoldPanMachine.openInventory(block, player, plugin);
    }
    
    @Override
    public void handleBreak(Block block, Player player, TechFactory plugin) {
        Location location = block.getLocation();

        // Drop items from inventory first
        dropInventoryItems(location, plugin);

        // Disconnect from energy network
        EnergyManager energyManager = plugin.getEnergyManager();
        EnergyNetwork network = energyManager.getNetworkByLocation(location);
        if (network != null) {
            network.disconnectConsumer(location);
            energyManager.unregisterDevice(location);
        }

        // Remove from database
        plugin.getDatabaseManager().removeBlock(location);

        // Remove inventory
        ElectricGoldPanMachine.removeMachine(location);

        // Drop Electric Gold Pan item
        location.getWorld().dropItemNaturally(location, getRecipeItem().getItemStack());

        player.sendMessage(ChatColor.YELLOW + getDisplayName() + " removed!");
    }
    
    @Override
    public void dropInventoryItems(Location location, TechFactory plugin) {
        ElectricGoldPanMachine.dropInventoryItems(location, plugin);
    }
    
    @Override
    public RecipeItem getRecipeItem() {
        return new org.ThefryGuy.techFactory.recipes.workstations.electric.ElectricGoldPan();
    }
}

