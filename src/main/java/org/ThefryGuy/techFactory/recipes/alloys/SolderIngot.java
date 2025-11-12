package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class SolderIngot implements RecipeItem {

    @Override
    public String getId() {
        return "solder_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Solder Ingot";
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
                ChatColor.GRAY + "An alloy of tin and lead",
                ChatColor.GRAY + "Crafted in: Alloy Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Lead Dust + Tin Dust + Lead Ingot
        return new ItemStack[] {
            new org.ThefryGuy.techFactory.recipes.dusts.LeadDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.dusts.TinDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.ingots.LeadIngot().getItemStack(),
            null, null, null, null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

