package org.ThefryGuy.techFactory.recipes.workstations.multiblocks;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.ThefryGuy.techFactory.recipes.RecipeItem;

import java.util.List;

/**
 * Automated Panning - A multiblock machine for panning gravel into sifted ore dust
 * 
 * Process:
 * Stage 1: Gravel → Sifted Ore Dust
 * Stage 2: Sifted Ore Dust → Random Metal Dusts (Iron, Copper, Gold, Tin, Silver, Aluminum, Lead, Zinc, Magnesium)
 * 
 * This is just the RECIPE data (how to build it).
 * The actual functionality is in workstations/multiblocks/AutomatedPanningMachine.java
 */
public class AutomatedPanning implements RecipeItem {

    @Override
    public String getId() {
        return "automated_panning_machine";
    }

    @Override
    public String getDisplayName() {
        return "Automated Panning Machine";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GOLD;
    }

    @Override
    public Material getMaterial() {
        return Material.CAULDRON;
    }

    @Override
    public List<String> getLore() {
        return List.of(
                ChatColor.GRAY + "Automated version of the Gold Pan",
                ChatColor.GRAY + "Pans gravel to extract sifted ore dust",
                "",
                ChatColor.GOLD + "How to Use:",
                ChatColor.GRAY + "Right-click the Cauldron",
                ChatColor.GRAY + "Place gravel inside",
                "",
                ChatColor.GOLD + "Output:",
                ChatColor.GRAY + "Place a chest next to the Cauldron",
                ChatColor.GRAY + "Items will output to the chest",
                ChatColor.GRAY + "Or stay in the Cauldron if no chest",
                "",
                ChatColor.GOLD + "Recipe:",
                ChatColor.GRAY + "Gravel → Sifted Ore Dust"
        );
    }

    @Override
    public ItemStack[] getRecipe() {
        // Recipe: Shows the structure to build
        // Top row: Trapdoor
        // Bottom row: Cauldron
        return new ItemStack[] {
            null, new ItemStack(Material.OAK_TRAPDOOR), null,
            null, new ItemStack(Material.CAULDRON), null,
            null, null, null
        };
    }

    @Override
    public String getMachineType() {
        return "Multiblock"; // This IS a multiblock, not made at a machine
    }
}