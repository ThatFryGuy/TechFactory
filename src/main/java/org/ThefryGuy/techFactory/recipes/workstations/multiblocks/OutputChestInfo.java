package org.ThefryGuy.techFactory.recipes.workstations.multiblocks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

/**
 * Output Chest Info - Informational item explaining how output chests work with multiblocks
 * This is NOT a craftable item, just an info display in the guide
 */
public class OutputChestInfo implements RecipeItem {

    @Override
    public String getId() {
        return "output_chest_info";
    }

    @Override
    public String getDisplayName() {
        return "Output Chest";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GOLD;
    }

    @Override
    public Material getMaterial() {
        return Material.CHEST;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Automate your multiblocks!",
                "",
                ChatColor.GOLD + "How to Use:",
                ChatColor.GRAY + "1. Build any multiblock machine",
                ChatColor.GRAY + "2. Place a chest touching any block",
                ChatColor.GRAY + "   of the multiblock structure",
                ChatColor.GRAY + "3. Items will auto-output to the chest!",
                "",
                ChatColor.GOLD + "Supported Chests:",
                ChatColor.GRAY + "• Regular Chest",
                ChatColor.GRAY + "• Trapped Chest",
                "",
                ChatColor.GOLD + "Placement:",
                ChatColor.GRAY + "Place chest on any side:",
                ChatColor.GRAY + "North, South, East, West, Up, or Down",
                "",
                ChatColor.YELLOW + "No chest? Items stay in the Dispenser!"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // No recipe - this is just an info item
        return new ItemStack[9];
    }

    @Override
    public String getMachineType() {
        return "Info";
    }
}

