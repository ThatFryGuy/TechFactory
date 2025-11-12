package org.ThefryGuy.techFactory.workstations.multiblocks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.RecipeRegistry;
import org.ThefryGuy.techFactory.recipes.resources.*;
import org.ThefryGuy.techFactory.util.VanillaItemRegistry;
import org.ThefryGuy.techFactory.util.ItemUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ore Crusher Machine - Handles the actual functionality
 *
 * Structure (side view):
 * Layer Y+1 (top):    [air] [nether_brick_fence] [air]
 * Layer Y (middle):   [iron_bars] [dispenser↑] [iron_bars]
 *
 * - Center: Dispenser (facing UP)
 * - Sides (same Y level): Iron Bars (on 2 opposite sides)
 * - Top (Y+1): Nether Brick Fence directly above dispenser
 *
 * How it works:
 * 1. Player places the multiblock structure
 * 2. Player clicks Dispenser to open GUI
 * 3. Player puts input items in Dispenser (e.g., 16x Netherrack)
 * 4. Player clicks Iron Bars or Nether Brick Fence to crush
 * 5. Output goes to adjacent chest or back to dispenser
 */
public class OreCrusherMachine {

    // REFACTORED: Recipes now centralized in RecipeRegistry
    // No local recipe map needed anymore!

    /**
     * Check if a valid Ore Crusher multiblock exists at this location.
     *
     * Structure (side view):
     * Layer Y+1 (top):    [air] [nether_brick_fence] [air]
     * Layer Y (middle):   [iron_bars] [dispenser↑] [iron_bars]
     *
     * @param centerBlock The center dispenser block
     * @return true if valid multiblock structure
     */
    public static boolean isValidStructure(Block centerBlock) {
        // Check if center is a dispenser facing UP
        if (centerBlock.getType() != Material.DISPENSER) {
            return false;
        }

        // Get facing direction from block data
        if (centerBlock.getBlockData() instanceof org.bukkit.block.data.Directional) {
            org.bukkit.block.data.Directional directional = (org.bukkit.block.data.Directional) centerBlock.getBlockData();
            BlockFace facing = directional.getFacing();
            if (facing != BlockFace.UP) {
                return false;
            }
        } else {
            return false;
        }

        // Structure (side view):
        // Layer Y+1 (top):    [air] [nether_brick_fence] [air]
        // Layer Y (middle):   [iron_bars] [dispenser↑] [iron_bars]
        //
        // The nether brick fence must be directly ABOVE the dispenser (Y+1)
        // The iron bars must be on 2 OPPOSITE SIDES at the same Y level as dispenser

        // Check if nether brick fence is directly above (Y+1)
        Block above = centerBlock.getRelative(0, 1, 0);
        if (above.getType() != Material.NETHER_BRICK_FENCE) {
            return false;
        }

        // Check Option 1: Iron bars on X axis (East and West)
        Block west = centerBlock.getRelative(-1, 0, 0);
        Block east = centerBlock.getRelative(1, 0, 0);

        if (west.getType() == Material.IRON_BARS && east.getType() == Material.IRON_BARS) {
            return true;
        }

        // Check Option 2: Iron bars on Z axis (North and South)
        Block north = centerBlock.getRelative(0, 0, -1);
        Block south = centerBlock.getRelative(0, 0, 1);

        if (north.getType() == Material.IRON_BARS && south.getType() == Material.IRON_BARS) {
            return true;
        }

        return false;
    }

    /**
     * Get the dispenser from a valid Ore Crusher structure.
     * 
     * @param centerBlock The center dispenser block
     * @return The dispenser block, or null if not valid structure
     */
    public static Dispenser getDispenser(Block centerBlock) {
        if (!isValidStructure(centerBlock)) {
            return null;
        }

        // Verify it's actually a dispenser
        if (!(centerBlock.getState() instanceof Dispenser)) {
            return null;
        }

        return (Dispenser) centerBlock.getState();
    }

    /**
     * Open the Ore Crusher GUI for the player.
     * This shows the dispenser inventory where they can place items to crush.
     * 
     * @param player The player who activated it
     * @param crusherBlock The center dispenser block
     */
    public static void openCrusher(Player player, Block crusherBlock) {
        Dispenser dispenser = getDispenser(crusherBlock);
        if (dispenser == null) {
            player.sendMessage(ChatColor.RED + "Invalid Ore Crusher structure!");
            return;
        }

        // Open the dispenser inventory
        player.openInventory(dispenser.getInventory());
        // Message is now shown in MultiblockListener for consistency
    }

    /**
     * Process a recipe when the player clicks the nether brick fence (Slimefun style).
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
            player.sendMessage(ChatColor.RED + "The Ore Crusher is empty!");
            return;
        }

        // Build a map of item IDs and their quantities in the inventory
        Map<String, Integer> availableItems = new HashMap<>();

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;

            String itemId = ItemUtils.getItemId(item);
            if (itemId == null) {
                // Check for vanilla items
                itemId = getVanillaItemId(item);
            }

            if (itemId != null) {
                availableItems.put(itemId, availableItems.getOrDefault(itemId, 0) + item.getAmount());
            }
        }

        // REFACTORED: Use RecipeRegistry with "at least" quantity matching
        // This allows players to put stacks and craft multiple times (Slimefun-style)
        RecipeRegistry.RecipeMatch match = RecipeRegistry.findRecipeWithQuantities("Ore Crusher", availableItems);

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
                        if (currentItemId == null) {
                            currentItemId = getVanillaItemId(item);
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

            // Try to output to adjacent chest first, then dispenser
            // Build multiblock blocks list for chest detection
            Block dispenserBlock = dispenser.getBlock();
            Block ironBars = dispenserBlock.getRelative(0, 1, 0);
            Block fence = dispenserBlock.getRelative(0, -1, 0);
            java.util.List<Block> multiblockBlocks = java.util.Arrays.asList(dispenserBlock, ironBars, fence);

            if (!ItemUtils.outputToChest(multiblockBlocks, outputItem)) {
                inv.addItem(outputItem);
            }

            player.sendMessage(ItemUtils.createCraftMessage(outputItem, "Crushed"));
        } else {
            // No valid recipe found
            player.sendMessage(ChatColor.RED + "✗ Invalid recipe! Check the guide for Ore Crusher recipes.");
        }
    }

    /**
     * Get vanilla item ID for recipe matching
     */
    /**
     * REFACTORED: Now uses centralized VanillaItemRegistry
     * @deprecated Use VanillaItemRegistry.getVanillaItemId() directly
     */
    @Deprecated
    private static String getVanillaItemId(ItemStack item) {
        return VanillaItemRegistry.getVanillaItemId(item);
    }

    /**
     * Break the multiblock and drop items.
     * Called when any part of the multiblock is broken.
     *
     * NOTE: This multiblock is built from vanilla blocks (Dispenser + Iron Bars + Nether Brick Fence),
     * so no special controller item needs to be dropped. Players get the vanilla blocks back.
     *
     * @param location The location where it was broken
     * @param player The player who broke it (can be null)
     */
    public static void breakMultiblock(Location location, Player player) {
        if (player != null) {
            player.sendMessage(ChatColor.RED + "Ore Crusher destroyed!");
        }
    }
}