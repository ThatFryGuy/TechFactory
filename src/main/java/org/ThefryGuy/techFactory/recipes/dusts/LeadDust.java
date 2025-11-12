package org.ThefryGuy.techFactory.recipes.dusts;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class LeadDust implements RecipeItem {

    @Override
    public String getId() {
        return "lead_dust";
    }

    @Override
    public String getDisplayName() {
        return "Lead Dust";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_GRAY;
    }

    @Override
    public Material getMaterial() {
        return Material.GUNPOWDER;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Dense heavy metal powder",
                ChatColor.GRAY + "Provides radiation shielding"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        SiftedOreDust siftedOreDust = new SiftedOreDust();
        return new ItemStack[] {
            siftedOreDust.getItemStack(),
            null, null,
            null, null, null,
            null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Ore Washer";
    }
}