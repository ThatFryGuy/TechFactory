package org.ThefryGuy.techFactory.recipes.workstations.multiblocks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

/**
 * Ore Crusher - A multiblock machine for crushing ore into useful materials
 * 
 * Process:
 * Netherrack (16x) → Sulfate
 * 
 * This is just the RECIPE data (how to build it).
 * The actual functionality is in workstations/multiblocks/OreCrusherMachine.java
 */
public class OreCrusher implements RecipeItem {

    @Override
    public String getId() {
        return "ore_crusher";
    }

    @Override
    public String getDisplayName() {
        return "Ore Crusher";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_RED;
    }

    @Override
    public Material getMaterial() {
        return Material.DISPENSER;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A multiblock machine for crushing ore",
                ChatColor.GRAY + "Crushes materials into their components",
                "",
                ChatColor.GOLD + "How to Build:",
                ChatColor.GRAY + "1. Place Dispenser facing UP",
                ChatColor.GRAY + "2. Place Iron Bars on 2 opposite sides",
                ChatColor.GRAY + "3. Place Nether Brick Fence on TOP of Dispenser",
                "",
                ChatColor.GOLD + "How to Use:",
                ChatColor.GRAY + "Right-click Dispenser to open",
                ChatColor.GRAY + "Click Iron Bars or Fence to crush",
                "",
                ChatColor.GOLD + "Output:",
                ChatColor.GRAY + "Place a chest next to any block",
                ChatColor.GRAY + "Items will output to the chest",
                "",
                ChatColor.GOLD + "Recipe:",
                ChatColor.GRAY + "Netherrack (16x) → Sulfate"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: Shows the structure to build (side view)
        // Top row: Air, Nether Brick Fence, Air
        // Middle row: Iron Bars, Dispenser (facing UP), Iron Bars
        // Bottom row: Empty

        // Create dispenser with custom lore showing it must face UP
        ItemStack dispenser = new ItemStack(Material.DISPENSER);
        ItemMeta dispenserMeta = dispenser.getItemMeta();
        if (dispenserMeta != null) {
            dispenserMeta.setLore(List.of(
                ChatColor.YELLOW + "Facing: UP"
            ));
            dispenser.setItemMeta(dispenserMeta);
        }

        return new ItemStack[] {
            null, new ItemStack(Material.NETHER_BRICK_FENCE), null,
            new ItemStack(Material.IRON_BARS), dispenser, new ItemStack(Material.IRON_BARS),
            null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Multiblock"; // This IS a multiblock, not made at a machine
    }
}