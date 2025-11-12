package org.ThefryGuy.techFactory.recipes.resources;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class Silicon implements RecipeItem {

    @Override
    public String getId() {
        return "silicon";
    }

    @Override
    public String getDisplayName() {
        return "Silicon";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_AQUA;
    }

    @Override
    public Material getMaterial() {
        return Material.QUARTZ;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Pure silicon extracted from quartz",
                ChatColor.GRAY + "Essential for advanced electronics",
                ChatColor.GRAY + "Crafted in: Basic Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: 1x Block of Quartz in Basic Smelter
        return new ItemStack[] {
            new ItemStack(Material.QUARTZ_BLOCK),  // [0]
            null,                                   // [1]
            null,                                   // [2]
            null,                                   // [3]
            null,                                   // [4]
            null,                                   // [5]
            null,                                   // [6]
            null,                                   // [7]
            null                                    // [8]
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

