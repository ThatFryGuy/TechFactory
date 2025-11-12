package org.ThefryGuy.techFactory.recipes.tools;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

/**
 * Gold Pan - A tool for manually panning gravel to get resources
 * 
 * Right-click while holding to pan gravel and get:
 * - Iron Nugget
 * - Sifted Ore Dust
 * - Clay Ball
 * - Flint
 */
public class GoldPan implements RecipeItem {

    @Override
    public String getId() {
        return "gold_pan";
    }

    @Override
    public String getDisplayName() {
        return "Gold Pan";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GOLD;
    }

    @Override
    public Material getMaterial() {
        return Material.BOWL;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A tool for panning gravel",
                ChatColor.GRAY + "Right-click gravel to pan for resources",
                "",
                ChatColor.GOLD + "Possible Drops:",
                ChatColor.GRAY + "• Iron Nugget",
                ChatColor.GRAY + "• Sifted Ore Dust",
                ChatColor.GRAY + "• Clay Ball",
                ChatColor.GRAY + "• Flint",
                "",
                ChatColor.DARK_GRAY + "Crafted at: Enhanced Crafting Table"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe:
        // [Stone] [Bowl] [Stone]
        // [Stone] [Stone] [Stone]
        // [null]  [null]  [null]
        return new ItemStack[] {
            new ItemStack(Material.STONE), new ItemStack(Material.BOWL), new ItemStack(Material.STONE),
            new ItemStack(Material.STONE), new ItemStack(Material.STONE), new ItemStack(Material.STONE),
            null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }
}

