package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class RedstoneAlloyIngot implements RecipeItem {

    @Override
    public String getId() {
        return "redstone_alloy_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Redstone Alloy Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.RED;
    }

    @Override
    public Material getMaterial() {
        return Material.REDSTONE_BLOCK;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A conductive redstone-infused alloy",
                ChatColor.GRAY + "Combines redstone with hardened metals",
                ChatColor.GRAY + "Crafted in: Basic Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: Redstone Dust + Block of Redstone + Ferrosilicon + Hardened Metal
        Ferrosilicon ferrosilicon = new Ferrosilicon();
        HardenedMetalIngot hardenedMetal = new HardenedMetalIngot();
        
        return new ItemStack[] {
            new ItemStack(Material.REDSTONE),       // [0] - Redstone Dust
            new ItemStack(Material.REDSTONE_BLOCK), // [1] - Block of Redstone
            ferrosilicon.getItemStack(),            // [2] - Ferrosilicon
            hardenedMetal.getItemStack(),           // [3] - Hardened Metal
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

