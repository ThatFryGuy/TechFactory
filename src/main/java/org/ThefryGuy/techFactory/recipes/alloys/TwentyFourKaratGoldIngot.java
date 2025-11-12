package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class TwentyFourKaratGoldIngot implements RecipeItem {

    @Override
    public String getId() {
        return "24_karat_gold_ingot";
    }

    @Override
    public String getDisplayName() {
        return "24 Karat Gold Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GOLD;
    }

    @Override
    public Material getMaterial() {
        return Material.GOLD_BLOCK;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Pure 24 karat gold",
                ChatColor.GRAY + "The finest gold alloy",
                ChatColor.GRAY + "Crafted in: Alloy Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // 1 Gold Dust + 10 Gold Ingots (stacked)
        ItemStack goldDust = new org.ThefryGuy.techFactory.recipes.dusts.GoldDust().getItemStack();
        ItemStack goldIngots = new org.ThefryGuy.techFactory.recipes.ingots.GoldIngot().getItemStack();
        goldIngots.setAmount(10);
        
        return new ItemStack[] {
            goldDust,
            goldIngots,
            null, null, null, null, null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

