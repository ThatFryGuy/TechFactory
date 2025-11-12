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
import org.ThefryGuy.techFactory.recipes.dusts.*;
import org.ThefryGuy.techFactory.util.ItemUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Ore Washer - Handles the actual functionality
 * 
 * Structure:
 * [Dispenser]  ← Top (player clicks here, holds items)
 * [Fence]      ← Middle
 * [Cauldron]   ← Bottom
 * 
 * Process:
 * Sifted Ore Dust → Random Metal Dust (Iron, Copper, Gold, etc.)
 * 
 * The recipe data is in recipes/workstations/multiblocks/OreWasher.java
 */
public class OreWasherMachine {

    private static final Random random = new Random();

    /**
     * Check if a valid Ore Washer multiblock exists at this location.
     * 
     * Structure:
     * [Dispenser]  ← Top (player clicks here)
     * [Fence]      ← Middle
     * [Cauldron]   ← Bottom
     * 
     * @param dispenser The dispenser block (top)
     * @return true if valid multiblock structure
     */
    public static boolean isValidStructure(Block dispenser) {
        // Check if top is a dispenser
        if (dispenser.getType() != Material.DISPENSER) {
            return false;
        }

        // Check if block below is a fence
        Block middle = dispenser.getRelative(0, -1, 0);
        if (!ItemUtils.isFenceType(middle.getType())) {
            return false;
        }

        // Check if block below fence is a cauldron
        Block bottom = middle.getRelative(0, -1, 0);
        return bottom.getType() == Material.CAULDRON;
    }

    /**
     * Get the dispenser from the multiblock
     */
    private static Dispenser getDispenser(Block dispenser) {
        if (dispenser.getType() != Material.DISPENSER) {
            return null;
        }
        return (Dispenser) dispenser.getState();
    }

    /**
     * Open the Ore Washer GUI for the player.
     * This shows the dispenser inventory where they can place items to wash.
     * 
     * @param player The player who clicked
     * @param dispenser The dispenser block
     */
    public static void openInventory(Player player, Block dispenser) {
        Dispenser dispenserState = getDispenser(dispenser);
        if (dispenserState == null) {
            player.sendMessage(ChatColor.RED + "Invalid multiblock structure!");
            return;
        }

        // Open the dispenser inventory
        player.openInventory(dispenserState.getInventory());
        // Message is now shown in MultiblockListener for consistency
    }

    /**
     * Process washing recipes when the player clicks the fence (Slimefun style).
     * Shows "Invalid recipe" if no recipe matches.
     *
     * Process: Sifted Ore Dust → Random Metal Dust
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
            player.sendMessage(ChatColor.RED + "The Ore Washer is empty!");
            return;
        }

        // Check for Sifted Ore Dust → Random Metal Dust
        int siftedCount = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && RecipeItem.isValidItem(item, "sifted_ore_dust")) {
                siftedCount += item.getAmount();
                inv.setItem(i, null); // Remove sifted ore dust
            }
        }

        if (siftedCount > 0) {
            // Give random metal dusts
            Map<String, Integer> dustCounts = new HashMap<>();

            for (int i = 0; i < siftedCount; i++) {
                RecipeItem randomDust = getRandomMetalDust();
                String dustName = randomDust.getDisplayName();
                dustCounts.put(dustName, dustCounts.getOrDefault(dustName, 0) + 1);
            }

            // Build multiblock blocks list for chest detection
            Block dispenserBlock = dispenser.getBlock();
            Block fenceBlock = dispenserBlock.getRelative(0, -1, 0);
            Block cauldronBlock = fenceBlock.getRelative(0, -1, 0);
            java.util.List<Block> multiblockBlocks = java.util.Arrays.asList(
                dispenserBlock,
                fenceBlock,
                cauldronBlock
            );

            // Output the dusts to chest or dispenser
            for (Map.Entry<String, Integer> entry : dustCounts.entrySet()) {
                RecipeItem dust = getMetalDustByName(entry.getKey());
                ItemStack output = dust.getItemStack();
                output.setAmount(entry.getValue());

                // Try to output to adjacent chest first, then dispenser
                if (!ItemUtils.outputToChest(multiblockBlocks, output)) {
                    // No chest found, output to dispenser inventory
                    inv.addItem(output);
                }
            }

            player.sendMessage(ChatColor.GREEN + "✓ Washed " + siftedCount + " Sifted Ore Dust into metal dusts!");
            player.sendMessage(ChatColor.GRAY + "Results: " + formatDustCounts(dustCounts));
        } else {
            // No valid items found
            player.sendMessage(ChatColor.RED + "✗ Invalid recipe! Ore Washer requires Sifted Ore Dust.");
        }
    }

    /**
     * Get a random metal dust from the pool
     */
    private static RecipeItem getRandomMetalDust() {
        RecipeItem[] dusts = {
            new IronDust(),
            new CopperDust(),
            new GoldDust(),
            new TinDust(),
            new SilverDust(),
            new AluminumDust(),
            new LeadDust(),
            new ZincDust(),
            new MagnesiumDust()
        };
        return dusts[random.nextInt(dusts.length)];
    }

    /**
     * Get a metal dust by its display name
     */
    private static RecipeItem getMetalDustByName(String name) {
        return switch (name) {
            case "Iron Dust" -> new IronDust();
            case "Copper Dust" -> new CopperDust();
            case "Gold Dust" -> new GoldDust();
            case "Tin Dust" -> new TinDust();
            case "Silver Dust" -> new SilverDust();
            case "Aluminum Dust" -> new AluminumDust();
            case "Lead Dust" -> new LeadDust();
            case "Zinc Dust" -> new ZincDust();
            case "Magnesium Dust" -> new MagnesiumDust();
            default -> new IronDust(); // Fallback
        };
    }

    /**
     * Format dust counts for display
     */
    private static String formatDustCounts(Map<String, Integer> dustCounts) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Map.Entry<String, Integer> entry : dustCounts.entrySet()) {
            if (count > 0) sb.append(", ");
            sb.append(entry.getValue()).append("x ").append(entry.getKey());
            count++;
        }
        return sb.toString();
    }



    /**
     * Break the multiblock and drop items.
     * Called when any part of the multiblock is broken.
     *
     * NOTE: This multiblock is built from vanilla blocks (Dispenser + Fence + Cauldron),
     * so no special controller item needs to be dropped. Players get the vanilla blocks back.
     *
     * @param location The location where it was broken
     * @param player The player who broke it (can be null)
     */
    public static void breakMultiblock(Location location, Player player) {
        if (player != null) {
            player.sendMessage(ChatColor.RED + "Ore Washer destroyed!");
        }
    }
}

