package org.ThefryGuy.techFactory.recipes.ingots;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.IronDust;

import java.util.List;

public class IronIngot implements RecipeItem {

    @Override
    public String getId() {
        return "iron_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Iron Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.WHITE;
    }

    @Override
    public Material getMaterial() {
        return Material.IRON_INGOT;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Smelted iron ingot",
                ChatColor.GRAY + "Created from Iron Dust"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        IronDust ironDust = new IronDust();
        return new ItemStack[] {
            ironDust.getItemStack(),
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

