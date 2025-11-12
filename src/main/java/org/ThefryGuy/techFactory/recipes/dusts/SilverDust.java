package org.ThefryGuy.techFactory.recipes.dusts;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class SilverDust implements RecipeItem {

    @Override
    public String getId() {
        return "silver_dust";
    }

    @Override
    public String getDisplayName() {
        return "Silver Dust";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.WHITE;
    }

    @Override
    public Material getMaterial() {
        return Material.GUNPOWDER;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Bright silvery powder",
                ChatColor.GRAY + "Excellent conductor for circuits"
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