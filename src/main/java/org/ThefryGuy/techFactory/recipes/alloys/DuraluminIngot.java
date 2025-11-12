package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class DuraluminIngot implements RecipeItem {

    @Override
    public String getId() {
        return "duralumin_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Duralumin Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.WHITE;
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_INGOT;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "An alloy of aluminum and copper",
                ChatColor.GRAY + "Crafted in: Alloy Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Aluminum Dust + Copper Dust + Aluminum Ingot
        return new ItemStack[] {
            new org.ThefryGuy.techFactory.recipes.dusts.AluminumDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.dusts.CopperDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.ingots.AluminumIngot().getItemStack(),
            null, null, null, null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

