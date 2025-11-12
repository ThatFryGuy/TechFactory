package org.ThefryGuy.techFactory.recipes.ingots;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.TinDust;

import java.util.List;

public class TinIngot implements RecipeItem {

    @Override
    public String getId() {
        return "tin_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Tin Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GRAY;
    }

    @Override
    public Material getMaterial() {
        return Material.BRICK;  // Using brick as tin ingot icon
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Smelted tin ingot",
                ChatColor.GRAY + "Created from Tin Dust"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        TinDust tinDust = new TinDust();
        return new ItemStack[] {
            tinDust.getItemStack(),
            null, null,
            null, null, null,
            null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

