package org.ThefryGuy.techFactory.recipes.ingots;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.GoldDust;

import java.util.List;

public class GoldIngot implements RecipeItem {

    @Override
    public String getId() {
        return "gold_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Gold Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GOLD;
    }

    @Override
    public Material getMaterial() {
        return Material.GOLD_INGOT;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Smelted gold ingot",
                ChatColor.GRAY + "Created from Gold Dust"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        GoldDust goldDust = new GoldDust();
        return new ItemStack[] {
            goldDust.getItemStack(),
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

