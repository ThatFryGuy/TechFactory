package org.ThefryGuy.techFactory.recipes.dusts;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class SiftedOreDust implements RecipeItem {

    @Override
    public String getId() {
        return "sifted_ore_dust";
    }

    @Override
    public String getDisplayName() {
        return "Sifted Ore Dust";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_GRAY;
    }

    @Override
    public Material getMaterial() {
        return Material.GUNPOWDER;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Mixed ore dust from sifting gravel",
                ChatColor.GRAY + "Sift again to separate specific metals"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[] {
            new ItemStack(Material.GRAVEL),
            null, null,
            null, null, null,
            null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Automated Panning Machine";
    }
}