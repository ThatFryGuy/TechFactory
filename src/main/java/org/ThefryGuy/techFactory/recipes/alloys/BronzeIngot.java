package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class BronzeIngot implements RecipeItem {

    @Override
    public String getId() {
        return "bronze_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Bronze Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GOLD;
    }

    @Override
    public Material getMaterial() {
        return Material.COPPER_INGOT;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "An alloy of copper and tin",
                ChatColor.GRAY + "Crafted in: Alloy Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Copper Dust + Tin Dust + Copper Ingot
        return new ItemStack[] {
            new org.ThefryGuy.techFactory.recipes.dusts.CopperDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.dusts.TinDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.ingots.CopperIngot().getItemStack(),
            null, null, null, null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

