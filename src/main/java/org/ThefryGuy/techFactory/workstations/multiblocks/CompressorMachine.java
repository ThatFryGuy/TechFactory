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
import org.ThefryGuy.techFactory.recipes.resources.Carbon;
import org.ThefryGuy.techFactory.recipes.resources.CompressedCarbon;
import org.ThefryGuy.techFactory.util.ItemUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compressor Machine - Handles the actual functionality
 * 
 * Structure:
 *   [Fence]                    ← Right-click here
 *   [Piston] [Dispenser] [Piston]  ← Pistons facing up, Dispenser facing up
 */
public class CompressorMachine {

    /**
     * Check if a block is part of a valid Compressor structure.
     * Called when player right-clicks a Fence.
     *
     * @param fenceBlock The fence block that was clicked
     * @return true if this is a valid Compressor
     */
    public static boolean isValidStructure(Block fenceBlock) {
        if (!ItemUtils.isFenceType(fenceBlock.getType())) {
            return false;
        }

        // Check dispenser below fence
        Block dispenserBlock = fenceBlock.getRelative(0, -1, 0);
        if (dispenserBlock.getType() != Material.DISPENSER) {
            return false;
        }

        // Check if dispenser is facing up
        org.bukkit.block.data.Directional dispenserData = (org.bukkit.block.data.Directional) dispenserBlock.getBlockData();
        if (dispenserData.getFacing() != org.bukkit.block.BlockFace.UP) {
            return false;
        }

        // Check for pistons on both sides (X-axis)
        Block side1 = dispenserBlock.getRelative(1, 0, 0);
        Block side2 = dispenserBlock.getRelative(-1, 0, 0);
        boolean xAxisValid = side1.getType() == Material.PISTON && side2.getType() == Material.PISTON;

        // Check if pistons are facing up (X-axis)
        if (xAxisValid) {
            org.bukkit.block.data.Directional piston1Data = (org.bukkit.block.data.Directional) side1.getBlockData();
            org.bukkit.block.data.Directional piston2Data = (org.bukkit.block.data.Directional) side2.getBlockData();
            if (piston1Data.getFacing() == org.bukkit.block.BlockFace.UP 
                && piston2Data.getFacing() == org.bukkit.block.BlockFace.UP) {
                return true;
            }
        }

        // Check for pistons on both sides (Z-axis)
        Block side3 = dispenserBlock.getRelative(0, 0, 1);
        Block side4 = dispenserBlock.getRelative(0, 0, -1);
        boolean zAxisValid = side3.getType() == Material.PISTON && side4.getType() == Material.PISTON;

        // Check if pistons are facing up (Z-axis)
        if (zAxisValid) {
            org.bukkit.block.data.Directional piston3Data = (org.bukkit.block.data.Directional) side3.getBlockData();
            org.bukkit.block.data.Directional piston4Data = (org.bukkit.block.data.Directional) side4.getBlockData();
            return piston3Data.getFacing() == org.bukkit.block.BlockFace.UP 
                && piston4Data.getFacing() == org.bukkit.block.BlockFace.UP;
        }

        return false;
    }

    /**
     * Open the dispenser inventory when player right-clicks the fence.
     *
     * @param fenceBlock The fence block
     * @param player The player who clicked
     */
    public static void openInventory(Block fenceBlock, Player player) {
        Block dispenserBlock = fenceBlock.getRelative(0, -1, 0);

        // Verify it's actually a dispenser
        if (!(dispenserBlock.getState() instanceof Dispenser)) {
            player.sendMessage(ChatColor.RED + "Invalid Compressor structure!");
            return;
        }

        Dispenser dispenser = (Dispenser) dispenserBlock.getState();
        player.openInventory(dispenser.getInventory());
        // Message is now shown in MultiblockListener for consistency
    }

    /**
     * Process compression when the player clicks the fence (Slimefun style).
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
            player.sendMessage(ChatColor.RED + "The Compressor is empty!");
            return;
        }

        // REFACTORED: Use RecipeRegistry with "at least" quantity matching
        // This allows players to put stacks and craft multiple times (Slimefun-style)
        // Build map of available items and quantities
        Map<String, Integer> availableItems = new HashMap<>();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                String itemId;

                // Check if it's a TechFactory item
                String techItemId = ItemUtils.getItemId(item);
                if (techItemId != null) {
                    itemId = techItemId;
                } else if (item.getType() == Material.COAL) {
                    itemId = "coal";
                } else {
                    continue; // Skip unknown items
                }

                availableItems.put(itemId, availableItems.getOrDefault(itemId, 0) + item.getAmount());
            }
        }

        // Find matching recipe with quantity checking
        RecipeRegistry.RecipeMatch match = RecipeRegistry.findRecipeWithQuantities("Compressor", availableItems);

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
                        if (currentItemId == null && item.getType() == Material.COAL) {
                            currentItemId = "coal";
                        }

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
            Block fence = dispenserBlock.getRelative(0, 1, 0);

            Block piston1 = dispenserBlock.getRelative(1, 0, 0);
            Block piston2 = dispenserBlock.getRelative(-1, 0, 0);
            boolean xAxisValid = piston1.getType() == Material.PISTON && piston2.getType() == Material.PISTON;

            Block piston3 = dispenserBlock.getRelative(0, 0, 1);
            Block piston4 = dispenserBlock.getRelative(0, 0, -1);
            boolean zAxisValid = piston3.getType() == Material.PISTON && piston4.getType() == Material.PISTON;

            List<Block> multiblockBlocks = new ArrayList<>();
            multiblockBlocks.add(dispenserBlock);
            multiblockBlocks.add(fence);
            if (xAxisValid) {
                multiblockBlocks.add(piston1);
                multiblockBlocks.add(piston2);
            } else if (zAxisValid) {
                multiblockBlocks.add(piston3);
                multiblockBlocks.add(piston4);
            }

            if (!ItemUtils.outputToChest(multiblockBlocks, outputItem)) {
                inv.addItem(outputItem);
            }

            player.sendMessage(ItemUtils.createCraftMessage(outputItem, "Compressed"));
            return;
        }

        // No valid recipe found
        player.sendMessage(ChatColor.RED + "✗ Invalid recipe! Check the guide for Compressor recipes.");
    }

}

