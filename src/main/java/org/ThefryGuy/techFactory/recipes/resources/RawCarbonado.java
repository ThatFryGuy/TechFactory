package org.ThefryGuy.techFactory.recipes.resources;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class RawCarbonado implements RecipeItem {

    @Override
    public String getId() {
        return "raw_carbonado";
    }

    @Override
    public String getDisplayName() {
        return "Raw Carbonado";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_GRAY;
    }

    @Override
    public Material getMaterial() {
        return Material.COAL;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "An unrefined black diamond",
                ChatColor.GRAY + "Requires extreme pressure to refine",
                ChatColor.GRAY + "Crafted in: Basic Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: Synthetic Diamond, Carbon Chunk, Glass Pane in Basic Smelter
        SyntheticDiamond syntheticDiamond = new SyntheticDiamond();
        CarbonChunk carbonChunk = new CarbonChunk();
        
        return new ItemStack[] {
            syntheticDiamond.getItemStack(),    // [0]
            carbonChunk.getItemStack(),         // [1]
            new ItemStack(Material.GLASS_PANE), // [2]
            null,                               // [3]
            null,                               // [4]
            null,                               // [5]
            null,                               // [6]
            null,                               // [7]
            null                                // [8]
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

