package org.ThefryGuy.techFactory.recipes.resources;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class Carbon implements RecipeItem {

    @Override
    public String getId() {
        return "carbon";
    }

    @Override
    public String getDisplayName() {
        return "Carbon";
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
                ChatColor.GRAY + "Compressed carbon",
                ChatColor.GRAY + "Created from 9x Coal",
                ChatColor.GRAY + "Used in alloy recipes"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // 12 coal compressed into 1 carbon
        ItemStack coal = new ItemStack(Material.COAL);
        return new ItemStack[] {
            coal.clone(), coal.clone(), coal.clone(),
            coal.clone(), coal.clone(), coal.clone(),
            coal.clone(), coal.clone(), coal.clone()
        };
    }

    @Override
    public String getMachineType() {
        return "Compressor";
    }
}

