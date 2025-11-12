package org.ThefryGuy.techFactory.recipes.workstations.multiblocks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

/**
 * Ore Washer - A multiblock machine for washing sifted ore dust into specific metal dusts
 * 
 * Process:
 * Sifted Ore Dust → Specific Metal Dusts (Iron, Copper, Gold, etc.)
 * 
 * This is just the RECIPE data (how to build it).
 * The actual functionality is in workstations/multiblocks/OreWasherMachine.java
 */
public class OreWasher implements RecipeItem {

    @Override
    public String getId() {
        return "ore_washer";
    }

    @Override
    public String getDisplayName() {
        return "Ore Washer";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.AQUA;
    }

    @Override
    public Material getMaterial() {
        return Material.CAULDRON;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A multiblock machine for washing ore dust",
                ChatColor.GRAY + "Separates sifted ore dust into specific metals",
                "",
                ChatColor.GOLD + "How to Use:",
                ChatColor.GRAY + "Right-click the Dispenser",
                "",
                ChatColor.GOLD + "Output:",
                ChatColor.GRAY + "Place a chest next to the Dispenser",
                ChatColor.GRAY + "Items will output to the chest",
                ChatColor.GRAY + "Or stay in the Dispenser if no chest",
                "",
                ChatColor.GOLD + "Recipe:",
                ChatColor.GRAY + "Sifted Ore Dust → Metal Dusts"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: Shows the structure to build
        // Top row: Dispenser
        // Middle row: Fence
        // Bottom row: Cauldron
        return new ItemStack[] {
            null, new ItemStack(Material.DISPENSER), null,
            null, new ItemStack(Material.OAK_FENCE), null,
            null, new ItemStack(Material.CAULDRON), null
        };
    }

    @Override
    public String getMachineType() {
        return "Multiblock"; // This IS a multiblock, not made at a machine
    }
}

