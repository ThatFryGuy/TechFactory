package org.ThefryGuy.techFactory.recipes.resources;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class Carbonado implements RecipeItem {

    @Override
    public String getId() {
        return "carbonado";
    }

    @Override
    public String getDisplayName() {
        return "Carbonado";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_GRAY;
    }

    @Override
    public Material getMaterial() {
        return Material.COAL_BLOCK;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "The legendary black diamond",
                ChatColor.GRAY + "Hardest material known",
                ChatColor.GRAY + "Refined under extreme pressure",
                ChatColor.GRAY + "Crafted in: Pressure Chamber"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: 1x Raw Carbonado in Pressure Chamber
        RawCarbonado rawCarbonado = new RawCarbonado();
        
        return new ItemStack[] {
            null,                           // [0]
            null,                           // [1]
            null,                           // [2]
            null,                           // [3]
            rawCarbonado.getItemStack(),    // [4] - center
            null,                           // [5]
            null,                           // [6]
            null,                           // [7]
            null                            // [8]
        };
    }

    @Override
    public String getMachineType() {
        return "Pressure Chamber";
    }
}

