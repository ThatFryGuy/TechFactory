package org.ThefryGuy.techFactory.workstations.multiblocks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.RecipeRegistry;
import org.ThefryGuy.techFactory.recipes.resources.*;
import org.ThefryGuy.techFactory.recipes.components.*;
import org.ThefryGuy.techFactory.util.VanillaItemRegistry;
import org.ThefryGuy.techFactory.util.ItemUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced Crafting Table Machine - Handles the actual functionality
 *
 * Structure (like Slimefun's Enhanced Crafting Table):
 * - Bottom: Dispenser (holds the recipe items)
 * - Top: Crafting Table (player right-clicks this)
 *
 * How it works:
 * 1. Player places Dispenser on ground
 * 2. Player places Crafting Table on top of Dispenser
 * 3. Player puts recipe items in Dispenser
 * 4. Player right-clicks Crafting Table
 * 5. If recipe matches, output item is given to player
 *
 * The recipe data is in recipes/workstations/multiblocks/BasicWorkbench.java
 */
public class BasicWorkbenchMachine {

    // REFACTORED: Recipes now centralized in RecipeRegistry
    // No local recipe map needed anymore!

    /**
     * Check if a valid Basic Workbench multiblock exists at this location.
     * 
     * Structure:
     * [Crafting Table] ← Top (player clicks here)
     * [Dispenser]      ← Bottom (holds items)
     * 
     * @param craftingTable The crafting table block (top)
     * @return true if valid multiblock structure
     */
    public static boolean isValidStructure(Block craftingTable) {
        // Check if top is a crafting table
        if (craftingTable.getType() != Material.CRAFTING_TABLE) {
            return false;
        }

        // Check if block below is a dispenser
        Block below = craftingTable.getRelative(0, -1, 0);
        return below.getType() == Material.DISPENSER;
    }

    /**
     * Get the dispenser below the crafting table.
     * 
     * @param craftingTable The crafting table block
     * @return The dispenser block, or null if not valid structure
     */
    public static Dispenser getDispenser(Block craftingTable) {
        if (!isValidStructure(craftingTable)) {
            return null;
        }

        Block below = craftingTable.getRelative(0, -1, 0);

        // Verify it's actually a dispenser
        if (!(below.getState() instanceof Dispenser)) {
            return null;
        }

        return (Dispenser) below.getState();
    }

    /**
     * Open the Enhanced Crafting Table GUI for the player.
     * This shows the dispenser inventory where they can place recipe items.
     *
     * @param player The player who clicked
     * @param craftingTable The crafting table block
     */
    public static void openWorkbench(Player player, Block craftingTable) {
        Dispenser dispenser = getDispenser(craftingTable);
        if (dispenser == null) {
            player.sendMessage(ChatColor.RED + "Invalid multiblock structure!");
            return;
        }

        // Open the dispenser inventory
        player.openInventory(dispenser.getInventory());
        player.sendMessage(ChatColor.GREEN + "Enhanced Crafting Table opened!");
        player.sendMessage(ChatColor.GRAY + "Place items in the 3x3 grid and close to craft.");
    }

    /**
     * Process a recipe when the player clicks the crafting table (Slimefun style).
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
            player.sendMessage(ChatColor.RED + "The Enhanced Crafting Table is empty!");
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
        RecipeRegistry.RecipeMatch match = RecipeRegistry.findRecipeWithQuantities("Basic Workbench", availableItems);

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
            Block craftingTable = dispenserBlock.getRelative(0, 1, 0);
            java.util.List<Block> multiblockBlocks = java.util.Arrays.asList(dispenserBlock, craftingTable);

            if (!ItemUtils.outputToChest(multiblockBlocks, outputItem)) {
                inv.addItem(outputItem);
            }

            player.sendMessage(ItemUtils.createCraftMessage(outputItem, "Crafted"));
        } else {
            // No valid recipe found
            player.sendMessage(ChatColor.RED + "✗ Invalid recipe! Check the guide for Enhanced Crafting Table recipes.");
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
     * NOTE: This multiblock is built from vanilla blocks (Dispenser + Crafting Table),
     * so no special controller item needs to be dropped. Players get the vanilla blocks back.
     *
     * @param location The location where it was broken
     * @param player The player who broke it (can be null)
     */
    public static void breakMultiblock(Location location, Player player) {
        if (player != null) {
            player.sendMessage(ChatColor.RED + "Enhanced Crafting Table destroyed!");
        }
    }
}

