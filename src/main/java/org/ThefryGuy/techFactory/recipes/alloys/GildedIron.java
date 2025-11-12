package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.IronDust;

import java.util.List;

public class GildedIron implements RecipeItem {

    @Override
    public String getId() {
        return "gilded_iron";
    }

    @Override
    public String getDisplayName() {
        return "Gilded Iron";
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
                ChatColor.GRAY + "Iron coated with pure gold",
                ChatColor.GRAY + "Combines strength with luxury",
                ChatColor.GRAY + "Crafted in: Basic Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: 24 Karat Gold Ingot + Iron Dust in Basic Smelter
        TwentyFourKaratGoldIngot goldIngot = new TwentyFourKaratGoldIngot();
        IronDust ironDust = new IronDust();
        
        return new ItemStack[] {
            goldIngot.getItemStack(),   // [0]
            ironDust.getItemStack(),    // [1]
            null,                       // [2]
            null,                       // [3]
            null,                       // [4]
            null,                       // [5]
            null,                       // [6]
            null,                       // [7]
            null                        // [8]
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

