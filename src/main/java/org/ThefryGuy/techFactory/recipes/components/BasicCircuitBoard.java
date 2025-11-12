package org.ThefryGuy.techFactory.recipes.components;

import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

public class BasicCircuitBoard implements RecipeItem {

    @Override
    public String getId() {
        return "basic_circuit_board";
    }

    @Override
    public String getDisplayName() {
        return "Basic Circuit Board";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.BLUE;
    }

    @Override
    public Material getMaterial() {
        return Material.REPEATER;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A basic electronic circuit board",
                ChatColor.GRAY + "Used in simple tech devices"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        return new ItemStack[] {
            new ItemStack(Material.REDSTONE),
            new ItemStack(Material.IRON_INGOT),
            new ItemStack(Material.REDSTONE),
            new ItemStack(Material.COPPER_INGOT),
            new ItemStack(Material.GOLD_NUGGET),
            new ItemStack(Material.COPPER_INGOT),
            new ItemStack(Material.GLASS_PANE),
            new ItemStack(Material.IRON_NUGGET),
            new ItemStack(Material.GLASS_PANE)
        };
    }

    @Override
    public String getMachineType() {
        return "Enhanced Crafting Table";
    }
}