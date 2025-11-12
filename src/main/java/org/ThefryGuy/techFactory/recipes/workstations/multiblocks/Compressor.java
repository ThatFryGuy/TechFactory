package org.ThefryGuy.techFactory.recipes.workstations.multiblocks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

/**
 * Compressor - A multiblock machine for compressing items
 * Structure: Fence + Pistons + Dispenser
 */
public class Compressor implements RecipeItem {

    @Override
    public String getId() {
        return "compressor";
    }

    @Override
    public String getDisplayName() {
        return "Compressor";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.DARK_GRAY;
    }

    @Override
    public Material getMaterial() {
        return Material.PISTON;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A multiblock machine for compressing items",
                ChatColor.GRAY + "Compresses coal into carbon",
                "",
                ChatColor.GOLD + "How to Build:",
                ChatColor.GRAY + "1. Place Dispenser facing upward in center",
                ChatColor.GRAY + "2. Place Pistons facing up on both sides",
                ChatColor.GRAY + "3. Place Fence on top of Dispenser",
                "",
                ChatColor.GOLD + "How to Use:",
                ChatColor.GRAY + "Right-click the Fence",
                ChatColor.GRAY + "Place items in the Dispenser",
                ChatColor.GRAY + "Close inventory to compress",
                "",
                ChatColor.GOLD + "Recipes:",
                ChatColor.GRAY + "12x Coal → 1x Carbon",
                "",
                ChatColor.GOLD + "Output:",
                ChatColor.GRAY + "Place a chest next to any block",
                ChatColor.GRAY + "Items will output to the chest",
                ChatColor.GRAY + "Or stay in the Dispenser if no chest"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Multiblock structure layout:
        // Row 1 (top):    [Empty] [Fence] [Empty]
        // Row 2 (middle): [Piston] [Dispenser] [Piston]
        // Row 3 (bottom): [Empty] [Empty] [Empty]

        // Create dispenser with custom lore
        ItemStack dispenser = new ItemStack(Material.DISPENSER);
        ItemMeta dispenserMeta = dispenser.getItemMeta();
        if (dispenserMeta != null) {
            dispenserMeta.setLore(List.of(ChatColor.YELLOW + "Facing: UP"));
            dispenser.setItemMeta(dispenserMeta);
        }

        // Create pistons with custom lore
        ItemStack piston = new ItemStack(Material.PISTON);
        ItemMeta pistonMeta = piston.getItemMeta();
        if (pistonMeta != null) {
            pistonMeta.setLore(List.of(ChatColor.YELLOW + "Facing: UP"));
            piston.setItemMeta(pistonMeta);
        }

        return new ItemStack[] {
            null,                                   // Slot 0: Empty
            new ItemStack(Material.OAK_FENCE),     // Slot 1: Fence
            null,                                   // Slot 2: Empty
            piston.clone(),                        // Slot 3: Piston (facing up)
            dispenser,                             // Slot 4: Dispenser (facing up)
            piston.clone(),                        // Slot 5: Piston (facing up)
            null,                                   // Slot 6: Empty
            null,                                   // Slot 7: Empty
            null                                    // Slot 8: Empty
        };
    }

    @Override
    public String getMachineType() {
        return "Multiblock";
    }
}

