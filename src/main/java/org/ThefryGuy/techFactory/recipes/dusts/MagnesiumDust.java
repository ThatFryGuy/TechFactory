package org.ThefryGuy.techFactory.recipes.dusts;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class MagnesiumDust implements RecipeItem {

    @Override
    public String getId() {
        return "magnesium_dust";
    }

    @Override
    public String getDisplayName() {
        return "Magnesium Dust";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.YELLOW; // Sandy/light for magnesium
    }

    @Override
    public Material getMaterial() {
        return Material.GUNPOWDER;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Reactive metal powder",
                ChatColor.GRAY + "Produces intense reactions"
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