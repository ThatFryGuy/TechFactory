package org.ThefryGuy.techFactory.recipes.resources;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class SyntheticDiamond implements RecipeItem {

    @Override
    public String getId() {
        return "synthetic_diamond";
    }

    @Override
    public String getDisplayName() {
        return "Synthetic Diamond";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.AQUA;
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A diamond created through extreme pressure",
                ChatColor.GRAY + "Compressed from carbon chunks",
                ChatColor.GRAY + "Crafted in: Pressure Chamber"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: 1x Carbon Chunk in Pressure Chamber
        CarbonChunk carbonChunk = new CarbonChunk();
        
        return new ItemStack[] {
            null,                           // [0]
            null,                           // [1]
            null,                           // [2]
            null,                           // [3]
            carbonChunk.getItemStack(),     // [4] - center
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

