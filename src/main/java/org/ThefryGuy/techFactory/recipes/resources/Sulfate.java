package org.ThefryGuy.techFactory.recipes.resources;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class Sulfate implements RecipeItem {

    @Override
    public String getId() {
        return "sulfate";
    }

    @Override
    public String getDisplayName() {
        return "Sulfate";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.YELLOW;
    }

    @Override
    public Material getMaterial() {
        return Material.GLOWSTONE_DUST;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A chemical compound",
                ChatColor.GRAY + "Extracted from Netherrack",
                ChatColor.GRAY + "Used in electronic components"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // 16x Netherrack → 1x Sulfate (in Ore Crusher)
        // Display as a single stack of 16 netherrack to show it's one recipe requirement
        ItemStack netherrack = new ItemStack(Material.NETHERRACK, 16);
        return new ItemStack[] {
            netherrack, null, null,
            null, null, null,
            null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Ore Crusher";
    }
}