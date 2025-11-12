package org.ThefryGuy.techFactory.recipes.components;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class AdvancedCircuitBoard implements RecipeItem {

    @Override
    public String getId() {
        return "advanced_circuit_board";
    }

    @Override
    public String getDisplayName() {
        return "Advanced Circuit Board";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.AQUA;
    }

    @Override
    public Material getMaterial() {
        return Material.COMPARATOR;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "An advanced electronic circuit board",
                ChatColor.GRAY + "Used in sophisticated tech devices",
                ChatColor.GRAY + "Made from Basic Circuit Boards"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Get the BasicCircuitBoard for the center of the recipe
        BasicCircuitBoard basicBoard = new BasicCircuitBoard();
        
        return new ItemStack[] {
            new ItemStack(Material.LAPIS_BLOCK),
            new ItemStack(Material.LAPIS_BLOCK),
            new ItemStack(Material.LAPIS_BLOCK),
            new ItemStack(Material.REDSTONE_BLOCK),
            basicBoard.getItemStack(),
            new ItemStack(Material.REDSTONE_BLOCK),
            new ItemStack(Material.LAPIS_BLOCK),
            new ItemStack(Material.LAPIS_BLOCK),
            new ItemStack(Material.LAPIS_BLOCK)
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }
}