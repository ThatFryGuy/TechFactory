package org.ThefryGuy.techFactory.recipes.ingots;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.ZincDust;

import java.util.List;

public class ZincIngot implements RecipeItem {

    @Override
    public String getId() {
        return "zinc_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Zinc Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GRAY;
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_INGOT;  // Using netherite ingot as zinc ingot icon
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Smelted zinc ingot",
                ChatColor.GRAY + "Created from Zinc Dust"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        ZincDust zincDust = new ZincDust();
        return new ItemStack[] {
            zincDust.getItemStack(),
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

