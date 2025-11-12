package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class CobaltIngot implements RecipeItem {

    @Override
    public String getId() {
        return "cobalt_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Cobalt Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.BLUE;
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_INGOT;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "An alloy of iron, copper, and nickel",
                ChatColor.GRAY + "Crafted in: Alloy Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Iron Dust + Copper Dust + Nickel Ingot
        return new ItemStack[] {
            new org.ThefryGuy.techFactory.recipes.dusts.IronDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.dusts.CopperDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.alloys.NickelIngot().getItemStack(),
            null, null, null, null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

