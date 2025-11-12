package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class CorinthianBronzeIngot implements RecipeItem {

    @Override
    public String getId() {
        return "corinthian_bronze_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Corinthian Bronze Ingot";
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
                ChatColor.GRAY + "A rare alloy of silver, gold, copper, and bronze",
                ChatColor.GRAY + "Crafted in: Alloy Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Silver Dust + Gold Dust + Copper Dust + Bronze Ingot (4 items!)
        return new ItemStack[] {
            new org.ThefryGuy.techFactory.recipes.dusts.SilverDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.dusts.GoldDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.dusts.CopperDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.alloys.BronzeIngot().getItemStack(),
            null, null, null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

