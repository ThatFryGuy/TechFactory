package org.ThefryGuy.techFactory.workstations.multiblocks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.RecipeRegistry;
import org.ThefryGuy.techFactory.util.ItemUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pressure Chamber Machine - Handles extreme compression
 * 
 * Structure (3x3x3):
 * Layer 1 (Top):    [Smooth Stone Slab] [Dispenser ↓] [Smooth Stone Slab]
 * Layer 2 (Middle): [Piston ↑]          [Glass]        [Piston ↑]
 * Layer 3 (Bottom): [Piston ↑]          [Cauldron]     [Piston ↑]
 * 
 * Player right-clicks the Dispenser to open it
 */
public class PressureChamberMachine {

    /**
     * Check if a block is part of a valid Pressure Chamber structure.
     * Called when player right-clicks a Dispenser.
     *
     * @param dispenserBlock The dispenser block that was clicked
     * @return true if this is a valid Pressure Chamber
     */
    public static boolean isValidStructure(Block dispenserBlock) {
        if (dispenserBlock.getType() != Material.DISPENSER) {
            return false;
        }

        // Check if dispenser is facing down
        org.bukkit.block.data.Directional dispenserData = (org.bukkit.block.data.Directional) dispenserBlock.getBlockData();
        if (dispenserData.getFacing() != org.bukkit.block.BlockFace.DOWN) {
            return false;
        }

        // Layer 1 (Top) - Check smooth stone slabs on both sides of dispenser
        Block slab1 = dispenserBlock.getRelative(1, 0, 0);
        Block slab2 = dispenserBlock.getRelative(-1, 0, 0);
        boolean xAxisSlabs = slab1.getType() == Material.SMOOTH_STONE_SLAB && slab2.getType() == Material.SMOOTH_STONE_SLAB;

        Block slab3 = dispenserBlock.getRelative(0, 0, 1);
        Block slab4 = dispenserBlock.getRelative(0, 0, -1);
        boolean zAxisSlabs = slab3.getType() == Material.SMOOTH_STONE_SLAB && slab4.getType() == Material.SMOOTH_STONE_SLAB;

        // Must have slabs on one axis (either X or Z)
        if (!xAxisSlabs && !zAxisSlabs) {
            return false;
        }

        // Layer 2 (Middle) - Check glass below dispenser
        Block glassBlock = dispenserBlock.getRelative(0, -1, 0);
        if (glassBlock.getType() != Material.GLASS) {
            return false;
        }

        // Layer 2 - Check pistons on both sides of glass (same axis as slabs)
        if (xAxisSlabs) {
            Block piston1 = glassBlock.getRelative(1, 0, 0);
            Block piston2 = glassBlock.getRelative(-1, 0, 0);
            
            if (piston1.getType() != Material.PISTON || piston2.getType() != Material.PISTON) {
                return false;
            }

            // Check if pistons are facing up
            org.bukkit.block.data.Directional piston1Data = (org.bukkit.block.data.Directional) piston1.getBlockData();
            org.bukkit.block.data.Directional piston2Data = (org.bukkit.block.data.Directional) piston2.getBlockData();
            if (piston1Data.getFacing() != org.bukkit.block.BlockFace.UP || 
                piston2Data.getFacing() != org.bukkit.block.BlockFace.UP) {
                return false;
            }
        } else if (zAxisSlabs) {
            Block piston1 = glassBlock.getRelative(0, 0, 1);
            Block piston2 = glassBlock.getRelative(0, 0, -1);
            
            if (piston1.getType() != Material.PISTON || piston2.getType() != Material.PISTON) {
                return false;
            }

            // Check if pistons are facing up
            org.bukkit.block.data.Directional piston1Data = (org.bukkit.block.data.Directional) piston1.getBlockData();
            org.bukkit.block.data.Directional piston2Data = (org.bukkit.block.data.Directional) piston2.getBlockData();
            if (piston1Data.getFacing() != org.bukkit.block.BlockFace.UP || 
                piston2Data.getFacing() != org.bukkit.block.BlockFace.UP) {
                return false;
            }
        }

        // Layer 3 (Bottom) - Check cauldron below glass
        Block cauldronBlock = glassBlock.getRelative(0, -1, 0);
        if (cauldronBlock.getType() != Material.CAULDRON) {
            return false;
        }

        // Layer 3 - Check pistons on both sides of cauldron (same axis as slabs)
        if (xAxisSlabs) {
            Block piston1 = cauldronBlock.getRelative(1, 0, 0);
            Block piston2 = cauldronBlock.getRelative(-1, 0, 0);
            
            if (piston1.getType() != Material.PISTON || piston2.getType() != Material.PISTON) {
                return false;
            }

            // Check if pistons are facing up
            org.bukkit.block.data.Directional piston1Data = (org.bukkit.block.data.Directional) piston1.getBlockData();
            org.bukkit.block.data.Directional piston2Data = (org.bukkit.block.data.Directional) piston2.getBlockData();
            return piston1Data.getFacing() == org.bukkit.block.BlockFace.UP && 
                   piston2Data.getFacing() == org.bukkit.block.BlockFace.UP;
        } else if (zAxisSlabs) {
            Block piston1 = cauldronBlock.getRelative(0, 0, 1);
            Block piston2 = cauldronBlock.getRelative(0, 0, -1);
            
            if (piston1.getType() != Material.PISTON || piston2.getType() != Material.PISTON) {
                return false;
            }

            // Check if pistons are facing up
            org.bukkit.block.data.Directional piston1Data = (org.bukkit.block.data.Directional) piston1.getBlockData();
            org.bukkit.block.data.Directional piston2Data = (org.bukkit.block.data.Directional) piston2.getBlockData();
            return piston1Data.getFacing() == org.bukkit.block.BlockFace.UP && 
                   piston2Data.getFacing() == org.bukkit.block.BlockFace.UP;
        }

        return false;
    }

