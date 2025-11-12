package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class NickelIngot implements RecipeItem {

    @Override
    public String getId() {
        return "nickel_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Nickel Ingot";
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
                ChatColor.GRAY + "An alloy of iron and copper",
                ChatColor.GRAY + "Crafted in: Alloy Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Iron Dust + Iron Ingot + Copper Dust
        return new ItemStack[] {
            new org.ThefryGuy.techFactory.recipes.dusts.IronDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.ingots.IronIngot().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.dusts.CopperDust().getItemStack(),
            null, null, null, null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

