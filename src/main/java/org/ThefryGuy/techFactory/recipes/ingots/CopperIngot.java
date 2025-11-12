package org.ThefryGuy.techFactory.recipes.ingots;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.CopperDust;

import java.util.List;

public class CopperIngot implements RecipeItem {

    @Override
    public String getId() {
        return "copper_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Copper Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GOLD;
    }

    @Override
    public Material getMaterial() {
        return Material.COPPER_INGOT;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Smelted copper ingot",
                ChatColor.GRAY + "Created from Copper Dust"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        CopperDust copperDust = new CopperDust();
        return new ItemStack[] {
            copperDust.getItemStack(),
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

