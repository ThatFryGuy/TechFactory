package org.ThefryGuy.techFactory.recipes.resources;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.dusts.AluminumDust;
import org.ThefryGuy.techFactory.recipes.ingots.AluminumIngot;

import java.util.List;

public class SynthicSapphire implements RecipeItem {

    @Override
    public String getId() {
        return "synthic_sapphire";
    }

    @Override
    public String getDisplayName() {
        return "Synthic Sapphire";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.BLUE;
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A synthetic sapphire gemstone",
                ChatColor.GRAY + "Created from aluminum and lapis",
                ChatColor.GRAY + "Crafted in: Basic Smelter"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: Aluminum Dust, Glass, Glass Pane, Aluminum Ingot, Lapis Lazuli
        AluminumDust aluminumDust = new AluminumDust();
        AluminumIngot aluminumIngot = new AluminumIngot();
        
        return new ItemStack[] {
            aluminumDust.getItemStack(),      // [0]
            new ItemStack(Material.GLASS),     // [1]
            new ItemStack(Material.GLASS_PANE), // [2]
            aluminumIngot.getItemStack(),      // [3]
            new ItemStack(Material.LAPIS_LAZULI), // [4]
            null,                              // [5]
            null, null, null                   // [6] [7] [8]
        };
    }

    @Override
    public String getMachineType() {
        return "Smelter";
    }
}

