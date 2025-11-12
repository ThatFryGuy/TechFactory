package org.ThefryGuy.techFactory.recipes.alloys;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class HardenedMetalIngot implements RecipeItem {

    @Override
    public String getId() {
        return "hardened_metal_ingot";
    }

    @Override
    public String getDisplayName() {
        return "Hardened Metal Ingot";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_AQUA;
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_INGOT;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "An extremely durable metal alloy",
                ChatColor.GRAY + "Reinforced with compressed carbon",
                ChatColor.GRAY + "Crafted in: Alloy Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Damascus Steel + Duralumin + Compressed Carbon + Aluminum Bronze (4 items)
        return new ItemStack[] {
            new org.ThefryGuy.techFactory.recipes.alloys.DamascusSteelIngot().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.alloys.DuraluminIngot().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.resources.CompressedCarbon().getItemStack(),
            new org.ThefryGuy.techFactory.recipes.alloys.AluminumBronzeIngot().getItemStack(),
            null, null, null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

