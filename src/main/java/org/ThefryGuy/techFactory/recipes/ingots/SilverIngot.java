package org.ThefryGuy.techFactory.recipes.ingots;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.SilverDust;

import java.util.List;

public class SilverIngot implements RecipeItem {

    @Override
    public String getId() {
        return "silver_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Silver Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.WHITE;
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_INGOT;  // Using iron ingot as placeholder
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Smelted silver ingot",
                ChatColor.GRAY + "Created from Silver Dust"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        SilverDust silverDust = new SilverDust();
        return new ItemStack[] {
            silverDust.getItemStack(),
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

