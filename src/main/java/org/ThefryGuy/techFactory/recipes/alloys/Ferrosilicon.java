package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.IronDust;
import org.ThefryGuy.techFactory.recipes.ingots.IronIngot;

import java.util.List;

public class Ferrosilicon implements RecipeItem {

    @Override
    public String getId() {
        return "ferrosilicon";
    }

    @Override
    public String getDisplayName() {
        return "Ferrosilicon";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GRAY;
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_BLOCK;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "An iron-silicon alloy",
                ChatColor.GRAY + "Used in advanced metallurgy",
                ChatColor.GRAY + "Crafted in: Basic Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: Iron Ingot + Iron Dust + Silicon in Basic Smelter
        IronIngot ironIngot = new IronIngot();
        IronDust ironDust = new IronDust();
        org.ThefryGuy.techFactory.recipes.resources.Silicon silicon = 
            new org.ThefryGuy.techFactory.recipes.resources.Silicon();
        
        return new ItemStack[] {
            ironIngot.getItemStack(),   // [0]
            ironDust.getItemStack(),    // [1]
            silicon.getItemStack(),     // [2]
            null,                       // [3]
            null,                       // [4]
            null,                       // [5]
            null,                       // [6]
            null,                       // [7]
            null                        // [8]
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

