package org.ThefryGuy.techFactory.recipes.dusts;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class GoldDust implements RecipeItem {

    @Override
    public String getId() {
        return "gold_dust";
    }

    @Override
    public String getDisplayName() {
        return "Gold Dust";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GOLD;
    }

    @Override
    public Material getMaterial() {
        return Material.GUNPOWDER;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Precious gold powder",
                ChatColor.GRAY + "Highly valued in crafting"
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