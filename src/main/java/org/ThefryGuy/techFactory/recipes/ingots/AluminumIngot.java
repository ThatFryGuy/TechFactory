package org.ThefryGuy.techFactory.recipes.ingots;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.AluminumDust;

import java.util.List;

public class AluminumIngot implements RecipeItem {

    @Override
    public String getId() {
        return "aluminum_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Aluminum Ingot";
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
                ChatColor.GRAY + "Smelted aluminum ingot",
                ChatColor.GRAY + "Created from Aluminum Dust"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        AluminumDust aluminumDust = new AluminumDust();
        return new ItemStack[] {
            aluminumDust.getItemStack(),
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

