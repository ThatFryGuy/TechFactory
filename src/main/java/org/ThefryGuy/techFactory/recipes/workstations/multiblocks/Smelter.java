package org.ThefryGuy.techFactory.recipes.workstations.multiblocks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

/**
 * Smelter - A multiblock smelter for creating alloys and smelting dusts
 * Structure: Iron Bars + Bricks + Blast Furnace + Campfire
 */
public class Smelter implements RecipeItem {

    @Override
    public String getId() {
        return "smelter";
    }

    @Override
    public String getDisplayName() {
        return "Smelter";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GOLD;
    }

    @Override
    public Material getMaterial() {
        return Material.BLAST_FURNACE;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "A multiblock smelter for creating alloys",
                ChatColor.GRAY + "Smelts dusts into ingots and creates alloys",
                "",
                ChatColor.GOLD + "How to Build:",
                ChatColor.GRAY + "1. Place Blast Furnace in center",
                ChatColor.GRAY + "2. Place Bricks on both sides",
                ChatColor.GRAY + "3. Place Iron Bars on top of Blast Furnace",
                ChatColor.GRAY + "4. Place Campfire underneath Blast Furnace",
                ChatColor.GRAY + "5. Place Bricks underneath the side Bricks",
                "",
                ChatColor.GOLD + "How to Use:",
                ChatColor.GRAY + "Right-click the Blast Furnace",
                ChatColor.GRAY + "Place ingredients in input slots",
                ChatColor.GRAY + "Wait ~1.3 seconds per smelt",
                "",
                ChatColor.GOLD + "Output:",
                ChatColor.GRAY + "Place a chest next to any block",
                ChatColor.GRAY + "Items will output to the chest",
                ChatColor.GRAY + "Or stay in the output slot if no chest"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Multiblock structure layout:
        // Row 1 (top):    [Empty] [Iron Bars] [Empty]
        // Row 2 (middle): [Brick] [Blast Furnace] [Brick]
        // Row 3 (bottom): [Brick] [Campfire] [Brick]
        
        return new ItemStack[] {
            null,                                      // Slot 0: Empty
            new ItemStack(Material.IRON_BARS),        // Slot 1: Iron Bars
            null,                                      // Slot 2: Empty
            new ItemStack(Material.BRICKS),           // Slot 3: Brick
            new ItemStack(Material.BLAST_FURNACE),    // Slot 4: Blast Furnace
            new ItemStack(Material.BRICKS),           // Slot 5: Brick
            new ItemStack(Material.BRICKS),           // Slot 6: Brick
            new ItemStack(Material.CAMPFIRE),         // Slot 7: Campfire
            new ItemStack(Material.BRICKS)            // Slot 8: Brick
        };
    }

    @Override
    public String getMachineType() {
        return "Multiblock";
    }
}

