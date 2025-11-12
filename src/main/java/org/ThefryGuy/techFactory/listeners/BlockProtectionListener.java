package org.ThefryGuy.techFactory.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.ThefryGuy.techFactory.TechFactory;
import org.ThefryGuy.techFactory.data.DatabaseManager;
import org.ThefryGuy.techFactory.data.PlacedBlock;
import org.ThefryGuy.techFactory.energy.EnergyManager;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.bukkit.inventory.ItemStack;

/**
 * GHOST BLOCK PREVENTION SYSTEM
 * 
 * Prevents ghost blocks (blocks in database but not in world) by:
 * 1. Protecting energy blocks from explosions
 * 2. Protecting energy blocks from pistons
 * 3. Validating blocks still exist before operations
 * 
 * This prevents the Slimefun ghost block plague!
 */
public class BlockProtectionListener implements Listener {

    private final TechFactory plugin;
    private final DatabaseManager databaseManager;
    private final EnergyManager energyManager;

    public BlockProtectionListener(TechFactory plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.energyManager = plugin.getEnergyManager();
    }

    /**
     * Prevent explosions from destroying energy blocks
     * Instead, drop the custom item and clean up properly
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled()) return;

        // Check each block in the explosion
        event.blockList().removeIf(block -> {
            try {
                Location location = block.getLocation();
                PlacedBlock placedBlock = databaseManager.getBlock(location);

                if (placedBlock == null) {
                    return false; // Not our block, allow explosion
                }

                // This is an energy block - handle it properly
                String blockType = placedBlock.getBlockType();

                if (blockType.equals("energy_regulator")) {
                    handleRegulatorDestruction(block, location);
                    return true; // Remove from explosion list (we handled it)
                }

                if (blockType.equals("energy_connector")) {
                    handleConnectorDestruction(block, location);
                    return true; // Remove from explosion list (we handled it)
                }

                if (blockType.equals("solar_generator")) {
                    handleSolarGeneratorDestruction(block, location);
                    return true; // Remove from explosion list (we handled it)
                }

                return false;
            } catch (Exception e) {
                plugin.getLogger().severe("Error handling explosion for energy block: " + e.getMessage());
                return false; // Don't crash server, allow explosion
            }
        });
    }

    /**
     * Prevent block explosions from destroying energy blocks
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (event.isCancelled()) return;

        // Check each block in the explosion
        event.blockList().removeIf(block -> {
            try {
                Location location = block.getLocation();
                PlacedBlock placedBlock = databaseManager.getBlock(location);

                if (placedBlock == null) {
                    return false; // Not our block, allow explosion
                }

                // This is an energy block - handle it properly
                String blockType = placedBlock.getBlockType();

                if (blockType.equals("energy_regulator")) {
                    handleRegulatorDestruction(block, location);
                    return true; // Remove from explosion list (we handled it)
                }

                if (blockType.equals("energy_connector")) {
                    handleConnectorDestruction(block, location);
                    return true; // Remove from explosion list (we handled it)
                }

                if (blockType.equals("solar_generator")) {
                    handleSolarGeneratorDestruction(block, location);
                    return true; // Remove from explosion list (we handled it)
                }

                return false;
            } catch (Exception e) {
                plugin.getLogger().severe("Error handling block explosion for energy block: " + e.getMessage());
                return false; // Don't crash server, allow explosion
            }
        });
    }

    /**
     * Prevent pistons from moving energy blocks
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            PlacedBlock placedBlock = databaseManager.getBlock(block.getLocation());
            if (placedBlock != null) {
                // Energy block detected - cancel piston movement
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Prevent pistons from pulling energy blocks
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            PlacedBlock placedBlock = databaseManager.getBlock(block.getLocation());
            if (placedBlock != null) {
                // Energy block detected - cancel piston movement
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Handle Energy Regulator destruction (explosion, etc.)
     */
    private void handleRegulatorDestruction(Block block, Location location) {
        try {
            // Remove energy network
            if (energyManager != null) {
                energyManager.removeNetwork(location);
            }

            // Drop the custom item
            RecipeItem energyRegulator = new org.ThefryGuy.techFactory.recipes.energy.EnergyRegulator();
            ItemStack drop = energyRegulator.getItemStack();

            if (block.getWorld() != null) {
                block.getWorld().dropItemNaturally(location, drop);
            }

            // Remove the physical block
            block.setType(Material.AIR);

            // Remove from database
            if (databaseManager != null) {
                databaseManager.removeBlockAsync(location, null);
            }

            plugin.getLogger().info("Energy Regulator destroyed by external force at " +
                location.getWorld().getName() + " " +
                location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
        } catch (Exception e) {
            plugin.getLogger().severe("Error handling regulator destruction: " + e.getMessage());
        }
    }

    /**
     * Handle Energy Connector destruction (explosion, etc.)
     */
    private void handleConnectorDestruction(Block block, Location location) {
        try {
            // PERFORMANCE: O(1) lookup using reverse map instead of O(n) iteration
            if (energyManager != null) {
                org.ThefryGuy.techFactory.energy.EnergyNetwork network = energyManager.getNetworkByLocation(location);
                if (network != null) {
                    network.disconnectConnector(location);
                    energyManager.unregisterDevice(location);
                }
            }

            // Drop the custom item
            RecipeItem energyConnector = new org.ThefryGuy.techFactory.recipes.energy.EnergyConnector();
            ItemStack drop = energyConnector.getItemStack();
            drop.setAmount(1);

            if (block.getWorld() != null) {
                block.getWorld().dropItemNaturally(location, drop);
            }

            // Remove the physical block
            block.setType(Material.AIR);

            // Remove from database
            if (databaseManager != null) {
                databaseManager.removeBlockAsync(location, null);
            }

            plugin.getLogger().info("Energy Connector destroyed by external force at " +
                location.getWorld().getName() + " " +
                location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
        } catch (Exception e) {
            plugin.getLogger().severe("Error handling connector destruction: " + e.getMessage());
        }
    }

    /**
     * Handle Solar Generator destruction (explosion, etc.)
     */
    private void handleSolarGeneratorDestruction(Block block, Location location) {
        try {
            // PERFORMANCE: O(1) lookup using reverse map instead of O(n) iteration
            if (energyManager != null) {
                org.ThefryGuy.techFactory.energy.EnergyNetwork network = energyManager.getNetworkByLocation(location);
                if (network != null) {
                    network.disconnectPanel(location);
                    energyManager.unregisterDevice(location);
                }
            }

            // Drop the custom item
            RecipeItem solarGenerator = new org.ThefryGuy.techFactory.recipes.energy.SolarGenerator();
            ItemStack drop = solarGenerator.getItemStack();
            drop.setAmount(1);

            if (block.getWorld() != null) {
                block.getWorld().dropItemNaturally(location, drop);
            }

            // Remove the physical block
            block.setType(Material.AIR);

            // Remove from database
            if (databaseManager != null) {
                databaseManager.removeBlockAsync(location, null);
            }

            plugin.getLogger().info("Solar Generator destroyed by external force at " +
                location.getWorld().getName() + " " +
                location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
        } catch (Exception e) {
            plugin.getLogger().severe("Error handling solar generator destruction: " + e.getMessage());
        }
    }
}

