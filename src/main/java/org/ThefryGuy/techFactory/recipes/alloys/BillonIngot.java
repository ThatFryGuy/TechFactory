package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class BillonIngot implements RecipeItem {

    @Override
    public String getId() {
        return "billon_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Billon Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GRAY;
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_INGOT;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "An alloy of silver and copper",
                ChatColor.GRAY + "Crafted in: Alloy Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Silver Dust + Copper Dust + Silver Ingot
        return new ItemStack[] {
            new org.ThefryGuy.techFactory.recipes.dusts.SilverDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.dusts.CopperDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.ingots.SilverIngot().getItemStack(),
            null, null, null, null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

