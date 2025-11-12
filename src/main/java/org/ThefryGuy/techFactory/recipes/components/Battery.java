package org.ThefryGuy.techFactory.recipes.components;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;
import org.ThefryGuy.techFactory.recipes.resources.Sulfate;
import org.ThefryGuy.techFactory.recipes.ingots.ZincIngot;

import java.util.List;

public class Battery implements RecipeItem {

    @Override
    public String getId() {
        return "battery";
    }

    @Override
    public String getDisplayName() {
        return "Battery";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_RED;
    }

    @Override
    public Material getMaterial() {
        return Material.COPPER_BLOCK;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A portable power storage device",
                ChatColor.GRAY + "Used to power electronic components",
                ChatColor.GRAY + "Made with Zinc and Sulfate"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe:
        // [empty] [redstone_dust] [empty]
        // [zinc_ingot] [sulfate] [copper_ingot]
        // [zinc_ingot] [sulfate] [copper_ingot]
        
        Sulfate sulfate = new Sulfate();
        ZincIngot zincIngot = new ZincIngot();
        
        return new ItemStack[] {
            new ItemStack(Material.AIR),
            new ItemStack(Material.REDSTONE),
            new ItemStack(Material.AIR),
            zincIngot.getItemStack(),
            sulfate.getItemStack(),
            new ItemStack(Material.COPPER_INGOT),
            zincIngot.getItemStack(),
            sulfate.getItemStack(),
            new ItemStack(Material.COPPER_INGOT)
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }
}