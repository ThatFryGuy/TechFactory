package org.ThefryGuy.techFactory.recipes.ingots;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.MagnesiumDust;

import java.util.List;

public class MagnesiumIngot implements RecipeItem {

    @Override
    public String getId() {
        return "magnesium_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Magnesium Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.WHITE;
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_INGOT;  // Using iron ingot as magnesium ingot icon (like Slimefun)
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Smelted magnesium ingot",
                ChatColor.GRAY + "Created from Magnesium Dust"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        MagnesiumDust magnesiumDust = new MagnesiumDust();
        return new ItemStack[] {
            magnesiumDust.getItemStack(),
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

