package org.ThefryGuy.techFactory.recipes.workstations.multiblocks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

/**
 * Enhanced Crafting Table - A simple multiblock crafting station
 *
 * This is just the RECIPE data (what items needed to build it).
 * The actual functionality is in machines/multiblocks/BasicWorkbenchMachine.java
 */
public class BasicWorkbench implements RecipeItem {

    @Override
    public String getId() {
        return "enhanced_crafting_table";
    }

    @Override
    public String getDisplayName() {
        return "Enhanced Crafting Table";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.YELLOW;
    }

    @Override
    public Material getMaterial() {
        return Material.CRAFTING_TABLE;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A multiblock crafting station",
                ChatColor.GRAY + "Enhanced crafting capabilities",
                "",
                ChatColor.GOLD + "How to Build:",
                ChatColor.GRAY + "1. Place Dispenser on ground",
                ChatColor.GRAY + "2. Place Crafting Table on top",
                "",
                ChatColor.GOLD + "How to Use:",
                ChatColor.GRAY + "Right-click the Crafting Table",
                "",
                ChatColor.GOLD + "Output:",
                ChatColor.GRAY + "Place a chest next to the Dispenser",
                ChatColor.GRAY + "Items will output to the chest",
                ChatColor.GRAY + "Or stay in the Dispenser if no chest"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: Crafting Table + Dispenser in shaped pattern
        // This shows how to BUILD it, not craft an item
        return new ItemStack[] {
            null, new ItemStack(Material.CRAFTING_TABLE), null,
            null, new ItemStack(Material.DISPENSER), null,
            null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Crafting Table"; // Made at a regular crafting table
    }
}

