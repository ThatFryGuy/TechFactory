package org.ThefryGuy.techFactory.recipes.resources;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.AluminumDust;
import org.ThefryGuy.techFactory.recipes.ingots.AluminumIngot;

import java.util.List;

public class SyntheticEmerald implements RecipeItem {

    @Override
    public String getId() {
        return "synthetic_emerald";
    }

    @Override
    public String getDisplayName() {
        return "Synthetic Emerald";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GREEN;
    }

    @Override
    public Material getMaterial() {
        return Material.EMERALD;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "An emerald created through synthesis",
                ChatColor.GRAY + "Crafted from sapphire and aluminum",
                ChatColor.GRAY + "Crafted in: Basic Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: Synthetic Sapphire + Aluminum Dust + Aluminum Ingot + Glass Pane
        SynthicSapphire synthicSapphire = new SynthicSapphire();
        AluminumDust aluminumDust = new AluminumDust();
        AluminumIngot aluminumIngot = new AluminumIngot();
        
        return new ItemStack[] {
            synthicSapphire.getItemStack(),     // [0]
            aluminumDust.getItemStack(),        // [1]
            aluminumIngot.getItemStack(),       // [2]
            new ItemStack(Material.GLASS_PANE), // [3]
            null,                               // [4]
            null,                               // [5]
            null,                               // [6]
            null,                               // [7]
            null                                // [8]
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

