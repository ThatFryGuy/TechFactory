package org.ThefryGuy.techFactory.recipes.resources;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class CompressedCarbon implements RecipeItem {

    @Override
    public String getId() {
        return "compressed_carbon";
    }

    @Override
    public String getDisplayName() {
        return "Compressed Carbon";
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
                ChatColor.GRAY + "Highly compressed carbon material",
                ChatColor.GRAY + "Used in advanced alloys",
                ChatColor.GRAY + "Crafted in: Compressor"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // 9x Coal (vanilla item)
        ItemStack coal = new ItemStack(Material.COAL);
        return new ItemStack[] {
            coal, coal, coal,
            coal, coal, coal,
            coal, coal, coal
        };
    }

    @Override
    public String getMachineType() {
        return "Compressor";
    }
}

