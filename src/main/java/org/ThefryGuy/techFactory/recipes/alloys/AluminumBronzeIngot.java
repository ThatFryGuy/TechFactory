package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class AluminumBronzeIngot implements RecipeItem {

    @Override
    public String getId() {
        return "aluminum_bronze_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Aluminum Bronze Ingot";
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
                ChatColor.GRAY + "An alloy of aluminum and bronze",
                ChatColor.GRAY + "Crafted in: Alloy Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Aluminum Dust + Bronze Ingot + Aluminum Ingot
        return new ItemStack[] {
            new org.ThefryGuy.techFactory.recipes.dusts.AluminumDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.alloys.BronzeIngot().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.ingots.AluminumIngot().getItemStack(),
            null, null, null, null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

