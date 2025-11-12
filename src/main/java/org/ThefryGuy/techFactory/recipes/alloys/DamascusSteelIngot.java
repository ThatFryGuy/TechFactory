package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class DamascusSteelIngot implements RecipeItem {

    @Override
    public String getId() {
        return "damascus_steel_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Damascus Steel Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_GRAY;
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_INGOT;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A legendary steel alloy",
                ChatColor.GRAY + "Known for its strength and beauty",
                ChatColor.GRAY + "Crafted in: Alloy Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Steel Ingot + Iron Dust + Carbon + Iron Ingot (4 items)
        return new ItemStack[] {
            new org.ThefryGuy.techFactory.recipes.alloys.SteelIngot().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.dusts.IronDust().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.resources.Carbon().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.ingots.IronIngot().getItemStack(),
            null, null, null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

