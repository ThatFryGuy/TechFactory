package org.ThefryGuy.techFactory.recipes.resources;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class CarbonChunk implements RecipeItem {

    @Override
    public String getId() {
        return "carbon_chunk";
    }

    @Override
    public String getDisplayName() {
        return "Carbon Chunk";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_GRAY;
    }

    @Override
    public Material getMaterial() {
        return Material.CHARCOAL;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A dense chunk of compressed carbon",
                ChatColor.GRAY + "Reinforced with flint",
                ChatColor.GRAY + "Crafted in: Enhanced Crafting Table"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: 8x Compressed Carbon + 1x Flint in center
        CompressedCarbon compressedCarbon = new CompressedCarbon();
        
        return new ItemStack[] {
            compressedCarbon.getItemStack(),   // [0]
            compressedCarbon.getItemStack(),   // [1]
            compressedCarbon.getItemStack(),   // [2]
            compressedCarbon.getItemStack(),   // [3]
            new ItemStack(Material.FLINT),     // [4] - center
            compressedCarbon.getItemStack(),   // [5]
            compressedCarbon.getItemStack(),   // [6]
            compressedCarbon.getItemStack(),   // [7]
            compressedCarbon.getItemStack()    // [8]
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }
}