    /**
     * Open the dispenser inventory when player right-clicks the dispenser.
     *
     * @param dispenserBlock The dispenser block
     * @param player The player who clicked
     */
    public static void openInventory(Block dispenserBlock, Player player) {
        Dispenser dispenser = (Dispenser) dispenserBlock.getState();

        player.openInventory(dispenser.getInventory());
        player.sendMessage(ChatColor.DARK_PURPLE + "Pressure Chamber opened!");
        player.sendMessage(ChatColor.GRAY + "Place items to compress them further!");
    }

    /**
     * Process compression when the player clicks the glass block (Slimefun style).
     * Shows "Invalid recipe" if no recipe matches.
     *
     * @param dispenser The dispenser inventory
     * @param player The player who clicked
     */
    public static void processRecipe(Dispenser dispenser, Player player) {
        Inventory inv = dispenser.getInventory();

        // Check if inventory is empty
        boolean isEmpty = true;
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) != null) {
                isEmpty = false;
                break;
            }
        }

        if (isEmpty) {
            player.sendMessage(ChatColor.RED + "The Pressure Chamber is empty!");
            return;
        }

        // REFACTORED: Use RecipeRegistry with "at least" quantity matching
        // This allows players to put stacks and craft multiple times (Slimefun-style)
        // Build map of available items and quantities
        Map<String, Integer> availableItems = new HashMap<>();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                String itemId = ItemUtils.getItemId(item);
                if (itemId != null) {
                    availableItems.put(itemId, availableItems.getOrDefault(itemId, 0) + item.getAmount());
                }
            }
        }

        // Find matching recipe with quantity checking
        RecipeRegistry.RecipeMatch match = RecipeRegistry.findRecipeWithQuantities("Pressure Chamber", availableItems);

        if (match != null) {
            RecipeItem output = match.output;

            // Consume only the required quantities (not the entire stack!)
            for (Map.Entry<String, Integer> required : match.requiredQuantities.entrySet()) {
                String itemId = required.getKey();
                int qtyToRemove = required.getValue();

                // Remove items from inventory
                for (int i = 0; i < inv.getSize() && qtyToRemove > 0; i++) {
                    ItemStack item = inv.getItem(i);
                    if (item != null) {
                        String currentItemId = ItemUtils.getItemId(item);
                        if (itemId.equals(currentItemId)) {
                            int amountInSlot = item.getAmount();
                            if (amountInSlot <= qtyToRemove) {
                                // Remove entire stack
                                inv.setItem(i, null);
                                qtyToRemove -= amountInSlot;
                            } else {
                                // Remove partial stack
                                item.setAmount(amountInSlot - qtyToRemove);
                                qtyToRemove = 0;
                            }
                        }
                    }
                }
            }

            ItemStack outputItem = output.getItemStack();

            // Try to output to chest first, then dispenser
            // Build multiblock blocks list for chest detection
            Block dispenserBlock = dispenser.getBlock();
            Block glassBlock = dispenserBlock.getRelative(0, -1, 0);
            Block cauldronBlock = glassBlock.getRelative(0, -1, 0);

            // Determine which axis the structure is on
            Block slab1 = dispenserBlock.getRelative(1, 0, 0);
            Block slab2 = dispenserBlock.getRelative(-1, 0, 0);
            boolean xAxisSlabs = slab1.getType() == Material.SMOOTH_STONE_SLAB && slab2.getType() == Material.SMOOTH_STONE_SLAB;

            java.util.List<Block> multiblockBlocks = new java.util.ArrayList<>();
            multiblockBlocks.add(dispenserBlock);
            multiblockBlocks.add(glassBlock);
            multiblockBlocks.add(cauldronBlock);

            if (xAxisSlabs) {
                multiblockBlocks.add(slab1);
                multiblockBlocks.add(slab2);
                multiblockBlocks.add(glassBlock.getRelative(1, 0, 0));
                multiblockBlocks.add(glassBlock.getRelative(-1, 0, 0));
                multiblockBlocks.add(cauldronBlock.getRelative(1, 0, 0));
                multiblockBlocks.add(cauldronBlock.getRelative(-1, 0, 0));
            } else {
                Block slab3 = dispenserBlock.getRelative(0, 0, 1);
                Block slab4 = dispenserBlock.getRelative(0, 0, -1);
                multiblockBlocks.add(slab3);
                multiblockBlocks.add(slab4);
                multiblockBlocks.add(glassBlock.getRelative(0, 0, 1));
                multiblockBlocks.add(glassBlock.getRelative(0, 0, -1));
                multiblockBlocks.add(cauldronBlock.getRelative(0, 0, 1));
                multiblockBlocks.add(cauldronBlock.getRelative(0, 0, -1));
            }

            if (!ItemUtils.outputToChest(multiblockBlocks, outputItem)) {
                inv.addItem(outputItem);
            }

            player.sendMessage(ChatColor.GREEN + "✓ Processed items into " + output.getDisplayName() + "!");
            return;
        }

        // No valid recipe found
        player.sendMessage(ChatColor.RED + "✗ Invalid recipe! Check the guide for Pressure Chamber recipes.");
    }

}

